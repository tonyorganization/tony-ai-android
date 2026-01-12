package ton_core.endpoints;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import ton_core.models.LanguageResponse;

public interface LanguageEndpoint {
    @GET("list-languages")
    Call<List<LanguageResponse>> getAllLanguages(@Header("X-API-Key") String apiKey);
}
