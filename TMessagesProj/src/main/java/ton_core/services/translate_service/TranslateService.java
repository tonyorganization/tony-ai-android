package ton_core.services.translate_service;

import org.telegram.messenger.BuildConfig;
import ton_core.TonApiClient;
import ton_core.endpoints.TranslateEndpoint;
import ton_core.models.responses.TranslateMessageResponse;
import ton_core.models.requests.TranslateRequest;
import ton_core.services.BaseService;
import ton_core.services.IOnApiCallback;

public class TranslateService extends BaseService implements ITranslateService {

    TranslateEndpoint translateEndpoint = TonApiClient.getInstance().create(TranslateEndpoint.class);

    @Override
    public void translate(String text, String lang, IOnApiCallback<TranslateMessageResponse> onResult) {
        apiCallExecutor.execute(() -> handleWithoutBaseResponse(translateEndpoint.translate(new TranslateRequest(text, lang), BuildConfig.TON_API_KEY), onResult));
    }
}
