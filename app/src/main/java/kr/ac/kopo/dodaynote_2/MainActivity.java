package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    CardView layout_habit_1, layout_habit_2;
    Button btn_habit_list;

    // 체크박스 뷰를 담을 변수 추가
    View check_1, check_2;

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

        layout_habit_1 = findViewById(R.id.layout_habit_1);
        layout_habit_2 = findViewById(R.id.layout_habit_2);

        check_1 = layout_habit_1.findViewById(R.id.check_habit_done);
        check_2 = layout_habit_2.findViewById(R.id.check_habit_done);

        btn_habit_create.setOnClickListener(onClickListener);
        btn_habit_list.setOnClickListener(onClickListener);

        // 카드 자체는 클릭 시 상세 화면으로 이동
        layout_habit_1.setOnClickListener(onClickListener);
        layout_habit_2.setOnClickListener(onClickListener);

        // 체크박스 영역 클릭 시 완료 처리 로직 실행
        check_1.setOnClickListener(onCheckClickListener);
        check_2.setOnClickListener(onCheckClickListener);
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
            } else {
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

    View.OnClickListener onCheckClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CardView parentCard;
            if (v == check_1) parentCard = layout_habit_1;
            else parentCard = layout_habit_2;

            if (parentCard.getAlpha() == 1.0f) {
                parentCard.setAlpha(0.5f);
                parentCard.setCardBackgroundColor(Color.parseColor("#E0E0E0"));

                v.setBackgroundResource(R.drawable.shape_checkbox_checked);

                Toast.makeText(MainActivity.this, "습관 완료!", Toast.LENGTH_SHORT).show();
            } else {
                parentCard.setAlpha(1.0f);
                parentCard.setCardBackgroundColor(Color.WHITE);

                v.setBackgroundResource(R.drawable.shape_checkbox_outline);

                Toast.makeText(MainActivity.this, "다시 도전!", Toast.LENGTH_SHORT).show();
            }
        }
    };
}