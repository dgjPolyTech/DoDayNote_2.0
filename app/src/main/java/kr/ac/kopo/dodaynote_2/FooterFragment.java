package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FooterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃 파일을 연결합니다.
        View view = inflater.inflate(R.layout.layout_bottom_nav, container, false);

        // 버튼을 찾습니다.
        Button btnHome = view.findViewById(R.id.btn_home);
        Button btnRecord = view.findViewById(R.id.btn_record);
        Button btnSettings = view.findViewById(R.id.btn_settings);

        // 리스너를 연결합니다.
        btnHome.setOnClickListener(footerListener);
        btnRecord.setOnClickListener(footerListener);
        btnSettings.setOnClickListener(footerListener);

        return view;
    }

    private final View.OnClickListener footerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_home) {
                // 홈으로 이동
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else if (id == R.id.btn_record) {
                // 기록 클릭 시 토스트
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else if (id == R.id.btn_settings) {
                // 설정 클릭 시 토스트
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        }
    };
}