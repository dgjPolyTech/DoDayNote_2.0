package kr.ac.kopo.dodaynote_2;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HabitDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        // 1. 제목 데이터 받기 (MainActivity에서 보낸 것)
        String title = getIntent().getStringExtra("habit_title");
        TextView textHabitTitle = findViewById(R.id.text_habit_title);
        if (title != null) textHabitTitle.setText(title);

        // 2. X 버튼 클릭 시 뒤로 가기 (현재 화면 종료)
        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티를 닫고 이전 화면으로 이동
            }
        });
    }
}