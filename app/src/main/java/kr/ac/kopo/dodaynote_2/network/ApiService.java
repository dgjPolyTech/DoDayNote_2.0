package kr.ac.kopo.dodaynote_2.network;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.dodaynote_2.domain.AiFeedbackResponse;
import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;
import kr.ac.kopo.dodaynote_2.domain.UserRequestDto;
import kr.ac.kopo.dodaynote_2.domain.UserResponseDto;
import kr.ac.kopo.dodaynote_2.domain.YearlyStatDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/users")
    Call<UserResponseDto> signup(@Body UserRequestDto requestDto);

    @POST("/api/users/login")
    Call<UserResponseDto> login(@Body UserRequestDto requestDto);

    @PUT("/api/users/{email}")
    Call<UserResponseDto> updateUser(@Path("email") String email, @Body UserRequestDto requestDto);

    @DELETE("/api/users/{email}")
    Call<Void> deleteUser(@Path("email") String email);

    @POST("/api/habits")
    Call<Habit> createHabit(@Query("userEmail") String userEmail, @Body Habit habit);

    @GET("/api/habits")
    Call<List<Habit>> getAllHabits(@Query("userEmail") String userEmail);

    @GET("/api/habits/completed")
    Call<List<Habit>> getCompletedHabits(@Query("userEmail") String userEmail);

    @POST("/api/habits/{habitId}/records")
    Call<HabitRecord> addProgress(
            @Path("habitId") Long habitId,
            @Body Map<String, Integer> request
    );

    @POST("api/habits/{habitId}/records/toggle")
    Call<HabitRecord> toggleHabitDone(@Path("habitId") Long habitId);

    @GET("api/habits/{habitId}/records")
    Call<List<HabitRecord>> getHabitRecords(@Path("habitId") Long habitId);

    @GET("api/ai/feedback/{habitId}")
    Call<AiFeedbackResponse> getAiFeedback(@Path("habitId") Long habitId);

    // 전체 습관 기반 AI 종합 코멘트
    @GET("api/ai/feedback/all")
    Call<AiFeedbackResponse> getOverallAiFeedback(@Query("userEmail") String userEmail);

    // 연도별/월별 통계 (year=null → 전체 연도별, year=2026 → 해당 연도 월별)
    @GET("api/habits/stats")
    Call<List<YearlyStatDto>> getHabitStats(
            @Query("userEmail") String userEmail, 
            @Query("year") Integer year
    );

    @PUT("/api/habits/{id}")
    Call<Habit> updateHabit(@Path("id") Long id, @Body Habit habit);

    @DELETE("/api/habits/{id}")
    Call<Void> deleteHabit(@Path("id") Long id);
}