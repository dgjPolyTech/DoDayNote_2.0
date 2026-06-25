package kr.ac.kopo.dodaynote_2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.kopo.dodaynote_2.domain.UserRequestDto;
import kr.ac.kopo.dodaynote_2.domain.UserResponseDto;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView textSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textSignup = findViewById(R.id.textSignup);

        btnLogin.setOnClickListener(v -> loginUser());

        textSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRequestDto request = new UserRequestDto(email, password, null);
        ApiService apiService = ApiClient.getApiService();
        apiService.login(request).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 로그인 성공 시 SharedPreferences에 이메일 및 유저명 저장
                    SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userEmail", email);
                    if (response.body().getUserName() != null) {
                        editor.putString("userName", response.body().getUserName());
                    } else {
                        editor.putString("userName", email.split("@")[0]); // fallback
                    }
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "환영합니다!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 실패: 이메일이나 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
