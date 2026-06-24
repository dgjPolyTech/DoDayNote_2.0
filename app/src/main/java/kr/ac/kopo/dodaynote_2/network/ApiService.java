package kr.ac.kopo.dodaynote_2.network;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.dodaynote_2.domain.AiFeedbackResponse;
import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/api/habits")
    Call<Habit> createHabit(@Body Habit habit);

    @GET("/api/habits")
    Call<List<Habit>> getAllHabits();

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

    @PUT("/api/habits/{id}")
    Call<Habit> updateHabit(@Path("id") Long id, @Body Habit habit);

    @DELETE("/api/habits/{id}")
    Call<Void> deleteHabit(@Path("id") Long id);
}