package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btn_habit_create;
    // 카드뷰들을 담을 변수 선언
    CardView layout_habit_1, layout_habit_2;

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

        // 1. 위젯 연결
        btn_habit_create = findViewById(R.id.btn_habit_create);

        // 3. 연결 부분 (이곳이 로그상의 38라인 근처일 것입니다)
        layout_habit_1 = findViewById(R.id.layout_habit_1);
        layout_habit_2 = findViewById(R.id.layout_habit_2);

        // 2. 리스너 연결
        btn_habit_create.setOnClickListener(onClickListener);

        // 카드 1 설정
        layout_habit_1.setOnClickListener(onClickListener);
        layout_habit_1.setOnLongClickListener(onLongClickListener); // 롱클릭은 완료 처리용

        // 카드 2 설정
        layout_habit_2.setOnClickListener(onClickListener);
        layout_habit_2.setOnLongClickListener(onLongClickListener);
    }

    // [클릭 리스너] - 페이지 이동 및 데이터 전달
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_habit_create) {
                Intent intent = new Intent(MainActivity.this, HabitCreateActivity.class);
                startActivity(intent);
            } else {
                // 어떤 카드가 클릭되었는지에 따라 다른 제목 전달
                Intent intent = new Intent(MainActivity.this, HabitDetailActivity.class);

                if (v.getId() == R.id.layout_habit_1) {
                    intent.putExtra("habit_title", "매일 20분 걷기");
                } else if (v.getId() == R.id.layout_habit_2) {
                    intent.putExtra("habit_title", "물 2L 마시기");
                }

                startActivity(intent);
            }
        }
    };

    // [롱 클릭 리스너] - 완료 처리 기능 (시각적 변화)
    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // 현재 투명도를 체크하여 완료 상태 토글
            if (v.getAlpha() == 1.0f) {
                v.setAlpha(0.5f); // 반투명 (완료 느낌)
                v.setBackgroundColor(Color.parseColor("#E0E0E0")); // 연한 회색 배경
                Toast.makeText(MainActivity.this, "습관 완료!", Toast.LENGTH_SHORT).show();
            } else {
                v.setAlpha(1.0f); // 복구
                v.setBackgroundColor(Color.WHITE); // 원복
                Toast.makeText(MainActivity.this, "다시 도전!", Toast.LENGTH_SHORT).show();
            }
            return true; // 이벤트를 여기서 소비함 (일반 클릭이 실행되지 않게 함)
        }
    };
}