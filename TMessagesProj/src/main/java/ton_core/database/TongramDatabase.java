package ton_core.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ton_core.daos.LanguageDao;
import ton_core.daos.TranslatedMessageDao;
import ton_core.entities.LanguageEntity;
import ton_core.entities.TranslatedMessageEntity;

@Database(
        entities = {TranslatedMessageEntity.class, LanguageEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class TongramDatabase extends RoomDatabase {
    private static volatile TongramDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 2;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `language` (" + "`code` TEXT NOT NULL, " + "`name` TEXT NOT NULL,  " + "`nativeName` TEXT NOT NULL, " + "PRIMARY KEY(`code`))");
        }
    };

    public static TongramDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TongramDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TongramDatabase.class, "tongram_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract TranslatedMessageDao translatedMessageDao();
    public abstract LanguageDao languageDao();
}
