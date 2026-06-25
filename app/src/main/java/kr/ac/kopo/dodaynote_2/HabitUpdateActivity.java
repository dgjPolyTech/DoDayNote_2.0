package kr.ac.kopo.dodaynote_2;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitUpdateActivity extends AppCompatActivity {

    private EditText editHabitTitle;
    private CardView cardStartDate, cardEndDate;
    private TextView textStartDate, textEndDate;
    private NumberPicker pickerDuration;
    private CheckBox checkMon, checkTue, checkWed, checkThu, checkFri, checkSat, checkSun;
    private SwitchCompat switchAlarm;
    private TextView textResult;
    private ImageButton btn_back;
    private Button btn_done;

    private Long habitId;
    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    private SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_update); // XML 파일명에 맞춰 확인 필요

        // 1. 뷰 바인딩
        btn_back = findViewById(R.id.btn_back);
        btn_done = findViewById(R.id.btn_done);
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

        // 2. Intent 데이터 수신 및 초기화
        initDataFromIntent();

        // 3. 리스너 설정
        setupClickListeners();
        setupChangeListeners();

        // 최초 요약본 업데이트
        updateSummary();
    }

    // 데이터 초기화 로직
    private void initDataFromIntent() {
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // DETAIL에서 보낸 값들 받기
        habitId = getIntent().getLongExtra("habit_id", -1L);
        String title = getIntent().getStringExtra("habit_title");
        String startDateStr = getIntent().getStringExtra("start_date");
        String endDateStr = getIntent().getStringExtra("end_date");
        int duration = getIntent().getIntExtra("duration", 20);

        if (title != null) editHabitTitle.setText(title);

        // 날짜 문자열을 Calendar 객체로 변환
        try {
            if (startDateStr != null) startCalendar.setTime(sdf.parse(startDateStr));
            if (endDateStr != null) endCalendar.setTime(sdf.parse(endDateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        updateDateLabels();

        // NumberPicker 설정
        pickerDuration.setMinValue(5);
        pickerDuration.setMaxValue(120);
        pickerDuration.setValue(duration);

        // 스위치 및 체크박스
        switchAlarm.setChecked(getIntent().getBooleanExtra("alarm_on", true));
        String activeDays = getIntent().getStringExtra("active_days");
        if (activeDays != null && activeDays.length() >= 7) {
            checkMon.setChecked(activeDays.charAt(0) == '1');
            checkTue.setChecked(activeDays.charAt(1) == '1');
            checkWed.setChecked(activeDays.charAt(2) == '1');
            checkThu.setChecked(activeDays.charAt(3) == '1');
            checkFri.setChecked(activeDays.charAt(4) == '1');
            checkSat.setChecked(activeDays.charAt(5) == '1');
            checkSun.setChecked(activeDays.charAt(6) == '1');
        } else {
            checkMon.setChecked(true);
            checkTue.setChecked(true);
            checkWed.setChecked(true);
            checkThu.setChecked(true);
            checkFri.setChecked(true);
            checkSat.setChecked(true);
            checkSun.setChecked(true);
        }
    }

    private void setupClickListeners() {
        // 뒤로가기 버튼
        btn_back.setOnClickListener(v -> finish());

        // 날짜 선택 버튼은 수정 시 비활성화됨 (클릭 이벤트 제거)

        // [핵심] 수정 완료 버튼 (유효성 검사 포함)
        btn_done.setOnClickListener(v -> {
            String title = editHabitTitle.getText().toString().trim();
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            int durationDays = (int) (diff / (24 * 60 * 60 * 1000)) + 1;

            if (title.isEmpty()) {
                Toast.makeText(this, "오류: 수정할 습관 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (durationDays < 1) {
                Toast.makeText(this, "오류: 종료일은 시작일보다 빠를 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버로 보낼 Habit 객체 구성
            Habit habit = new Habit();
            habit.setTitle(title);
            habit.setStartDate(apiSdf.format(startCalendar.getTime()));
            habit.setEndDate(apiSdf.format(endCalendar.getTime()));
            habit.setAlertOn(switchAlarm.isChecked());
            habit.setTargetMinutes(pickerDuration.getValue());

            StringBuilder activeDaysBuilder = new StringBuilder();
            activeDaysBuilder.append(checkMon.isChecked() ? "1" : "0");
            activeDaysBuilder.append(checkTue.isChecked() ? "1" : "0");
            activeDaysBuilder.append(checkWed.isChecked() ? "1" : "0");
            activeDaysBuilder.append(checkThu.isChecked() ? "1" : "0");
            activeDaysBuilder.append(checkFri.isChecked() ? "1" : "0");
            activeDaysBuilder.append(checkSat.isChecked() ? "1" : "0");
            activeDaysBuilder.append(checkSun.isChecked() ? "1" : "0");
            habit.setActiveDays(activeDaysBuilder.toString());

            // PUT API 호출로 서버에 수정 내용 저장
            ApiClient.getApiService().updateHabit(habitId, habit).enqueue(new Callback<Habit>() {
                @Override
                public void onResponse(Call<Habit> call, Response<Habit> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(HabitUpdateActivity.this, "습관 정보가 성공적으로 수정되었습니다!", Toast.LENGTH_SHORT).show();
                        // RESULT_OK를 세팅해야 HabitDetailActivity의 updateLauncher가 감지합니다.
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(HabitUpdateActivity.this, "수정에 실패했습니다. (서버 오류)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Habit> call, Throwable t) {
                    Toast.makeText(HabitUpdateActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupChangeListeners() {
        // 제목 변경 감지
        editHabitTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateSummary(); }
        });

        // 분 선택 변경 감지
        pickerDuration.setOnValueChangedListener((picker, oldVal, newVal) -> updateSummary());

        // 요일 및 알림 변경 감지
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



    private void updateDateLabels() {
        textStartDate.setText(sdf.format(startCalendar.getTime()));
        textEndDate.setText(sdf.format(endCalendar.getTime()));
    }

    // 실시간 요약본 갱신 로직 (Create와 동일한 형식)
    private void updateSummary() {
        String title = editHabitTitle.getText().toString().trim();
        if (title.isEmpty()) title = "(습관 이름)";

        long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        int durationDays = (int) (diff / (24 * 60 * 60 * 1000)) + 1;
        int displayDays = Math.max(durationDays, 0);

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
        if (days.isEmpty()) days = "매일";

        String alarmStatus = switchAlarm.isChecked() ? "ON" : "OFF";

        String summary = String.format(
                "수정된 기간: %s ~ %s (%d일간)\n" +
                        "하루 %d분씩 [%s]을(를) %s에 합니다.\n" +
                        "알림 설정: %s",
                sdf.format(startCalendar.getTime()), sdf.format(endCalendar.getTime()), displayDays,
                minutes, title, days, alarmStatus
        );

        textResult.setText(summary);
    }
}