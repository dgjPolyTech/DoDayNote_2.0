package kr.ac.kopo.dodaynote_2.network;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/connect")
    Call<Map<String, String>> checkConnection();
}