package ton_core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ton_core.daos.TranslatedMessageDao;
import ton_core.entities.TranslatedMessageEntity;

@Database(
        entities = {TranslatedMessageEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class TongramDatabase extends RoomDatabase {
    private static volatile TongramDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 2;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static TongramDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TongramDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TongramDatabase.class, "tongram_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract TranslatedMessageDao translatedMessageDao();
}
