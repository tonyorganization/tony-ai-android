package ton_core.services.languages;

import java.util.List;

import ton_core.models.LanguageResponse;
import ton_core.services.IOnApiCallback;

public interface ILanguageService {
    void getLanguages(IOnApiCallback<List<LanguageResponse>> onResult);
}
