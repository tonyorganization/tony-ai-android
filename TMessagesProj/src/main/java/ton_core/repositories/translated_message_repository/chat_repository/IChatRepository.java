package ton_core.repositories.translated_message_repository.chat_repository;

import ton_core.models.requests.GenerateTemplateRequest;
import ton_core.models.requests.ToneTransformRequest;
import ton_core.models.responses.FixGrammarResponse;
import ton_core.models.responses.GenerateTemplateResponse;
import ton_core.models.responses.ToneTransformResponse;
import ton_core.services.IOnApiCallback;

public interface IChatRepository {
    void toneTransform(ToneTransformRequest request, IOnApiCallback<ToneTransformResponse> onResult);
    void generateTemplate(GenerateTemplateRequest request, IOnApiCallback<GenerateTemplateResponse> onResult);
    void fixGrammar(ToneTransformRequest request, IOnApiCallback<FixGrammarResponse> onResult);
}
