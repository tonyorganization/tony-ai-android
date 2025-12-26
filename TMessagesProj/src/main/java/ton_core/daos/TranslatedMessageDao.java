package ton_core.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ton_core.entities.TranslatedMessageEntity;

@Dao
public interface TranslatedMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TranslatedMessageEntity translatedMessage);

    @Query("UPDATE translated_messages SET isShow = :isShow WHERE messageId = :messageId")
    void updateTranslatedState(int messageId, boolean isShow);


    @Query("SELECT * FROM translated_messages WHERE messageId = :messageId")
    LiveData<TranslatedMessageEntity> getTranslatedMessage(int messageId);

    @Query("SELECT * FROM translated_messages WHERE accountId = :accountId ORDER BY messageId DESC LIMIT 50")
    LiveData<List<TranslatedMessageEntity>> getChatMessages(long accountId);
}
