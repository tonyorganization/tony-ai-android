package ton_core.repositories.translated_message_repository.languages;

import androidx.lifecycle.LiveData;

import java.util.List;

import ton_core.entities.LanguageEntity;

public interface ILanguageRepository {
    LiveData<List<LanguageEntity>> getLanguages();

    void insertAll(List<LanguageEntity> languages);

    void fetchLanguages();
}
