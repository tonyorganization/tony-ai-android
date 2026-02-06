package ton_core.repositories.translated_message_repository.assist_repository;

import ton_core.models.requests.SummaryRequest;
import ton_core.models.responses.SummaryResponse;
import ton_core.services.IOnApiCallback;
import ton_core.services.assist_service.AssistService;
import ton_core.services.assist_service.IAssistService;

public class AssistRepository implements IAssistRepository {
    private static AssistRepository INSTANCE;
    private final IAssistService assistService;

    private AssistRepository() {
        this.assistService = new AssistService();
    }

    public static synchronized AssistRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AssistRepository();
        }
        return INSTANCE;
    }

    @Override
    public void summarizeChat(SummaryRequest request, IOnApiCallback<SummaryResponse> onResult) {
        assistService.summarizeChat(request, onResult);
    }
}
