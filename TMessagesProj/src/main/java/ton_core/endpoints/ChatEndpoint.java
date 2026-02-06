package ton_core.endpoints;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ton_core.models.requests.GenerateTemplateRequest;
import ton_core.models.requests.ToneTransformRequest;
import ton_core.models.responses.FixGrammarResponse;
import ton_core.models.responses.GenerateTemplateResponse;
import ton_core.models.responses.ToneTransformResponse;

public interface ChatEndpoint {
    @POST("chat/tone-transform")
    Call<ToneTransformResponse> toneTransform(@Header("X-API-Key") String apiKey, @Body ToneTransformRequest request);

    @POST("chat/template-generate")
    Call<GenerateTemplateResponse> generateTemplate(@Header("X-API-Key") String apiKey, @Body GenerateTemplateRequest request);

    @POST("chat/fix-grammar")
    Call<FixGrammarResponse> fixGrammar(@Header("X-API-Key") String apiKey, @Body ToneTransformRequest request);
}
