package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 2초(2000ms) 딜레이 후 메인 화면으로 이동
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                android.content.SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
                String userEmail = prefs.getString("userEmail", null);

                Intent intent;
                if (userEmail != null && !userEmail.isEmpty()) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                
                startActivity(intent);
                // 스플래시 액티비티를 종료하여 뒤로 가기 시 다시 보이지 않도록 함
                finish();
            }
        }, 2000);
    }
}
