package ton_core.endpoints;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ton_core.models.SampleRequest;
import ton_core.models.SampleResponse;

public interface SampleEndpoint {
    @POST("/translate")
    Call<SampleResponse> getSampleData(@Body SampleRequest request);
}
