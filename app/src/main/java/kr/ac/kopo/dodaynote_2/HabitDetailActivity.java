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
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import android.graphics.drawable.GradientDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import kr.ac.kopo.dodaynote_2.domain.AiFeedbackResponse;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.annotation.NonNull;

public class HabitDetailActivity extends AppCompatActivity {

    Button btnUpdate;
    Button btnPlay;
    TextView textDelete;

    private Long habitId;
    private String habitTitle = "";
    private String habitStartDate = "";
    private String habitEndDate = "";
    private boolean isAlertOn = false;
    private int habitDuration = 20;
    private int todayProgress = 0;
    private String habitActiveDays = "";
    private TextView textAiFeedback;

    // HabitUpdateActivity에서 수정 완료(RESULT_OK) 시 이 액티비티도 RESULT_OK를 세팅하여
    // 최종적으로 MainActivity가 onResume() 시 목록을 새로고침하도록 체인을 이어줍니다.
    private final ActivityResultLauncher<Intent> updateLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    (ActivityResult result) -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // 수정이 완료되었으므로 이 화면도 갱신 필요 신호를 상위로 전파
                            setResult(RESULT_OK);
                            finish();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        // 상단 타이틀 설정
        habitId = getIntent().getLongExtra("habit_id", -1L);
        habitTitle = getIntent().getStringExtra("habit_title");
        if (habitTitle == null) {
            habitTitle = "습관 정보";
        }
        habitDuration = getIntent().getIntExtra("target_minutes", 20);
        todayProgress = getIntent().getIntExtra("today_progress", 0);
        habitStartDate = getIntent().getStringExtra("start_date");
        habitEndDate = getIntent().getStringExtra("end_date");
        isAlertOn = getIntent().getBooleanExtra("is_alert_on", false);
        habitActiveDays = getIntent().getStringExtra("active_days");
        if (habitActiveDays == null) {
            habitActiveDays = "1111111"; // Default to every day if null
        }

        TextView textHabitTitle = findViewById(R.id.text_habit_title);
        textHabitTitle.setText(habitTitle);

        TextView textHabitDate = findViewById(R.id.text_habit_date);
        if (habitStartDate != null && habitEndDate != null) {
            textHabitDate.setText(habitStartDate.replace("-", ".") + " ~ " + habitEndDate.replace("-", "."));
        }

        // --- 특정 단어 포인트 컬러 로직 추가 시작 ---
        TextView textHabitCycle = findViewById(R.id.text_habit_cycle);
        String alertText = isAlertOn ? "ON" : "OFF";
        
        String daysStr = "매일";
        if (habitActiveDays != null && habitActiveDays.length() >= 7) {
            StringBuilder db = new StringBuilder();
            if(habitActiveDays.charAt(0) == '1') db.append("월 ");
            if(habitActiveDays.charAt(1) == '1') db.append("화 ");
            if(habitActiveDays.charAt(2) == '1') db.append("수 ");
            if(habitActiveDays.charAt(3) == '1') db.append("목 ");
            if(habitActiveDays.charAt(4) == '1') db.append("금 ");
            if(habitActiveDays.charAt(5) == '1') db.append("토 ");
            if(habitActiveDays.charAt(6) == '1') db.append("일 ");
            String parsed = db.toString().trim();
            if (!parsed.isEmpty() && !parsed.equals("월 화 수 목 금 토 일")) {
                daysStr = parsed;
            }
        }
        
        String fullText = String.format("%s %d분 씩 반복(알림 설정 %s)", daysStr, habitDuration, alertText);
        SpannableStringBuilder ssb = new SpannableStringBuilder(fullText);
        int pointColor = Color.parseColor("#70C18E"); // 포인트 초록색

