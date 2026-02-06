package ton_core.services.assist_service;

import ton_core.models.requests.SummaryRequest;
import ton_core.models.responses.SummaryResponse;
import ton_core.services.IOnApiCallback;

public interface IAssistService {
    void summarizeChat(SummaryRequest request, IOnApiCallback<SummaryResponse> onResult);
}
