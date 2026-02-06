package ton_core.services.chat_service;

import org.telegram.messenger.BuildConfig;

import ton_core.TonApiClient;
import ton_core.endpoints.ChatEndpoint;
import ton_core.models.requests.GenerateTemplateRequest;
import ton_core.models.requests.ToneTransformRequest;
import ton_core.models.responses.FixGrammarResponse;
import ton_core.models.responses.GenerateTemplateResponse;
import ton_core.models.responses.ToneTransformResponse;
import ton_core.services.BaseService;
import ton_core.services.IOnApiCallback;

public class ChatService extends BaseService implements IChatService {

    ChatEndpoint chatEndpoint = TonApiClient.getInstance().create(ChatEndpoint.class);
    @Override
    public void toneTransform(ToneTransformRequest request, IOnApiCallback<ToneTransformResponse> onResult) {
        apiCallExecutor.execute(() -> handleWithoutBaseResponse(chatEndpoint.toneTransform(BuildConfig.TON_API_KEY, request), onResult));
    }

    @Override
    public void generateTemplate(GenerateTemplateRequest request, IOnApiCallback<GenerateTemplateResponse> onResult) {
        apiCallExecutor.execute(() -> handleWithoutBaseResponse(chatEndpoint.generateTemplate(BuildConfig.TON_API_KEY, request), onResult));
    }

    @Override
    public void fixGrammar(ToneTransformRequest request, IOnApiCallback<FixGrammarResponse> onResult) {
        apiCallExecutor.execute(() -> handleWithoutBaseResponse(chatEndpoint.fixGrammar(BuildConfig.TON_API_KEY, request), onResult));
    }
}
