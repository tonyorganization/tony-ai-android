package ton_core.services.chat_service;

import ton_core.models.WritingAssistantRequest;
import ton_core.models.WritingAssistantResponse;
import ton_core.services.IOnApiCallback;

public interface IChatService {
    void writeAssistant(WritingAssistantRequest request, IOnApiCallback<WritingAssistantResponse> onResult);

}
