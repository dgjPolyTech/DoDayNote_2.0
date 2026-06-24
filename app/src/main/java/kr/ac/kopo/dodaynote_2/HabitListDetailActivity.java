package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import kr.ac.kopo.dodaynote_2.domain.AiFeedbackResponse;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitListDetailActivity extends AppCompatActivity {

    private ImageButton btn_close;
    private TextView textHabitTitle;
    private TextView textHabitDate;
    private TextView textHabitCycle;
    private TextView textAiFeedback;
    private ApiService apiService;
    private Long habitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list_detail);

        btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener(v -> finish());

        textHabitTitle = findViewById(R.id.text_habit_title);
        textHabitDate = findViewById(R.id.text_habit_date);
        textHabitCycle = findViewById(R.id.text_habit_cycle);
        textAiFeedback = findViewById(R.id.text_ai_feedback);

        apiService = ApiClient.getApiService();

        // Hide cycle for completed habits (optional, or we can fetch it)
        textHabitCycle.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) {
            habitId = intent.getLongExtra("habit_id", -1);
            String title = intent.getStringExtra("habit_title");
            String date = intent.getStringExtra("habit_date");

            if (title != null) textHabitTitle.setText(title);
            if (date != null) textHabitDate.setText(date);

            if (habitId != -1) {
                loadAiFeedback(habitId);
            } else {
                textAiFeedback.setText("잘못된 접근입니다.");
            }
        }
    }

    private void loadAiFeedback(Long id) {
        apiService.getAiFeedback(id).enqueue(new Callback<AiFeedbackResponse>() {
            @Override
            public void onResponse(Call<AiFeedbackResponse> call, Response<AiFeedbackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    textAiFeedback.setText(response.body().getFeedback());
                } else {
                    textAiFeedback.setText("AI 피드백을 생성할 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<AiFeedbackResponse> call, Throwable t) {
                Log.e("HabitListDetail", "AI 피드백 호출 실패", t);
                textAiFeedback.setText("네트워크 오류로 AI 피드백을 가져올 수 없습니다.");
            }
        });
    }
}