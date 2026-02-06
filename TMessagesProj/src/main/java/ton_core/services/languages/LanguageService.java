package ton_core.services.languages;

import org.telegram.messenger.BuildConfig;

import java.util.List;

import ton_core.TonApiClient;
import ton_core.endpoints.LanguageEndpoint;
import ton_core.models.responses.LanguageResponse;
import ton_core.services.BaseService;
import ton_core.services.IOnApiCallback;

public class LanguageService extends BaseService implements ILanguageService {

    LanguageEndpoint languageEndpoint = TonApiClient.getInstance().create(LanguageEndpoint.class);

    @Override
    public void getLanguages(IOnApiCallback<List<LanguageResponse>> onResult) {
        apiCallExecutor.execute(() -> handleWithoutBaseResponse(languageEndpoint.getAllLanguages(BuildConfig.TON_API_KEY), onResult));
    }
}
