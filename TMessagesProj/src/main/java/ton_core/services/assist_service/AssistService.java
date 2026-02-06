package ton_core.services.assist_service;

import org.telegram.messenger.BuildConfig;

import ton_core.TonApiClient;
import ton_core.endpoints.AssistEndpoint;
import ton_core.models.requests.SummaryRequest;
import ton_core.models.responses.SummaryResponse;
import ton_core.services.BaseService;
import ton_core.services.IOnApiCallback;

public class AssistService extends BaseService implements IAssistService {

    AssistEndpoint assistEndpoint = TonApiClient.getInstance().create(AssistEndpoint.class);

    @Override
    public void summarizeChat(SummaryRequest request, IOnApiCallback<SummaryResponse> onResult) {
        apiCallExecutor.execute(() -> handleWithoutBaseResponse(assistEndpoint.summarizeChat(BuildConfig.TON_API_KEY, request), onResult));
    }
}
