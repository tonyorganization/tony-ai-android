package ton_core.repositories.translated_message_repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import ton_core.daos.TranslatedMessageDao;
import ton_core.database.TongramDatabase;
import ton_core.entities.TranslatedMessageEntity;
import ton_core.models.responses.TranslateMessageResponse;
import ton_core.models.TranslatedChoice;
import ton_core.models.TranslatedMessage;
import ton_core.models.TranslatedMessageResult;
import ton_core.services.IOnApiCallback;
import ton_core.services.translate_service.ITranslateService;
import ton_core.services.translate_service.TranslateService;

public class TranslatedMessageRepository implements ITranslatedMessageRepository {
    private static TranslatedMessageRepository INSTANCE;
    private final TranslatedMessageDao dao;
    private final ITranslateService translateService;

    private TranslatedMessageRepository(TranslatedMessageDao dao) {
        this.dao = dao;
        this.translateService = new TranslateService();
    }

    public static synchronized TranslatedMessageRepository getInstance(Context context) {
        if (INSTANCE == null) {
            TongramDatabase database = TongramDatabase.getDatabase(context);
            INSTANCE = new TranslatedMessageRepository(database.translatedMessageDao());
        }
        return INSTANCE;
    }

    @Override
    public LiveData<TranslatedMessageEntity> getTranslatedMessage(int messageId) {
        return dao.getTranslatedMessage(messageId);
    }

    @Override
    public void insert(TranslatedMessageEntity translatedMessage) {
        TongramDatabase.databaseWriteExecutor.execute(() -> dao.insert(translatedMessage));
    }

    @Override
    public void updateTranslatedState(int messageId, boolean isShow) {
        TongramDatabase.databaseWriteExecutor.execute(() -> dao.updateTranslatedState(messageId, isShow));
    }

    @Override
    public LiveData<List<TranslatedMessageEntity>> getTranslatedMessages(long accountId) {
        return dao.getChatMessages(accountId);
    }

    @Override
    public void translate(String text, String lang, int messageId, long chatId, int accountId, IOnApiCallback<TranslatedMessageEntity> result) {
        translateService.translate(text, lang, new IOnApiCallback<TranslateMessageResponse>() {
            @Override
            public void onSuccess(TranslateMessageResponse data) {
                final TranslatedMessageResult dataResult = data.getResult();
                final List<TranslatedChoice> choices = dataResult.getChoices();
                if (!choices.isEmpty()) {
                    final TranslatedMessage translatedMessage = choices.get(0).getMessage();
                    final TranslatedMessageEntity entity = new TranslatedMessageEntity(messageId, accountId, chatId, translatedMessage.getContent(), lang, true);
                    insert(entity);
                    result.onSuccess(entity);
                }
            }

            @Override
            public void onError(String errorMessage) {
                result.onError(errorMessage);
            }
        });
    }

    @Override
    public void draftTranslate(String text, String lang, IOnApiCallback<TranslateMessageResponse> result) {
        translateService.translate(text, lang, result);
    }
}
