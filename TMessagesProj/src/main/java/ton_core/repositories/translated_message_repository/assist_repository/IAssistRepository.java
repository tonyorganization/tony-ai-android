package ton_core.repositories.translated_message_repository.assist_repository;

import ton_core.models.SummaryRequest;
import ton_core.models.SummaryResponse;
import ton_core.services.IOnApiCallback;

public interface IAssistRepository {
    void summarizeChat(SummaryRequest request, IOnApiCallback<SummaryResponse> onResult);
}
