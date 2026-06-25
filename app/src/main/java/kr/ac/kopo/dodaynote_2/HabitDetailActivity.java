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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
        textDelete = findViewById(R.id.text_delete);

        btnClose.setOnClickListener(onClickListener);
        btnUpdate.setOnClickListener(onClickListener);
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
            setupHabitChart(0f);
            return;
        }

        String datePattern = habitStartDate.contains("-") ? "yyyy-MM-dd" : "yyyy.MM.dd";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.getDefault());

        try {
            Date startDate = sdf.parse(habitStartDate);
            if (startDate == null) {
                setupHabitChart(0f);
                return;
            }

            // 오늘 날짜까지만 달성률 계산
            Date today = new Date();
            Date endDate = null;
            if (habitEndDate != null && !habitEndDate.isEmpty()) {
                String endPattern = habitEndDate.contains("-") ? "yyyy-MM-dd" : "yyyy.MM.dd";
                endDate = new SimpleDateFormat(endPattern, Locale.getDefault()).parse(habitEndDate);
            }
            Date effectiveEnd = (endDate != null && endDate.before(today)) ? endDate : today;

            // 시작일이 미래인 경우 0%
            if (startDate.after(today)) {
                setupHabitChart(0f);
                return;
            }

            // 시작일부터 오늘(또는 종료일)까지의 총 일수 계산 (확인 시점에서 오늘까지)
            long totalDays = (effectiveEnd.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000) + 1;
            if (totalDays <= 0) {
                setupHabitChart(0f);
                return;
            }

            int successDays = 0;
            SimpleDateFormat recordSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (HabitRecord record : records) {
                if (!record.isDone() || record.getRecordDate() == null) continue;

                String recordDateStr = record.getRecordDate().length() >= 10
                        ? record.getRecordDate().substring(0, 10).replace(".", "-")
                        : record.getRecordDate().replace(".", "-");

                Date rDate = recordSdf.parse(recordDateStr);
                if (rDate == null) continue;

                // 시작일과 오늘(또는 종료일) 사이의 기록만 인정
                if (!rDate.before(startDate) && !rDate.after(effectiveEnd)) {
                    successDays++;
                }
            }

            float achievementRate = ((float) successDays / totalDays) * 100f;
            if (achievementRate > 100f) achievementRate = 100f;

            setupHabitChart(achievementRate);

        } catch (ParseException e) {
            android.util.Log.e("HabitDetail", "Date parse error: " + e.getMessage());
            setupHabitChart(0f);
        }
    }

    /**
     * MPAndroidChart의 PieChart를 활용하여 도넛 차트를 그리는 메서드입니다.
     */
    private void setupHabitChart(float achievementRate) {
        PieChart pieChart = findViewById(R.id.habit_pie_chart);
        if (pieChart == null) return;

        List<PieEntry> entries = new ArrayList<>();
        
        float failRate = 100f - achievementRate;
        if (achievementRate > 0) {
            entries.add(new PieEntry(achievementRate, "달성"));
        }
        if (failRate > 0) {
            entries.add(new PieEntry(failRate, "미달성"));
        }
        
        // 데이터가 없을 경우 (0% 달성)
        if (entries.isEmpty()) {
            entries.add(new PieEntry(100f, "미달성"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // 색상 설정: 달성(초록), 미달성(회색)
        List<Integer> colors = new ArrayList<>();
        if (achievementRate > 0) colors.add(Color.parseColor("#70C18E"));
        if (failRate > 0 || achievementRate == 0) colors.add(Color.parseColor("#E5E5E5"));
        dataSet.setColors(colors);

        dataSet.setDrawValues(false); // 항목별 텍스트 값 숨김

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // 도넛 차트 설정
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(65f); // 도넛 두께 조절 (구멍 크기)

        // 중앙 텍스트 설정 (달성률 퍼센트)
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText(Math.round(achievementRate) + "%");
        pieChart.setCenterTextSize(24f);
        pieChart.setCenterTextColor(Color.parseColor("#333333"));
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); // 범례 숨김
        pieChart.setDrawEntryLabels(false); // 도넛 안의 달성/미달성 텍스트 숨김
        pieChart.setTouchEnabled(false); // 터치 비활성화 (단순 표시용)

        // 애니메이션 효과
        pieChart.animateY(1000);
        pieChart.invalidate();
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