        // 1. "매일" 또는 요일 강조
        int startDays = fullText.indexOf(daysStr);
        if (startDays != -1) {
            ssb.setSpan(new ForegroundColorSpan(pointColor), startDays, startDays + daysStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), startDays, startDays + daysStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 2. 시간 강조 (숫자 부분 찾기)
        String durationStr = String.valueOf(habitDuration);
        int startDuration = fullText.indexOf(durationStr);
        if (startDuration != -1) {
            ssb.setSpan(new ForegroundColorSpan(pointColor), startDuration, startDuration + durationStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), startDuration, startDuration + durationStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 3. "ON/OFF" 강조
        int startOn = fullText.indexOf(alertText);
        if (startOn != -1) {
            ssb.setSpan(new ForegroundColorSpan(pointColor), startOn, startOn + alertText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), startOn, startOn + alertText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textHabitCycle.setText(ssb);
        // --- 포인트 컬러 로직 추가 끝 ---

        ImageButton btnClose = findViewById(R.id.btn_close);
        btnUpdate = findViewById(R.id.btn_update);
        btnPlay = findViewById(R.id.btn_play);
        textDelete = findViewById(R.id.text_delete);

        btnClose.setOnClickListener(onClickListener);
        btnUpdate.setOnClickListener(onClickListener);
        btnPlay.setOnClickListener(onClickListener);
        textDelete.setOnClickListener(onClickListener);

        textAiFeedback = findViewById(R.id.text_ai_feedback);

        // 서버에서 기록 데이터 로드
        loadHabitRecordsFromServer();
        loadAiFeedback();
    }

    private void loadAiFeedback() {
        if (habitId == -1L) return;

        ApiClient.getApiService().getAiFeedback(habitId).enqueue(new Callback<AiFeedbackResponse>() {
            @Override
            public void onResponse(@NonNull Call<AiFeedbackResponse> call, @NonNull Response<AiFeedbackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    textAiFeedback.setText(response.body().getFeedback());
                } else {
                    textAiFeedback.setText("피드백을 불러오는 데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AiFeedbackResponse> call, @NonNull Throwable t) {
                textAiFeedback.setText("네트워크 오류가 발생했습니다.");
            }
        });
    }

    private void loadHabitRecordsFromServer() {
        if (habitId == -1L) return;

        ApiClient.getApiService().getHabitRecords(habitId).enqueue(new Callback<List<HabitRecord>>() {
            @Override
            public void onResponse(@NonNull Call<List<HabitRecord>> call, @NonNull Response<List<HabitRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processRecordsAndDrawChart(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<HabitRecord>> call, @NonNull Throwable t) {
                Toast.makeText(HabitDetailActivity.this, "기록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processRecordsAndDrawChart(List<HabitRecord> records) {
        if (habitStartDate == null || habitStartDate.isEmpty()) {
            setupHabitChart(null, null);
            return;
        }

        String datePattern = habitStartDate.contains("-") ? "yyyy-MM-dd" : "yyyy.MM.dd";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.getDefault());

        try {
            Date startDate = sdf.parse(habitStartDate);
            if (startDate == null) {
                setupHabitChart(null, null);
                return;
            }

            // 종료일 파싱 — 실제 주차별 날짜 수(분모) 계산에 사용
            Date endDate = null;
            if (habitEndDate != null && !habitEndDate.isEmpty()) {
                String endPattern = habitEndDate.contains("-") ? "yyyy-MM-dd" : "yyyy.MM.dd";
                endDate = new SimpleDateFormat(endPattern, Locale.getDefault()).parse(habitEndDate);
            }
            // endDate가 없거나 미래면 오늘로 제한
            Date today = new Date();
            Date effectiveEnd = (endDate != null && endDate.before(today)) ? endDate : today;

            // 습관 전체 기간 일수 (startDate ~ effectiveEnd)
            long totalDays = (effectiveEnd.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000) + 1;
            // 마지막 주차 인덱스 (0-based)
            int lastWeekIndex = (int) ((totalDays - 1) / 7);

            // 주차별 달성(done=true) 카운트
            TreeMap<Integer, Integer> weeklyDoneCount = new TreeMap<>();

            for (HabitRecord record : records) {
                if (!record.isDone() || record.getRecordDate() == null) continue;

                String recordDateStr = record.getRecordDate().length() >= 10
                        ? record.getRecordDate().substring(0, 10).replace(".", "-")
                        : record.getRecordDate().replace(".", "-");

                SimpleDateFormat recordSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date rDate = recordSdf.parse(recordDateStr);
                if (rDate == null) continue;

                long diff = rDate.getTime() - startDate.getTime();
                if (diff < 0) continue;

                int days = (int) (diff / (24 * 60 * 60 * 1000));
                int weekIndex = days / 7;

                Integer current = weeklyDoneCount.get(weekIndex);
                weeklyDoneCount.put(weekIndex, (current == null ? 0 : current) + 1);
            }

            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            int maxWeek = Math.max(3, lastWeekIndex);

            for (int i = 0; i <= maxWeek; i++) {
                Integer doneDays = weeklyDoneCount.get(i);
                int success = (doneDays == null) ? 0 : doneDays;

                // ─── 핵심 수정: 분모를 7 고정이 아닌 실제 주차 내 날짜 수로 계산 ───────
                // 마지막 주차는 7일 미만일 수 있음 (예: 습관이 수요일에 끝나면 3일)
                int daysInThisWeek;
                if (i < lastWeekIndex) {
                    daysInThisWeek = 7;                       // 중간 주차: 항상 7일
                } else {
                    // 마지막 주차: 나머지 일수 (1 ~ 7)
                    daysInThisWeek = (int) (totalDays - (long) i * 7);
                    daysInThisWeek = Math.min(daysInThisWeek, 7);
                }
                // ────────────────────────────────────────────────────────────────────────

                float achievementRate = (daysInThisWeek > 0)
                        ? ((float) success / daysInThisWeek) * 100f : 0f;
                entries.add(new Entry((float) i, achievementRate));
                labels.add((i + 1) + "주차");
            }

            setupHabitChart(entries, labels);

        } catch (ParseException e) {
            android.util.Log.e("HabitDetail", "Date parse error: " + e.getMessage());
            setupHabitChart(null, null);
        }
    }

    /**
     * MPAndroidChart의 LineChart를 활용하여 커스텀 꺾은선 차트를 그리는 메서드입니다.
     */
    private void setupHabitChart(List<Entry> dynamicEntries, List<String> dynamicLabels) {
        LineChart lineChart = findViewById(R.id.habit_line_chart);
        if (lineChart == null) return;

        List<Entry> entries;
        final String[] labels;

        if (dynamicEntries != null && !dynamicEntries.isEmpty()) {
            entries = dynamicEntries;
            labels = dynamicLabels.toArray(new String[0]);
        } else {
            // 데이터가 없을 경우 샘플 데이터
            entries = new ArrayList<>();
            entries.add(new Entry(0f, 0f));
            entries.add(new Entry(1f, 0f));
            entries.add(new Entry(2f, 0f));
            entries.add(new Entry(3f, 0f));
            labels = new String[]{"1주차", "2주차", "3주차", "4주차"};
        }

        // 2. 데이터 세트 구성 및 스타일링
        LineDataSet dataSet = new LineDataSet(entries, "달성률 (%)");
        dataSet.setColor(Color.parseColor("#70C18E"));       // 선 색상 (테마 초록색)
        dataSet.setCircleColor(Color.parseColor("#70C18E")); // 데이터 점 색상
        dataSet.setCircleHoleColor(Color.WHITE);             // 점 중앙 흰색 구멍 효과
        dataSet.setCircleRadius(4f);                         // 점 크기
        dataSet.setCircleHoleRadius(2f);
        dataSet.setLineWidth(2f);                            // 선 두께
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);      // 부드러운 곡선 효과
        dataSet.setDrawValues(true);                         // 점 위의 값 텍스트 활성화
        dataSet.setValueTextSize(10f);                       // 값 텍스트 크기
        dataSet.setValueTextColor(Color.parseColor("#70C18E")); // 텍스트 컬러 통일

        // 값 포맷터 설정 (예: 40.0 -> 40%)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Math.round(value) + "%";
            }
        });

        // 선 하단 그라데이션 채우기 적용
        dataSet.setDrawFilled(true);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#3370C18E"), Color.parseColor("#0070C18E")} // 반투명 초록 -> 투명
        );
        dataSet.setFillDrawable(gradientDrawable);

        // 3. 차트 전체 설정
        lineChart.getDescription().setEnabled(false);        // 우측 하단 설명 문구 제거
        lineChart.getLegend().setEnabled(false);             // 범례 제거 (심플함 유지)
        lineChart.setTouchEnabled(true);                     // 터치 가능
        lineChart.setPinchZoom(false);                       // 줌 불가능 (심플 대시보드 목적)
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setExtraOffsets(10f, 15f, 10f, 10f);       // 여백 설정

        // 4. X축(가로축) 설정
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);       // 아래쪽에 위치
        xAxis.setDrawGridLines(false);                       // 수직 격자선 제거
        xAxis.setDrawAxisLine(false);                        // 축 기준선 제거
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // 레이블 등록
        xAxis.setTextColor(Color.parseColor("#AAAAAA"));
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);                            // 레이블 간격 1로 고정
        xAxis.setLabelCount(labels.length);

        // 5. Y축(세로축) 설정
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);                     // 수평 격자선 활성화
        leftAxis.setGridColor(Color.parseColor("#E5E5E5"));  // 아주 연한 회색의 격자선
        leftAxis.setGridLineWidth(1f);
        leftAxis.setDrawAxisLine(false);                     // 축 기준선 제거
        leftAxis.setTextColor(Color.parseColor("#AAAAAA"));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);                         // 최소 0%
        leftAxis.setAxisMaximum(100f);                       // 최대 100%
        leftAxis.setLabelCount(5, true);

        // 우측 Y축 비활성화
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // 6. 데이터 적용 및 애니메이션 시작
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.animateY(800);                             // 위로 솟구치는 0.8초 애니메이션
        lineChart.invalidate();                              // 렌더링 갱신
    }

    /**
     * 간단한 텍스트 크기 변환 유틸 메서드
     */
    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_update) {
                // 실제 멤버 변수 데이터를 Intent에 담아 수정 화면으로 이동
                // ActivityResultLauncher를 사용하여 수정 완료 결과를 수신합니다.
                Intent intent = new Intent(HabitDetailActivity.this, HabitUpdateActivity.class);
                intent.putExtra("habit_id", habitId);
                intent.putExtra("habit_title", habitTitle);
                intent.putExtra("start_date", habitStartDate != null ? habitStartDate.replace("-", ".") : "");
                intent.putExtra("end_date", habitEndDate != null ? habitEndDate.replace("-", ".") : "");
                intent.putExtra("duration", habitDuration);
                intent.putExtra("alarm_on", isAlertOn);
                intent.putExtra("active_days", habitActiveDays);
                updateLauncher.launch(intent);

            } else if (v.getId() == R.id.text_delete) {
                // 삭제 전 확인 다이얼로그 표시
                new AlertDialog.Builder(HabitDetailActivity.this)
                        .setTitle("습관 삭제")
                        .setMessage("'" + habitTitle + "' 습관을 삭제하시겠습니까?\n삭제된 기록은 복구할 수 없습니다.")
                        .setPositiveButton("삭제", (dialog, which) -> deleteHabit())
                        .setNegativeButton("취소", null)
                        .show();

            } else if (v.getId() == R.id.btn_play) {
                Toast.makeText(HabitDetailActivity.this, "해당 기능은 준비중입니다.", Toast.LENGTH_SHORT).show();
            } else if (v.getId() == R.id.btn_close) {
                finish();
            }
        }
    };

    /** 서버에 DELETE 요청을 보내고, 성공 시 RESULT_OK를 세팅하여 MainActivity가 새로고침하도록 합니다. */
    private void deleteHabit() {
        if (habitId == -1L) return;

        ApiClient.getApiService().deleteHabit(habitId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HabitDetailActivity.this, "습관이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    // MainActivity에게 목록 새로고침이 필요하다는 신호 전달
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(HabitDetailActivity.this, "삭제에 실패했습니다. (서버 오류)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(HabitDetailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}