package kr.ac.kopo.dodaynote_2.network;

import java.util.List;
import kr.ac.kopo.dodaynote_2.domain.Habit;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/habits")
    Call<Habit> createHabit(@Body Habit habit);

    @GET("/api/habits")
    Call<List<Habit>> getAllHabits();
}