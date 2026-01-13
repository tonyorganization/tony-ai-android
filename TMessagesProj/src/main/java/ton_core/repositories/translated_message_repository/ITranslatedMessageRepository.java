package ton_core.repositories.translated_message_repository;

import androidx.lifecycle.LiveData;
import java.util.List;
import ton_core.entities.TranslatedMessageEntity;
import ton_core.models.TranslateMessageResponse;
import ton_core.services.IOnApiCallback;

public interface ITranslatedMessageRepository {
    LiveData<TranslatedMessageEntity> getTranslatedMessage(int messageId);
    void insert(TranslatedMessageEntity translatedMessage);
    void updateTranslatedState(int messageId, boolean isShow);
    LiveData<List<TranslatedMessageEntity>> getTranslatedMessages(long accountId);
    void translate(String text, String lang, int messageId, long chatId, int accountId, IOnApiCallback<TranslatedMessageEntity> result);
    void draftTranslate(String text, String lang, IOnApiCallback<TranslateMessageResponse> result);
}
