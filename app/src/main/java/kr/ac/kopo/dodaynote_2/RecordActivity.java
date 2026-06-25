package kr.ac.kopo.dodaynote_2;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kopo.dodaynote_2.domain.AiFeedbackResponse;
import kr.ac.kopo.dodaynote_2.domain.MonthlyStatDto;
import kr.ac.kopo.dodaynote_2.domain.YearlyStatDto;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordActivity extends AppCompatActivity {

    private Spinner spinnerYear;
    private BarChart barChart;
    private LineChart lineChart;
    private TextView textTotalHabits, textBestYear, textBestHabit, textTotalRate, textAiComment;
    private ProgressBar progressLoading;
    private CardView cardAiComment;

    private ApiService apiService;

    // ── 액티비티 인스턴스 내 AI 피드백 캐시 ───────────────────────────────────
    // 이 변수가 null이 아닌 경우: 이미 이번 액티비티 생명주기 내에서 GPT를 호출한 적이 있음
    // → GPT 재호출 없이 캐시된 값을 바로 표시하여 API 비용 및 과부하를 방지
    private String cachedAiFeedback = null;

    // 서버에서 받아온 전체 연도 통계 (Spinner 항목 구성 및 차트 렌더링에 사용)
    private List<YearlyStatDto> allYearlyStats = new ArrayList<>();

    // 현재 Spinner에서 선택된 연도 (null = "전체 연도")
    private Integer selectedYear = null;

    // Spinner 선택 변경 이벤트가 최초 세팅 시에도 발생하는 문제를 방지하는 플래그
    private boolean isSpinnerInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initViews();
        apiService = ApiClient.getApiService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 통계 데이터는 매번 갱신 (가볍고 습관 변경사항 반영 필요)
        loadStatsData(null);

        // AI 피드백은 캐시가 없을 때만 호출 → 불필요한 GPT 재호출 방지
        if (cachedAiFeedback == null) {
            loadAiFeedbackAsync();
        } else {
            showAiFeedback(cachedAiFeedback);
        }
    }

    // ── 뷰 초기화 ────────────────────────────────────────────────────────────

    private void initViews() {
        spinnerYear = findViewById(R.id.spinner_year_select);
        barChart = findViewById(R.id.bar_chart_habits);
        lineChart = findViewById(R.id.line_chart_habits);
        textTotalHabits = findViewById(R.id.text_total_habits);
        textBestYear = findViewById(R.id.text_best_year);
        textBestHabit = findViewById(R.id.text_best_habit);
        textTotalRate = findViewById(R.id.text_total_rate);
        textAiComment = findViewById(R.id.text_ai_comment);
        progressLoading = findViewById(R.id.progress_loading);
        cardAiComment = findViewById(R.id.card_ai_comment);

        setupBarChart();
        setupLineChart();
    }

    // ── 차트 기본 스타일 설정 ─────────────────────────────────────────────────

    private void setupBarChart() {
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setExtraBottomOffset(8f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF555555);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFEEEEEE);
        leftAxis.setTextColor(0xFF555555);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        barChart.getAxisRight().setEnabled(false);
    }

    private void setupLineChart() {
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setExtraBottomOffset(8f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF555555);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFEEEEEE);
        leftAxis.setTextColor(0xFF555555);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        lineChart.getAxisRight().setEnabled(false);
    }

    // ── 통계 데이터 로드 (API 호출) ────────────────────────────────────────────

    private void loadStatsData(Integer year) {
        android.content.SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", "");
        String userName = prefs.getString("userName", "유저");

        textTotalHabits.setText(userName + "의 리포트");

        apiService.getHabitStats(userEmail, year).enqueue(new Callback<List<YearlyStatDto>>() {
            @Override
            public void onResponse(Call<List<YearlyStatDto>> call,
                                   Response<List<YearlyStatDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YearlyStatDto> stats = response.body();

                    if (year == null) {
                        // 전체 연도 모드: Spinner 항목 동적 구성 후 차트 렌더링
                        allYearlyStats = stats;
                        setupSpinner(stats);
                        renderYearlyChart(stats);
                        updateSummaryForYearly(stats);
                    } else {
                        // 특정 연도 모드: 월별 차트 렌더링
                        if (!stats.isEmpty()) {
                            YearlyStatDto dto = stats.get(0);
                            renderMonthlyChart(dto);
                            updateSummaryForSpecificYear(dto, year);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<YearlyStatDto>> call, Throwable t) {
                textBestYear.setText("✨ 통계 데이터를 불러올 수 없습니다.");
            }
        });
    }

    // ── Spinner 동적 구성 ─────────────────────────────────────────────────────

    private void setupSpinner(List<YearlyStatDto> stats) {
        List<String> items = new ArrayList<>();
        items.add("전체 연도");
        // 서버 응답에서 연도를 추출하여 동적으로 추가 (이미 TreeMap으로 정렬 반환)
        for (YearlyStatDto dto : stats) {
            items.add(dto.getYear() + "년");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        isSpinnerInitialized = false; // 어댑터 교체 시 이벤트 억제
        spinnerYear.setAdapter(adapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 최초 세팅 시 자동 발생하는 이벤트는 무시
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    return;
                }

                if (position == 0) {
                    // "전체 연도" 선택 → 이미 보유한 데이터로 차트 갱신
                    selectedYear = null;
                    renderYearlyChart(allYearlyStats);
                    updateSummaryForYearly(allYearlyStats);
                } else {
                    // 특정 연도 선택 → 해당 연도 월별 통계 API 호출
                    selectedYear = allYearlyStats.get(position - 1).getYear();
                    loadStatsData(selectedYear);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── 차트 렌더링: 전체 연도별 달성률 ─────────────────────────────────────────

    private void renderYearlyChart(List<YearlyStatDto> stats) {
        barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            YearlyStatDto dto = stats.get(i);
            entries.add(new BarEntry(i, (float) dto.getAchievementRate()));
            labels.add(String.valueOf(dto.getYear()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "연도별 달성률");
        dataSet.setColor(0xFF3DAA5C);
        dataSet.setValueTextColor(0xFF334455);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0 ? "" : (int) value + "%";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.setData(barData);
        barChart.animateY(600);
        barChart.invalidate();
    }

    // ── 차트 렌더링: 특정 연도의 월별 달성률 ─────────────────────────────────────

    private void renderMonthlyChart(YearlyStatDto dto) {
        if (dto.getMonthly() == null) return;
        
        lineChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (MonthlyStatDto monthly : dto.getMonthly()) {
            entries.add(new Entry(monthly.getMonth() - 1, (float) monthly.getAchievementRate()));
            labels.add(monthly.getMonth() + "월");
        }

        LineDataSet dataSet = new LineDataSet(entries, dto.getYear() + "년 월별 달성률");
        dataSet.setColor(0xFF3DAA5C);
        dataSet.setCircleColor(0xFF3DAA5C);
        dataSet.setCircleHoleColor(android.graphics.Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setValueTextColor(0xFF334455);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0 ? "" : (int) value + "%";
            }
        });

        LineData lineData = new LineData(dataSet);

        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setLabelCount(labels.size());
        lineChart.setData(lineData);
        lineChart.animateY(600);
        lineChart.invalidate();
    }

    // ── 요약 텍스트 갱신: 전체 연도 모드 ───────────────────────────────────────

    private void updateSummaryForYearly(List<YearlyStatDto> stats) {
        if (stats == null || stats.isEmpty()) {
            textBestYear.setText("아직 기록된 데이터가 없습니다.");
            textBestHabit.setText("");
            textTotalRate.setText("");
            return;
        }

        // 달성률이 가장 높은 연도 탐색
        YearlyStatDto best = stats.stream()
                .max((a, b) -> Double.compare(a.getAchievementRate(), b.getAchievementRate()))
                .orElse(stats.get(0));

        int totalRecords = stats.stream().mapToInt(YearlyStatDto::getTotalRecords).sum();
        int totalSuccess = stats.stream().mapToInt(YearlyStatDto::getSuccessCount).sum();
        double overallRate = totalRecords > 0
                ? Math.round(totalSuccess * 100.0 / totalRecords * 10.0) / 10.0 : 0;

        textBestYear.setText("가장 평균 달성률이 높았던 해는 " + best.getYear() + "년 ("
                + best.getAchievementRate() + "%)");

        String bestHabit = best.getBestHabitTitle();
        textBestHabit.setText((bestHabit != null && !bestHabit.isEmpty())
                ? "최장 기간 달성 습관: " + bestHabit : "");

        textTotalRate.setText("전체 달성률: " + overallRate + "%"
                + " (" + totalSuccess + "/" + totalRecords + "일)");
    }

    // ── 요약 텍스트 갱신: 특정 연도 모드 ──────────────────────────────────────

    private void updateSummaryForSpecificYear(YearlyStatDto dto, int year) {
        textBestYear.setText(year + "년 전체 달성률: " + dto.getAchievementRate() + "%");

        String bestHabit = dto.getBestHabitTitle();
        textBestHabit.setText((bestHabit != null && !bestHabit.isEmpty())
                ? "최장 기간 달성 습관: " + bestHabit : "");

        textTotalRate.setText(dto.getSuccessCount() + "일 달성 / "
                + dto.getTotalRecords() + "일 기록");
    }

    // ── AI 피드백 비동기 로드 ─────────────────────────────────────────────────
    // Retrofit enqueue()는 내부적으로 백그라운드 스레드에서 HTTP 요청을 수행하고
    // onResponse/onFailure는 메인(UI) 스레드에서 실행되므로 UI 조작이 안전함

    private void loadAiFeedbackAsync() {
        android.content.SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", "");

        // 로딩 시작: ProgressBar 표시, AI 카드뷰 숨김
        progressLoading.setVisibility(View.VISIBLE);
        cardAiComment.setVisibility(View.GONE);

        apiService.getOverallAiFeedback(userEmail).enqueue(new Callback<AiFeedbackResponse>() {
            @Override
            public void onResponse(Call<AiFeedbackResponse> call,
                                   Response<AiFeedbackResponse> response) {
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    String feedback = response.body().getFeedback();
                    cachedAiFeedback = feedback; // 캐시에 저장
                    showAiFeedback(feedback);
                } else {
                    showAiFeedback("AI 피드백을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<AiFeedbackResponse> call, Throwable t) {
                progressLoading.setVisibility(View.GONE);
                showAiFeedback("네트워크 오류로 AI 피드백을 불러올 수 없습니다.");
            }
        });
    }

    // AI 피드백 텍스트 파싱 및 색상 적용
    private void showAiFeedback(String feedback) {
        android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder();
        
        int positiveIndex = feedback.indexOf("[긍정]");
        int negativeIndex = feedback.indexOf("[부정]");
        
        if (positiveIndex != -1 && negativeIndex != -1) {
            String beforePos = feedback.substring(0, positiveIndex);
            String posSection = feedback.substring(positiveIndex, negativeIndex);
            String negSection = feedback.substring(negativeIndex);
            
            builder.append(beforePos);
            
            int posStart = builder.length();
            builder.append(posSection);
            builder.setSpan(new android.text.style.ForegroundColorSpan(0xFF3DAA5C), posStart, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            int negStart = builder.length();
            builder.append(negSection);
            builder.setSpan(new android.text.style.ForegroundColorSpan(0xFFE53935), negStart, builder.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            builder.append(feedback);
        }

        textAiComment.setText(builder);
        cardAiComment.setAlpha(0f);
        cardAiComment.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(cardAiComment, "alpha", 0f, 1f)
                .setDuration(400)
                .start();
    }
}