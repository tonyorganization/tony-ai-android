package ton_core.services.chat_service;

import ton_core.models.requests.GenerateTemplateRequest;
import ton_core.models.requests.ToneTransformRequest;
import ton_core.models.responses.FixGrammarResponse;
import ton_core.models.responses.GenerateTemplateResponse;
import ton_core.models.responses.ToneTransformResponse;
import ton_core.services.IOnApiCallback;

public interface IChatService {
    void toneTransform(ToneTransformRequest request, IOnApiCallback<ToneTransformResponse> onResult);
    void generateTemplate(GenerateTemplateRequest request, IOnApiCallback<GenerateTemplateResponse> onResult);
    void fixGrammar(ToneTransformRequest request, IOnApiCallback<FixGrammarResponse> onResult);

}
