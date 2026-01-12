package ton_core.repositories.translated_message_repository.languages;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.android.exoplayer2.util.Log;

import java.util.List;

import ton_core.daos.LanguageDao;
import ton_core.database.TongramDatabase;
import ton_core.entities.LanguageEntity;
import ton_core.models.LanguageResponse;
import ton_core.services.IOnApiCallback;
import ton_core.services.languages.ILanguageService;
import ton_core.services.languages.LanguageService;

public class LanguageRepository implements ILanguageRepository {

    private static LanguageRepository INSTANCE;
    private final LanguageDao dao;
    private final ILanguageService languageService;

    private LanguageRepository(LanguageDao dao) {
        this.dao = dao;
        this.languageService = new LanguageService();
    }

    public static synchronized LanguageRepository getInstance(Context context) {
        if (INSTANCE == null) {
            TongramDatabase database = TongramDatabase.getDatabase(context);
            INSTANCE = new LanguageRepository(database.languageDao());
        }
        return INSTANCE;
    }

    @Override
    public LiveData<List<LanguageEntity>> getLanguages() {
        return dao.getLanguages();
    }

    @Override
    public void insertAll(List<LanguageEntity> languages) {
        TongramDatabase.databaseWriteExecutor.execute(() -> dao.insertAll(languages));
    }

    @Override
    public void fetchLanguages() {
        languageService.getLanguages(new IOnApiCallback<List<LanguageResponse>>() {
            @Override
            public void onSuccess(List<LanguageResponse> data) {
                if (data != null && !data.isEmpty()) {
                    insertAll(data.stream().map(e -> new LanguageEntity(e.code, e.name, e.nativeName)).toList());
                    return;
                }
                onError("LanguageRepository: Fetch languages error");
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("LanguageRepository", errorMessage);
            }
        });
    }
}
