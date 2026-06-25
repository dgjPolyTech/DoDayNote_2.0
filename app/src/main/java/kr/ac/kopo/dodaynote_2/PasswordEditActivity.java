package kr.ac.kopo.dodaynote_2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.kopo.dodaynote_2.domain.UserRequestDto;
import kr.ac.kopo.dodaynote_2.domain.UserResponseDto;
import kr.ac.kopo.dodaynote_2.network.ApiClient;
import kr.ac.kopo.dodaynote_2.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordEditActivity extends AppCompatActivity {

    private EditText editCurrentPassword, editNewPassword, editConfirmNewPassword;
    private Button btnSavePassword;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_edit);

        editCurrentPassword = findViewById(R.id.editCurrentPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmNewPassword = findViewById(R.id.editConfirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        ImageView btnBack = findViewById(R.id.btnBack);

        SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");

        btnBack.setOnClickListener(v -> finish());
        btnSavePassword.setOnClickListener(v -> changePassword());
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        if (!password.matches(".*[0-9].*")) return false;
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~].*")) return false;
        return true;
    }

    private void changePassword() {
        String currentPw = editCurrentPassword.getText().toString().trim();
        String newPw = editNewPassword.getText().toString().trim();
        String confirmPw = editConfirmNewPassword.getText().toString().trim();

        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(newPw)) {
            Toast.makeText(this, "비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPw.equals(confirmPw)) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        UserRequestDto loginRequest = new UserRequestDto(userEmail, currentPw, null);
        
        // 1. 현재 비밀번호 검증 (로그인 API 활용)
        apiService.login(loginRequest).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 2. 비밀번호 변경 API 호출
                    updatePassword(newPw);
                } else {
                    Toast.makeText(PasswordEditActivity.this, "현재 비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
                Toast.makeText(PasswordEditActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(String newPw) {
        ApiService apiService = ApiClient.getApiService();
        UserRequestDto updateRequest = new UserRequestDto(userEmail, newPw, null);

        apiService.updateUser(userEmail, updateRequest).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PasswordEditActivity.this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PasswordEditActivity.this, "비밀번호 변경 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
                Toast.makeText(PasswordEditActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
