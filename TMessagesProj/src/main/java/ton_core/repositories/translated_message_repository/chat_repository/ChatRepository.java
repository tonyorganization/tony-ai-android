package ton_core.repositories.translated_message_repository.chat_repository;

import ton_core.models.requests.GenerateTemplateRequest;
import ton_core.models.requests.ToneTransformRequest;
import ton_core.models.responses.FixGrammarResponse;
import ton_core.models.responses.GenerateTemplateResponse;
import ton_core.models.responses.ToneTransformResponse;
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
    public void toneTransform(ToneTransformRequest request, IOnApiCallback<ToneTransformResponse> onResult) {
        chatService.toneTransform(request, onResult);
    }

    @Override
    public void generateTemplate(GenerateTemplateRequest request, IOnApiCallback<GenerateTemplateResponse> onResult) {
        chatService.generateTemplate(request, onResult);
    }

    @Override
    public void fixGrammar(ToneTransformRequest request, IOnApiCallback<FixGrammarResponse> onResult) {
        chatService.fixGrammar(request, onResult);
    }
}
