package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.ac.kopo.dodaynote_2.domain.AiFeedbackResponse;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitListDetailActivity extends AppCompatActivity {

    private ImageButton btn_close;
    private TextView textHabitTitle;
    private TextView textHabitDate;
    private TextView textHabitCycle;
    private TextView textAiFeedback;
    private ApiService apiService;
    private Long habitId;
    private String habitDateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list_detail);

        btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener(v -> finish());

        textHabitTitle = findViewById(R.id.text_habit_title);
        textHabitDate = findViewById(R.id.text_habit_date);
        textHabitCycle = findViewById(R.id.text_habit_cycle);
        textAiFeedback = findViewById(R.id.text_ai_feedback);

        apiService = ApiClient.getApiService();

        // Hide cycle for completed habits (optional, or we can fetch it)
        textHabitCycle.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) {
            habitId = intent.getLongExtra("habit_id", -1);
            String title = intent.getStringExtra("habit_title");
            habitDateStr = intent.getStringExtra("habit_date");

            if (title != null) textHabitTitle.setText(title);
            if (habitDateStr != null) textHabitDate.setText(habitDateStr);

            if (habitId != -1) {
                loadAiFeedback(habitId);
                loadHabitRecords(habitId);
            } else {
                textAiFeedback.setText("잘못된 접근입니다.");
            }
        }
    }

    private void loadAiFeedback(Long id) {
        apiService.getAiFeedback(id).enqueue(new Callback<AiFeedbackResponse>() {
            @Override
            public void onResponse(Call<AiFeedbackResponse> call, Response<AiFeedbackResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    textAiFeedback.setText(response.body().getFeedback());
                } else {
                    textAiFeedback.setText("AI 피드백을 생성할 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<AiFeedbackResponse> call, Throwable t) {
                Log.e("HabitListDetail", "AI 피드백 호출 실패", t);
                textAiFeedback.setText("네트워크 오류로 AI 피드백을 가져올 수 없습니다.");
            }
        });
    }

    private void loadHabitRecords(Long id) {
        apiService.getHabitRecords(id).enqueue(new Callback<List<HabitRecord>>() {
            @Override
            public void onResponse(Call<List<HabitRecord>> call, Response<List<HabitRecord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processRecordsAndDrawChart(response.body());
                } else {
                    setupHabitChart(0f);
                }
            }

            @Override
            public void onFailure(Call<List<HabitRecord>> call, Throwable t) {
                Log.e("HabitListDetail", "기록 호출 실패", t);
                setupHabitChart(0f);
            }
        });
    }

    private void processRecordsAndDrawChart(List<HabitRecord> records) {
        if (habitDateStr == null || !habitDateStr.contains("~")) {
            setupHabitChart(0f);
            return;
        }

        String[] dates = habitDateStr.split("~");
        String habitStartDate = dates[0].trim();
        String habitEndDate = dates.length > 1 ? dates[1].trim() : "";

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
            if (!habitEndDate.isEmpty()) {
                String endPattern = habitEndDate.contains("-") ? "yyyy-MM-dd" : "yyyy.MM.dd";
                endDate = new SimpleDateFormat(endPattern, Locale.getDefault()).parse(habitEndDate);
            }
            Date effectiveEnd = (endDate != null && endDate.before(today)) ? endDate : today;

            // 시작일이 미래인 경우 0%
            if (startDate.after(today)) {
                setupHabitChart(0f);
                return;
            }

            // 시작일부터 오늘(또는 종료일)까지의 총 일수 계산
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

                if (!rDate.before(startDate) && !rDate.after(effectiveEnd)) {
                    successDays++;
                }
            }

            float achievementRate = ((float) successDays / totalDays) * 100f;
            if (achievementRate > 100f) achievementRate = 100f;

            setupHabitChart(achievementRate);

        } catch (ParseException e) {
            Log.e("HabitListDetail", "Date parse error: " + e.getMessage());
            setupHabitChart(0f);
        }
    }

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
        
        if (entries.isEmpty()) {
            entries.add(new PieEntry(100f, "미달성"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        List<Integer> colors = new ArrayList<>();
        if (achievementRate > 0) colors.add(Color.parseColor("#70C18E"));
        if (failRate > 0 || achievementRate == 0) colors.add(Color.parseColor("#E5E5E5"));
        dataSet.setColors(colors);

        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(65f);

        pieChart.setDrawCenterText(true);
        pieChart.setCenterText(Math.round(achievementRate) + "%");
        pieChart.setCenterTextSize(24f);
        pieChart.setCenterTextColor(Color.parseColor("#333333"));
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false); // 도넛 안의 텍스트 숨김
        pieChart.setTouchEnabled(false);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}