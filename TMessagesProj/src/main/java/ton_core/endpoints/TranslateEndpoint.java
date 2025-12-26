package ton_core.endpoints;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ton_core.models.TranslateMessageResponse;
import ton_core.models.TranslateRequest;

public interface TranslateEndpoint {
    @POST("translate-proxy")
    Call<TranslateMessageResponse> translate(@Body TranslateRequest request, @Header("X-API-Key") String apiKey);
}
