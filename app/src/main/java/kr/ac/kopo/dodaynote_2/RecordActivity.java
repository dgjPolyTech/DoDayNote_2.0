package kr.ac.kopo.dodaynote_2; // 본인의 패키지명 확인!

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class RecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record); // 만드신 XML 파일명과 일치해야 합니다.

        setupYearSpinner();
    }

    private void setupYearSpinner() {
        Spinner spinnerYear = findViewById(R.id.spinner_year_select);
        String[] years = {"전체 연도", "2026년", "2025년", "2024년"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerYear.setAdapter(adapter);
    }
}