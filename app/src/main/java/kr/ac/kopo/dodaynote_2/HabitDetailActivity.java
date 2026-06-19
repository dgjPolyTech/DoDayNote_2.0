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
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import android.graphics.drawable.GradientDrawable;

import java.util.ArrayList;
import java.util.List;

public class HabitDetailActivity extends AppCompatActivity {

    Button btnUpdate;
    Button btnPlay;

    private String habitTitle = "";
    private int habitDuration = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        // 상단 타이틀 설정
        habitTitle = getIntent().getStringExtra("habit_title");
        if (habitTitle == null) {
            habitTitle = "매일 20분 걷기";
        }
        habitDuration = getIntent().getIntExtra("duration", 20);

        TextView textHabitTitle = findViewById(R.id.text_habit_title);
        textHabitTitle.setText(habitTitle);

        // --- 특정 단어 포인트 컬러 로직 추가 시작 ---
        TextView textHabitCycle = findViewById(R.id.text_habit_cycle);
        String fullText = "매일 20분 씩 반복(알림 설정 ON)";
        SpannableStringBuilder ssb = new SpannableStringBuilder(fullText);
        int pointColor = Color.parseColor("#70C18E"); // 포인트 초록색

        // 1. "매일" 강조 (0~2번 인덱스)
        ssb.setSpan(new ForegroundColorSpan(pointColor), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 2. "20" 강조 (3~5번 인덱스)
        ssb.setSpan(new ForegroundColorSpan(pointColor), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 3. "ON" 강조 (단어 위치를 찾아서 적용)
        int startOn = fullText.indexOf("ON");
        if (startOn != -1) {
            ssb.setSpan(new ForegroundColorSpan(pointColor), startOn, startOn + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), startOn, startOn + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textHabitCycle.setText(ssb);
        // --- 포인트 컬러 로직 추가 끝 ---

        ImageButton btnClose = findViewById(R.id.btn_close);
        btnUpdate = findViewById(R.id.btn_update);
        btnPlay = findViewById(R.id.btn_play);

        btnClose.setOnClickListener(onClickListener);
        btnUpdate.setOnClickListener(onClickListener);
        btnPlay.setOnClickListener(onClickListener);

        // 테스트 차트 초기화 및 데이터 로드
        setupHabitChart();
    }

    /**
     * MPAndroidChart의 LineChart를 활용하여 커스텀 꺾은선 차트를 그리는 메서드입니다.
     */
    private void setupHabitChart() {
        LineChart lineChart = findViewById(R.id.habit_line_chart);
        if (lineChart == null) return;

        // 1. 샘플 데이터 설정 (X: 주차, Y: 달성률)
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 40f)); // 1주차
        entries.add(new Entry(1f, 60f)); // 2주차
        entries.add(new Entry(2f, 92f)); // 3주차 (가장 높음)
        entries.add(new Entry(3f, 75f)); // 4주차

        // X축에 들어갈 레이블 정의
        final String[] quarters = new String[]{"1주차", "2주차", "3주차", "4주차"};

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
        dataSet.setValueTextSize(10f);                       // 값 텍스트 크기 (spToPx 제거하여 크기 정상화)
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
        xAxis.setValueFormatter(new IndexAxisValueFormatter(quarters)); // 레이블 등록
        xAxis.setTextColor(Color.parseColor("#AAAAAA"));
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);                            // 레이블 간격 1로 고정
        xAxis.setLabelCount(4);

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

        // 우측 Y축 비활성화 (보통 양쪽에 있으면 지저분하므로 한쪽에만 표시)
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
            if(v.getId() == R.id.btn_update) {
                // 1. 택배 상자(Intent) 만들기
                Intent intent = new Intent(HabitDetailActivity.this, HabitUpdateActivity.class);

                // 2. 상자에 정보 담기 (key - value)
                intent.putExtra("habit_title", "매일 20분 걷기");
                intent.putExtra("start_date", "2026.05.01");
                intent.putExtra("end_date", "2026.05.31");
                intent.putExtra("duration", 20);
                intent.putExtra("alarm_on", true);

                // 요일 데이터 (모두 선택 상태로 보냄)
                intent.putExtra("mon", true);
                intent.putExtra("tue", true);
                intent.putExtra("wed", true);
                intent.putExtra("thu", true);
                intent.putExtra("fri", true);
                intent.putExtra("sat", true);
                intent.putExtra("sun", true);

                // 3. 출발!
                startActivity(intent);

            } else if(v.getId() == R.id.btn_play) {
                Intent intent = new Intent(HabitDetailActivity.this, HabitPlayActivity.class);
                intent.putExtra("habit_title", habitTitle);
                intent.putExtra("duration", habitDuration);
                startActivity(intent);
            } else if(v.getId() == R.id.btn_close) {
                finish();
            }
        }
    };
}