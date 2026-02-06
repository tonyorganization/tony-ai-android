package ton_core.endpoints;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ton_core.models.requests.SummaryRequest;
import ton_core.models.responses.SummaryResponse;

public interface AssistEndpoint {
    @POST("assist/chat-summary")
    Call<SummaryResponse> summarizeChat(@Header("X-API-Key") String apiKey, @Body SummaryRequest request);
}
