package ton_core.repositories.translated_message_repository.chat_repository;

import ton_core.models.WritingAssistantRequest;
import ton_core.models.WritingAssistantResponse;
import ton_core.services.IOnApiCallback;
import ton_core.services.chat_service.ChatService;
import ton_core.services.chat_service.IChatService;

public class ChatRepository implements IChatRepository {
    private static ChatRepository INSTANCE;
    private final IChatService chatService;

    private ChatRepository() {
        this.chatService = new ChatService();
    }

    public static synchronized ChatRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatRepository();
        }
        return INSTANCE;
    }

    @Override
    public void writeAssistant(WritingAssistantRequest request, IOnApiCallback<WritingAssistantResponse> onResult) {
        chatService.writeAssistant(request, onResult);
    }
}
