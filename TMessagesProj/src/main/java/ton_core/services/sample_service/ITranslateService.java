package ton_core.services.sample_service;

import ton_core.models.TranslateMessageResponse;
import ton_core.services.IOnApiCallback;

public interface ITranslateService {
    void translate(String text, String lang, IOnApiCallback<TranslateMessageResponse> onResult);
}
