package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import kr.ac.kopo.dodaynote_2.domain.Habit;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

// 서버 통신(Retrofit)을 위해 필요한 정석 라이브러리 및 패키지 임포트
import android.util.Log;

import java.util.List;
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
                intent.putExtra("habit_title", habit.getTitle());
                startActivity(intent);
            }

            @Override
            public void onCheckClick(Habit habit, View checkView, CardView cardView) {
                // 완료 상태 토글
                boolean newState = !habit.isDone();
                habit.setDone(newState);
                
                if (newState) {
                    cardView.setAlpha(0.5f);
                    cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
                    checkView.setBackgroundResource(R.drawable.shape_checkbox_checked);
                    Toast.makeText(MainActivity.this, "습관 완료!", Toast.LENGTH_SHORT).show();
                } else {
                    cardView.setAlpha(1.0f);
                    cardView.setCardBackgroundColor(Color.WHITE);
                    checkView.setBackgroundResource(R.drawable.shape_checkbox_outline);
                    Toast.makeText(MainActivity.this, "다시 도전!", Toast.LENGTH_SHORT).show();
                }
                // TODO: 서버에도 상태 변경 저장 필요 시 ApiService에 PATCH/PUT 요청 추가 가능
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