package ton_core.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "translated_messages")
public class TranslatedMessageEntity {
    @PrimaryKey
    @ColumnInfo
    public int messageId;

    @ColumnInfo
    public int accountId;

    @ColumnInfo
    public long chatId;

    @ColumnInfo
    @NonNull
    public String translatedMessage;

    @ColumnInfo
    @NonNull
    public String languageCode;

    @ColumnInfo
    public boolean isShow;

    public TranslatedMessageEntity(int messageId, int accountId, long chatId, @NonNull String translatedMessage, @NonNull String languageCode, boolean isShow) {
        this.messageId = messageId;
        this.accountId = accountId;
        this.chatId = chatId;
        this.translatedMessage = translatedMessage;
        this.languageCode = languageCode;
        this.isShow = isShow;
    }
}
