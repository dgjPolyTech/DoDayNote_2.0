package kr.ac.kopo.dodaynote_2;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kr.ac.kopo.dodaynote_2.domain.HabitRecord;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitPlayActivity extends AppCompatActivity {

    private TextView textPlayHabitTitle;
    private TextView textTimerCountdown;
    private TextView textTimerStatus;
    private CircularProgressIndicator progressTimer;
    private Button btnToggleTimer;
    private Button btnStopTimer;
    private ImageButton btnClosePlay;

    private String habitTitle;
    private Long habitId;
    private int targetMinutes;
    private int todayProgressMinutes;
    private long timeTotalInMillis;
    private long timeLeftInMillis;
    private long sessionStartLeftInMillis;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_play);

        // Intent 데이터 수신
        habitId = getIntent().getLongExtra("habit_id", -1L);
        habitTitle = getIntent().getStringExtra("habit_title");
        if (habitTitle == null) {
            habitTitle = "매일 20분 걷기";
        }
        targetMinutes = getIntent().getIntExtra("target_minutes", 20);
        todayProgressMinutes = getIntent().getIntExtra("today_progress", 0);

        // 전체 목표 시간 (분 -> 밀리초)
        timeTotalInMillis = (long) targetMinutes * 60 * 1000;
        
        // 남은 시간 계산 (이어서 하기 로직)
        long alreadyDoneInMillis = (long) todayProgressMinutes * 60 * 1000;
        timeLeftInMillis = timeTotalInMillis - alreadyDoneInMillis;
        
        // 현재 세션 시작 시점의 남은 시간 저장 (증분 계산용)
        sessionStartLeftInMillis = timeLeftInMillis;

        if (timeLeftInMillis <= 0) {
            timeLeftInMillis = 0;
            Toast.makeText(this, "이미 오늘 목표를 달성했습니다!", Toast.LENGTH_SHORT).show();
        }

        // UI 위젯 연결
        textPlayHabitTitle = findViewById(R.id.text_play_habit_title);
        textTimerCountdown = findViewById(R.id.text_timer_countdown);
        textTimerStatus = findViewById(R.id.text_timer_status);
        progressTimer = findViewById(R.id.progress_timer);
        btnToggleTimer = findViewById(R.id.btn_toggle_timer);
        btnStopTimer = findViewById(R.id.btn_stop_timer);
        btnClosePlay = findViewById(R.id.btn_close_play);

        // 초기 데이터 세팅
        textPlayHabitTitle.setText(habitTitle);
        
        // CircularProgressIndicator 설정
        progressTimer.setMax((int) (timeTotalInMillis / 1000));
        progressTimer.setProgress((int) (timeLeftInMillis / 1000));

        updateCountDownText();

        // 버튼 리스너 바인딩
        btnToggleTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        btnStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAndSaveRecord();
            }
        });

        btnClosePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmExit();
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeLeftInMillis = 0;
                updateCountDownText();
                updateProgressBar();
                
                textTimerStatus.setText("완료됨!");
                textTimerStatus.setTextColor(Color.parseColor("#70C18E"));
                btnToggleTimer.setText("완료");
                btnToggleTimer.setEnabled(false);
                
                // 완료 시 서버 저장
                saveProgressToServer();
                
                Toast.makeText(HabitPlayActivity.this, "목표 시간을 달성했습니다! 대단해요!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        textTimerStatus.setText("실천 중");
        textTimerStatus.setTextColor(Color.parseColor("#70C18E"));
        btnToggleTimer.setText("일시 정지");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        textTimerStatus.setText("일시 정지됨");
        textTimerStatus.setTextColor(Color.parseColor("#FF5252"));
        btnToggleTimer.setText("다시 시작");
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textTimerCountdown.setText(timeLeftFormatted);
    }

    private void updateProgressBar() {
        int progressSeconds = (int) (timeLeftInMillis / 1000);
        progressTimer.setProgress(progressSeconds);
    }

    private void stopAndSaveRecord() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        saveProgressToServer();
        finish();
    }

    private void saveProgressToServer() {
        // 이번 세션에서 진행한 시간 계산 (밀리초 -> 분)
        long sessionElapsedMillis = sessionStartLeftInMillis - timeLeftInMillis;
        int sessionElapsedMinutes = (int) (sessionElapsedMillis / 1000) / 60;

        if (sessionElapsedMinutes <= 0 && sessionElapsedMillis > 0) {
            // 1분 미만이라도 실천했다면 최소 1분으로 기록하거나 초 단위 저장이 필요할 수 있음
            // 일단은 0분 이상인 경우에만 전송
            sessionElapsedMinutes = 0; 
        }

        if (habitId == -1L || sessionElapsedMinutes < 0) return;

        Map<String, Integer> request = new HashMap<>();
        request.put("progressMinutes", sessionElapsedMinutes);

        ApiClient.getApiService().addProgress(habitId, request).enqueue(new Callback<HabitRecord>() {
            @Override
            public void onResponse(Call<HabitRecord> call, Response<HabitRecord> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HabitRecord record = response.body();
                    if (record.isDone()) {
                        Toast.makeText(HabitPlayActivity.this, "오늘의 목표 달성! 🏆", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HabitPlayActivity.this, "실천 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                    // 세션 시작 시점 갱신 (중복 저장 방지)
                    sessionStartLeftInMillis = timeLeftInMillis;
                }
            }

            @Override
            public void onFailure(Call<HabitRecord> call, Throwable t) {
                Log.e("API_PROGRESS", "전송 실패: " + t.getMessage());
            }
        });
    }

    private void confirmExit() {
        if (isTimerRunning) {
            pauseTimer();
        }
        Toast.makeText(this, "실천을 중단하고 이전 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
