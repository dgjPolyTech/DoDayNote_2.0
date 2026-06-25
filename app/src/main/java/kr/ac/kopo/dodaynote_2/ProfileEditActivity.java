package kr.ac.kopo.dodaynote_2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class ProfileEditActivity extends AppCompatActivity {

    private EditText editNickname;
    private Button btnSaveNickname, btnChangePassword;
    private TextView textDeleteAccount;

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        editNickname = findViewById(R.id.editNickname);
        btnSaveNickname = findViewById(R.id.btnSaveNickname);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        textDeleteAccount = findViewById(R.id.textDeleteAccount);
        ImageView btnBack = findViewById(R.id.btnBack);

        SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("userEmail", "");
        String userName = prefs.getString("userName", "");

        editNickname.setText(userName);

        btnBack.setOnClickListener(v -> finish());
        btnSaveNickname.setOnClickListener(v -> saveNickname());

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, PasswordEditActivity.class);
            startActivity(intent);
        });

        textDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void saveNickname() {
        String newNickname = editNickname.getText().toString().trim();
        if (newNickname.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRequestDto request = new UserRequestDto(userEmail, null, newNickname);
        ApiService apiService = ApiClient.getApiService();
        apiService.updateUser(userEmail, request).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(Call<UserResponseDto> call, Response<UserResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
                    prefs.edit().putString("userName", response.body().getUserName()).apply();

                    Toast.makeText(ProfileEditActivity.this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "닉네임 변경 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDto> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage(Html.fromHtml("<font color='#FF0000'>회원을 탈퇴하시겠습니까?<br/>삭제한 정보는 되돌릴 수 없습니다.</font>", Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton("탈퇴", (dialog, which) -> deleteAccount())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteAccount() {
        ApiService apiService = ApiClient.getApiService();
        apiService.deleteUser(userEmail).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    SharedPreferences prefs = getSharedPreferences("DoDayNotePrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Toast.makeText(ProfileEditActivity.this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileEditActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "탈퇴 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
