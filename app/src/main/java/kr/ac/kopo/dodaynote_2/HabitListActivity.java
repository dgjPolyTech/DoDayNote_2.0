package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HabitListActivity extends AppCompatActivity {

    // 1. 목록에 있는 항목(레이아웃 또는 카드뷰)들을 담을 변수 선언
    View layout_habit_1, layout_habit_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list);

        TextView btnBack = findViewById(R.id.btn_back);

        // 2. XML에 있는 습관 항목 ID 연결
        // ⚠️ 주의: 실제 activity_habit_list.xml에 부여하신 ID로 이름을 맞춰주세요!
        layout_habit_1 = findViewById(R.id.layout_habit_1);
        layout_habit_2 = findViewById(R.id.layout_habit_2);

        // 뒤로가기 버튼 클릭 이벤트
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼을 누르면 현재 리스트 창을 닫고 이전 화면으로 돌아감
                finish();
            }
        });

        View.OnClickListener itemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitListActivity.this, HabitListDetailActivity.class);
                startActivity(intent);
            }
        };

        // 4. 찾아온 뷰 객체에 클릭 리스너 달아주기
        // (null 체크를 통해 앱이 강제 종료되는 것을 방지합니다)
        if (layout_habit_1 != null) {
            layout_habit_1.setOnClickListener(itemClickListener);
        }
        if (layout_habit_2 != null) {
            layout_habit_2.setOnClickListener(itemClickListener);
        }
    }
}