package kr.ac.kopo.dodaynote_2;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitCreateActivity extends AppCompatActivity {

    private EditText editHabitTitle;
    private CardView cardStartDate, cardEndDate;
    private TextView textStartDate, textEndDate;
    private NumberPicker pickerDuration;
    private CheckBox checkMon, checkTue, checkWed, checkThu, checkFri, checkSat, checkSun;
    private Switch switchAlarm;
    private TextView textResult;
    private ImageButton btn_close;
    private Button btn_done; // '습관 형성 시작' 버튼 추가

    // 시작일과 종료일을 저장할 Calendar 객체
    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_create);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.habit_create), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뷰 바인딩
        btn_close = findViewById(R.id.btn_close);
        btn_done = findViewById(R.id.btn_done); // 버튼 바인딩 추가
        editHabitTitle = findViewById(R.id.edit_habit_title);
        cardStartDate = findViewById(R.id.card_start_date);
        cardEndDate = findViewById(R.id.card_end_date);
        textStartDate = findViewById(R.id.text_start_date);
        textEndDate = findViewById(R.id.text_end_date);
        pickerDuration = findViewById(R.id.picker_duration);
        textResult = findViewById(R.id.text_result);

        checkMon = findViewById(R.id.check_mon);
        checkTue = findViewById(R.id.check_tue);
        checkWed = findViewById(R.id.check_wed);
        checkThu = findViewById(R.id.check_thu);
        checkFri = findViewById(R.id.check_fri);
        checkSat = findViewById(R.id.check_sat);
        checkSun = findViewById(R.id.check_sun);
        switchAlarm = findViewById(R.id.switch_alarm);

        // 초기값 설정
        startCalendar = Calendar.getInstance();
        endCalendar = (Calendar) startCalendar.clone();
        endCalendar.add(Calendar.DAY_OF_MONTH, 30); // 기본 30일 설정

        updateDateLabels();

        // 닫기 버튼
        btn_close.setOnClickListener(v -> finish());

        // 시작일 선택
        cardStartDate.setOnClickListener(v -> showDatePicker(true));

        // 종료일 선택
        cardEndDate.setOnClickListener(v -> showDatePicker(false));

        // 습관 형성 시작 버튼 클릭 리스너 및 유효성 검사 추가
        btn_done.setOnClickListener(v -> {
            String title = editHabitTitle.getText().toString().trim();
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            int durationDays = (int) (diff / (24 * 60 * 60 * 1000)) + 1;

            // 1. 제목 입력 검사
            if (title.isEmpty()) {
                Toast.makeText(this, "오류: 습관 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 실천 기간 검사 (마이너스 일수 방지)
            if (durationDays < 1) {
                Toast.makeText(this, "오류: 종료일은 시작일 이후여야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Habit newHabit = new Habit();
            newHabit.setTitle(title);

            ApiClient.getApiService().createHabit(newHabit).enqueue(new Callback<Habit>() {
                @Override
                public void onResponse(Call<Habit> call, Response<Habit> response) {
                    if(response.isSuccessful()) {
                        Log.d("SERVER_DB_SAVE", "저장 성공!");
                        Toast.makeText(HabitCreateActivity.this, "버튼 클릭! 습관 형성을 시작합니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<Habit> call, Throwable t) {
                    Log.e("SERVER_DB_SAVE", "저장 실패: " + t.getMessage());
                }
            });
        });

        // NumberPicker 설정
        pickerDuration.setMinValue(5);
        pickerDuration.setMaxValue(120);
        pickerDuration.setValue(30);

        setupListeners();
        updateSummary();
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar targetCal = isStartDate ? startCalendar : endCalendar;

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            if (isStartDate) {
                startCalendar.set(year, month, dayOfMonth);
            } else {
                endCalendar.set(year, month, dayOfMonth);
            }
            updateDateLabels();
            updateSummary();
        }, targetCal.get(Calendar.YEAR), targetCal.get(Calendar.MONTH), targetCal.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void updateDateLabels() {
        textStartDate.setText(sdf.format(startCalendar.getTime()));
        textEndDate.setText(sdf.format(endCalendar.getTime()));
    }

    private void setupListeners() {
        editHabitTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateSummary(); }
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

    private void updateSummary() {
        String title = editHabitTitle.getText().toString().trim();
        if (title.isEmpty()) title = "(습관 이름)";

        long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        int durationDays = (int) (diff / (24 * 60 * 60 * 1000)) + 1;

        // 요약창에서는 마이너스가 나와도 그대로 보여주거나 0으로 처리 (유효성 검사는 버튼에서 수행)
        int displayDays = Math.max(durationDays, 0);

        int minutes = pickerDuration.getValue();

        StringBuilder daysBuilder = new StringBuilder();
        if (checkMon.isChecked()) daysBuilder.append("월 ");
        if (checkTue.isChecked()) daysBuilder.append("화 ");
        if (checkWed.isChecked()) daysBuilder.append("수 ");
        if (checkThu.isChecked()) daysBuilder.append("목 ");
        if (checkFri.isChecked()) daysBuilder.append("금 "); // 기존 오타 수정 완료 (checkThu -> checkFri)
        if (checkSat.isChecked()) daysBuilder.append("토 ");
        if (checkSun.isChecked()) daysBuilder.append("일 ");

        String days = daysBuilder.toString().trim();
        if (days.isEmpty()) days = "매일";

        String alarmStatus = switchAlarm.isChecked() ? "ON" : "OFF";

        String summary = String.format(
                "%s ~ %s (%d일간)\n" +
                        "하루 %d분씩 [%s]을(를) %s에 합니다.\n" +
                        "알림 설정: %s",
                sdf.format(startCalendar.getTime()), sdf.format(endCalendar.getTime()), displayDays,
                minutes, title, days, alarmStatus
        );

        textResult.setText(summary);
    }
}