package kr.ac.kopo.dodaynote_2;

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

public class SignupActivity extends AppCompatActivity {

    private EditText editSignupEmail, editSignupPassword, editSignupName;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editSignupEmail = findViewById(R.id.editSignupEmail);
        editSignupPassword = findViewById(R.id.editSignupPassword);
        editSignupName = findViewById(R.id.editSignupName);
        btnSignup = findViewById(R.id.btnSignup);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSignup.setOnClickListener(v -> signupUser());
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        if (!password.matches(".*[0-9].*")) return false;
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~].*")) return false;
        return true;
    }

    private void signupUser() {
        String email = editSignupEmail.getText().toString().trim();
        String password = editSignupPassword.getText().toString().trim();
        String name = editSignupName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRequestDto request = new UserRequestDto(email, password, name);
        ApiService apiService = ApiClient.getApiService();
        apiService.signup(request).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "회원가입 성공! 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    finish(); // 회원가입 후 로그인 화면으로 복귀
                } else {
                    Toast.makeText(SignupActivity.this, "회원가입 실패: 이미 존재하는 이메일일 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
