package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HabitDetailActivity extends AppCompatActivity {

    Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        // 상단 타이틀 설정
        String title = getIntent().getStringExtra("habit_title");
        TextView textHabitTitle = findViewById(R.id.text_habit_title);
        if (title != null) textHabitTitle.setText(title);

        // --- 특정 단어 포인트 컬러 로직 추가 시작 ---
        TextView textHabitCycle = findViewById(R.id.text_habit_cycle);
        String fullText = "매일 20분 씩 반복(알림 설정 ON)";
        SpannableStringBuilder ssb = new SpannableStringBuilder(fullText);
        int pointColor = Color.parseColor("#70C18E"); // 포인트 초록색

        // 1. "매일" 강조 (0~2번 인덱스)
        ssb.setSpan(new ForegroundColorSpan(pointColor), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 2. "20" 강조 (3~5번 인덱스)
        ssb.setSpan(new ForegroundColorSpan(pointColor), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 3. "ON" 강조 (단어 위치를 찾아서 적용)
        int startOn = fullText.indexOf("ON");
        if (startOn != -1) {
            ssb.setSpan(new ForegroundColorSpan(pointColor), startOn, startOn + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), startOn, startOn + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textHabitCycle.setText(ssb);
        // --- 포인트 컬러 로직 추가 끝 ---

        ImageButton btnClose = findViewById(R.id.btn_close);
        btnUpdate = findViewById(R.id.btn_update);

        btnClose.setOnClickListener(onClickListener);
        btnUpdate.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_update) {
                // 1. 택배 상자(Intent) 만들기
                Intent intent = new Intent(HabitDetailActivity.this, HabitUpdateActivity.class);

                // 2. 상자에 정보 담기 (key - value)
                intent.putExtra("habit_title", "매일 20분 걷기");
                intent.putExtra("start_date", "2026.05.01");
                intent.putExtra("end_date", "2026.05.31");
                intent.putExtra("duration", 20);
                intent.putExtra("alarm_on", true);

                // 요일 데이터 (모두 선택 상태로 보냄)
                intent.putExtra("mon", true);
                intent.putExtra("tue", true);
                intent.putExtra("wed", true);
                intent.putExtra("thu", true);
                intent.putExtra("fri", true);
                intent.putExtra("sat", true);
                intent.putExtra("sun", true);

                // 3. 출발!
                startActivity(intent);

            } else if(v.getId() == R.id.btn_close) {
                finish();
            }
        }
    };
}