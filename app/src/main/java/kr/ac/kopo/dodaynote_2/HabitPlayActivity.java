package kr.ac.kopo.dodaynote_2;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class HabitPlayActivity extends AppCompatActivity {

    private TextView textPlayHabitTitle;
    private TextView textTimerCountdown;
    private TextView textTimerStatus;
    private CircularProgressIndicator progressTimer;
    private Button btnToggleTimer;
    private Button btnStopTimer;
    private ImageButton btnClosePlay;

    private String habitTitle;
    private int totalDurationMinutes;
    private long timeTotalInMillis;
    private long timeLeftInMillis;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_play);

        // Intent 데이터 수신
        habitTitle = getIntent().getStringExtra("habit_title");
        if (habitTitle == null) {
            habitTitle = "매일 20분 걷기";
        }
        totalDurationMinutes = getIntent().getIntExtra("duration", 20);

        // 초기 밀리초 설정 (분 * 60초 * 1000)
        timeTotalInMillis = (long) totalDurationMinutes * 60 * 1000;
        timeLeftInMillis = timeTotalInMillis;

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
        
        long elapsedMillis = timeTotalInMillis - timeLeftInMillis;
        int elapsedMinutes = (int) (elapsedMillis / 1000) / 60;
        int elapsedSeconds = (int) (elapsedMillis / 1000) % 60;

        String message;
        if (elapsedMinutes > 0) {
            message = String.format(Locale.getDefault(), "총 %d분 %d초 동안 실천을 완료하고 기록했습니다!", elapsedMinutes, elapsedSeconds);
        } else {
            message = String.format(Locale.getDefault(), "총 %d초 동안 실천을 완료하고 기록했습니다!", elapsedSeconds);
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
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
