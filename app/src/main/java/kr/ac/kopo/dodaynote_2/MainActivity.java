package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

// 서버 통신(Retrofit)을 위해 필요한 정석 라이브러리 및 패키지 임포트
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btn_habit_create;
    Button btn_habit_list;
    TextView text_total_habits;

    // 리사이클러뷰 관련 변수
    RecyclerView recyclerHabits;
    HabitAdapter habitAdapter;

    // 상세 화면에서 수정/삭제 완료 시 RESULT_OK를 받아 목록을 새로고침합니다.
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    (ActivityResult result) -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadHabitsFromServer();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_habit_create = findViewById(R.id.btn_habit_create);
        btn_habit_list = findViewById(R.id.btn_habit_list);
        text_total_habits = findViewById(R.id.text_total_habits);

        // 리사이클러뷰 설정
        recyclerHabits = findViewById(R.id.recycler_habits);
        habitAdapter = new HabitAdapter(new HabitAdapter.OnHabitClickListener() {
            @Override
            public void onHabitClick(Habit habit) {
                Intent intent = new Intent(MainActivity.this, HabitDetailActivity.class);
                intent.putExtra("habit_id", habit.getId());
                intent.putExtra("habit_title", habit.getTitle());
                intent.putExtra("target_minutes", habit.getTargetMinutes());
                intent.putExtra("start_date", habit.getStartDate());
                intent.putExtra("end_date", habit.getEndDate());
                intent.putExtra("is_alert_on", habit.isAlertOn());
                
                // 오늘 날짜 기록 찾기
                int todayProgress = 0;
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
                if (habit.getRecords() != null) {
                    for (HabitRecord record : habit.getRecords()) {
                        if (record.getRecordDate() != null && record.getRecordDate().startsWith(today)) {
                            todayProgress = record.getProgressMinutes();
                            break;
                        }
                    }
                }
                intent.putExtra("today_progress", todayProgress);
                // 수정/삭제 RESULT_OK 감지를 위해 detailLauncher로 실행
                detailLauncher.launch(intent);
            }

            @Override
            public void onCheckClick(Habit habit, int position) {
                // 특정 고정 날짜가 아닌, 서버에서 내려온 '가장 최근 기록' 혹은 '가장 최신 날짜'를 기준으로 판단하도록 로직 개선
                HabitRecord latestRecord = null;
                if (habit.getRecords() != null && !habit.getRecords().isEmpty()) {
                    // 마지막 기록을 최신으로 간주 (서버에서 날짜순 정렬해서 준다고 가정)
                    latestRecord = habit.getRecords().get(habit.getRecords().size() - 1);
                }

                boolean isDoneNow = (latestRecord != null) && latestRecord.isDone();
                
                Log.d("HABIT_DEBUG", "Habit: " + habit.getTitle() + ", Latest record isDone: " + isDoneNow);

                if (isDoneNow) {
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle("실천 취소")
                            .setMessage("오늘의 실천 기록을 취소하시겠습니까?")
                            .setPositiveButton("확인", (dialog, which) -> toggleHabitStatus(habit, position))
                            .setNegativeButton("취소", null)
                            .show();
                } else {
                    toggleHabitStatus(habit, position);
                }
            }

            private void toggleHabitStatus(Habit habit, int position) {
                ApiClient.getApiService().toggleHabitDone(habit.getId()).enqueue(new Callback<HabitRecord>() {
                    @Override
                    public void onResponse(Call<HabitRecord> call, Response<HabitRecord> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            HabitRecord updatedRecord = response.body();
                            Log.d("HABIT_DEBUG", "Response Record: " + updatedRecord.getRecordDate() + ", isDone: " + updatedRecord.isDone());

                            List<HabitRecord> records = habit.getRecords();
                            if (records == null) {
                                records = new java.util.ArrayList<>();
                                habit.setRecords(records);
                            }

                            // 날짜 기반으로 기존 기록 찾아서 업데이트 (기기 날짜가 아닌 서버가 준 날짜 기준)
                            boolean found = false;
                            String updatedDateStr = updatedRecord.getRecordDate(); 
                            // 시분초 제외한 yyyy-MM-dd 추출
                            String updatedDay = (updatedDateStr != null && updatedDateStr.length() >= 10) 
                                    ? updatedDateStr.substring(0, 10) : updatedDateStr;

                            for (int i = 0; i < records.size(); i++) {
                                String existingDate = records.get(i).getRecordDate();
                                if (existingDate != null && existingDate.startsWith(updatedDay)) {
                                    records.set(i, updatedRecord);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) records.add(updatedRecord);

                            habitAdapter.notifyItemChanged(position);
                            String msg = updatedRecord.isDone() ? "오늘 습관 완료!" : "다시 도전!";
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "상태 업데이트 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<HabitRecord> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "네트워크 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerHabits.setAdapter(habitAdapter);

        btn_habit_create.setOnClickListener(onClickListener);
        btn_habit_list.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabitsFromServer();
    }

    private void loadHabitsFromServer() {
        ApiClient.getApiService().getAllHabits().enqueue(new Callback<List<Habit>>() {
            @Override
            public void onResponse(Call<List<Habit>> call, Response<List<Habit>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<Habit> habitList = response.body();
                    Log.d("SERVER_DB_LOAD", "데이터 불러오기 성공! 개수: " + habitList.size());
                    
                    // 어댑터에 데이터 전달
                    habitAdapter.setHabits(habitList);
                    
                    // 전체 습관 수 텍스트 업데이트
                    text_total_habits.setText("전체 습관 수: " + habitList.size() + "개");
                }
            }

            @Override
            public void onFailure(Call<List<Habit>> call, Throwable t) {
                Log.e("SERVER_DB_LOAD", "불러오기 실패: " + t.getMessage());
                Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_habit_create) {
                Intent intent = new Intent(MainActivity.this, HabitCreateActivity.class);
                startActivity(intent);
            } else if(v.getId() == R.id.btn_habit_list) {
                Intent intent = new Intent(MainActivity.this, HabitListActivity.class);
                startActivity(intent);
            }
        }
    };
}