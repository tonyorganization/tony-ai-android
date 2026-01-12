package ton_core.ui.models;

public class TongramLanguageModel {
    public String languageName;
    public String languageCode;
    public String nativeLanguage;
    public boolean isSelected;

    public TongramLanguageModel(String languageName, String languageCode, String nativeLanguage, boolean isSelected) {
        this.languageName = languageName;
        this.languageCode = languageCode;
        this.nativeLanguage = nativeLanguage;
        this.isSelected = isSelected;
    }
}
