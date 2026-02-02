package ton_core.repositories.translated_message_repository.chat_repository;

import ton_core.models.WritingAssistantRequest;
import ton_core.models.WritingAssistantResponse;
import ton_core.services.IOnApiCallback;

public interface IChatRepository {
    void writeAssistant(WritingAssistantRequest request, IOnApiCallback<WritingAssistantResponse> onResult);
}
