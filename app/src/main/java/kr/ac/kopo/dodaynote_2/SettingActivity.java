package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinnerLanguage = findViewById(R.id.spinner_language);
        String[] languageOptions = {"한국어", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        TextView textUserName = findViewById(R.id.text_user_name);
        TextView textUserEmail = findViewById(R.id.text_user_email);
        ImageView btnEditProfile = findViewById(R.id.btn_edit_profile);
        TextView textLogout = findViewById(R.id.text_logout);

        SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", "EMAIL@~.COM");
        textUserEmail.setText(userEmail);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });

        textLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "USER");
        TextView textUserName = findViewById(R.id.text_user_name);
        if (textUserName != null) {
            textUserName.setText(userName);
        }
    }
}