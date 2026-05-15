package kr.ac.kopo.dodaynote_2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class HabitCreateActivity extends AppCompatActivity {

    // 1. 객체명에도 규칙 적용 (btn_, edit_, check_ 등)
    private EditText editHabitTitle;
    private CalendarView calStart;
    private NumberPicker pickerDuration;
    private TextView textToggleOptions;
    private LinearLayout layoutOptionsContainer;
    private CheckBox checkMon, checkTue, checkWed, checkThu, checkFri, checkSat, checkSun;
    private Switch switchAlarm;
    private TextView textResult;

    // 시작 날짜 변수
    private int startYear, startMonth, startDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_create);

        // 루트 레이아웃 ID 복구 완료 (R.id.habit_create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.habit_create), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. 뷰 바인딩
        editHabitTitle = findViewById(R.id.edit_habit_title);
        calStart = findViewById(R.id.cal_start);
        pickerDuration = findViewById(R.id.picker_duration);
        textToggleOptions = findViewById(R.id.text_toggle_options);
        layoutOptionsContainer = findViewById(R.id.layout_options_container);
        textResult = findViewById(R.id.text_result);

        checkMon = findViewById(R.id.check_mon);
        checkTue = findViewById(R.id.check_tue);
        checkWed = findViewById(R.id.check_wed);
        checkThu = findViewById(R.id.check_thu);
        checkFri = findViewById(R.id.check_fri);
        checkSat = findViewById(R.id.check_sat);
        checkSun = findViewById(R.id.check_sun);
        switchAlarm = findViewById(R.id.switch_alarm);

        // 3. NumberPicker 범위 설정 (필수!)
        pickerDuration.setMinValue(5);
        pickerDuration.setMaxValue(120);
        pickerDuration.setValue(30);

        // 4. 캘린더 초기값 (오늘)
        Calendar today = Calendar.getInstance();
        startYear = today.get(Calendar.YEAR);
        startMonth = today.get(Calendar.MONTH) + 1;
        startDay = today.get(Calendar.DAY_OF_MONTH);

        // 5. 추가 옵션 접기/펼치기 (Toggle) 로직
        textToggleOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutOptionsContainer.getVisibility() == View.GONE) {
                    layoutOptionsContainer.setVisibility(View.VISIBLE);
                    textToggleOptions.setText("추가 옵션 (선택) ▲");
                } else {
                    layoutOptionsContainer.setVisibility(View.GONE);
                    textToggleOptions.setText("추가 옵션 (선택) ▼");
                }
            }
        });

        setupListeners();
        updateSummary();
    }

    // 모든 입력 감지기 세팅
    private void setupListeners() {
        editHabitTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateSummary(); }
        });

        calStart.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            startYear = year;
            startMonth = month + 1;
            startDay = dayOfMonth;
            updateSummary();
        });

        pickerDuration.setOnValueChangedListener((picker, oldVal, newVal) -> updateSummary());

        CompoundButton.OnCheckedChangeListener checkListener = (buttonView, isChecked) -> updateSummary();
        checkMon.setOnCheckedChangeListener(checkListener);
        checkTue.setOnCheckedChangeListener(checkListener);
        checkWed.setOnCheckedChangeListener(checkListener);
        checkThu.setOnCheckedChangeListener(checkListener);
        checkFri.setOnCheckedChangeListener(checkListener);
        checkSat.setOnCheckedChangeListener(checkListener);
        checkSun.setOnCheckedChangeListener(checkListener);
        switchAlarm.setOnCheckedChangeListener(checkListener);
    }

    // 요약본 업데이트
    private void updateSummary() {
        String title = editHabitTitle.getText().toString().trim();
        if (title.isEmpty()) title = "(습관 이름)";

        // 30일 뒤 날짜 계산
        int durationDays = 30;
        Calendar endCal = Calendar.getInstance();
        endCal.set(startYear, startMonth - 1, startDay);
        endCal.add(Calendar.DAY_OF_MONTH, durationDays);

        int endYear = endCal.get(Calendar.YEAR);
        int endMonth = endCal.get(Calendar.MONTH) + 1;
        int endDay = endCal.get(Calendar.DAY_OF_MONTH);

        int minutes = pickerDuration.getValue();

        StringBuilder daysBuilder = new StringBuilder();
        if (checkMon.isChecked()) daysBuilder.append("월 ");
        if (checkTue.isChecked()) daysBuilder.append("화 ");
        if (checkWed.isChecked()) daysBuilder.append("수 ");
        if (checkThu.isChecked()) daysBuilder.append("목 ");
        if (checkFri.isChecked()) daysBuilder.append("금 ");
        if (checkSat.isChecked()) daysBuilder.append("토 ");
        if (checkSun.isChecked()) daysBuilder.append("일 ");

        String days = daysBuilder.toString().trim();
        if (days.isEmpty()) days = "매일"; // 체크가 없으면 기본으로 매일로 간주

        String alarmStatus = switchAlarm.isChecked() ? "ON" : "OFF";

        String summary = String.format(
                "%d년 %d월 %d일 ~ %d년 %d월 %d일까지 (%d일간)\n" +
                        "하루 %d분씩 [%s]을(를) %s에 합니다.\n" +
                        "알림 설정: %s",
                startYear, startMonth, startDay,
                endYear, endMonth, endDay, durationDays,
                minutes, title, days, alarmStatus
        );

        textResult.setText(summary);
    }
}