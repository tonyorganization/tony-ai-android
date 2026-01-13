package ton_core.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "language")
public class LanguageEntity {
    @PrimaryKey
    @ColumnInfo
    @NonNull
    public String code;

    @NonNull
    @ColumnInfo
    public String name;

    @NonNull
    @ColumnInfo
    public String nativeName;

    public LanguageEntity(@NonNull String code, @NonNull String name, @NonNull String nativeName) {
        this.code = code;
        this.name = name;
        this.nativeName = nativeName;
    }
}
