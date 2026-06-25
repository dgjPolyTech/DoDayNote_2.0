package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitListActivity extends AppCompatActivity {

    private RecyclerView rvHabitHistory;
    private HabitHistoryAdapter adapter;
    private TextView textTotalHabits;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list);

        apiService = ApiClient.getApiService();

        TextView btnBack = findViewById(R.id.btn_back);
        textTotalHabits = findViewById(R.id.text_total_habits);
        rvHabitHistory = findViewById(R.id.rv_habit_history);

        rvHabitHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitHistoryAdapter();
        rvHabitHistory.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        adapter.setOnItemClickListener(habit -> {
            Intent intent = new Intent(HabitListActivity.this, HabitListDetailActivity.class);
            intent.putExtra("habit_id", habit.getId());
            intent.putExtra("habit_title", habit.getTitle());
            String dateRange = (habit.getStartDate() != null ? habit.getStartDate() : "") + " ~ " +
                               (habit.getEndDate() != null ? habit.getEndDate() : "");
            intent.putExtra("habit_date", dateRange);
            startActivity(intent);
        });

        loadCompletedHabits();
    }

    private void loadCompletedHabits() {
        android.content.SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", "");

        apiService.getCompletedHabits(userEmail).enqueue(new Callback<List<Habit>>() {
            @Override
            public void onResponse(Call<List<Habit>> call, Response<List<Habit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Habit> completedHabits = response.body();
                    adapter.setItems(completedHabits);
                    textTotalHabits.setText("전체 습관 수: " + completedHabits.size() + "개");
                } else {
                    Toast.makeText(HabitListActivity.this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Habit>> call, Throwable t) {
                Log.e("HabitListActivity", "API 호출 실패", t);
                Toast.makeText(HabitListActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}