package ton_core.services.assist_service;

import ton_core.models.SummaryRequest;
import ton_core.models.SummaryResponse;
import ton_core.services.IOnApiCallback;

public interface IAssistService {
    void summarizeChat(SummaryRequest request, IOnApiCallback<SummaryResponse> onResult);
}
