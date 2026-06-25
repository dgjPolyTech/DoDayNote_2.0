package kr.ac.kopo.dodaynote_2.domain;

public class UserRequestDto {
    private String userEmail;
    private String userPw;
    private String userName;

    public UserRequestDto(String userEmail, String userPw, String userName) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userName = userName;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPw() { return userPw; }
    public void setUserPw(String userPw) { this.userPw = userPw; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
