package ton_core.services.translate_service;

import ton_core.models.responses.TranslateMessageResponse;
import ton_core.services.IOnApiCallback;

public interface ITranslateService {
    void translate(String text, String lang, IOnApiCallback<TranslateMessageResponse> onResult);
}
