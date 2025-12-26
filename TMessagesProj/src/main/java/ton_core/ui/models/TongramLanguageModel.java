package ton_core.ui.models;

public class TongramLanguageModel {
    public String languageName;
    public String languageCode;
    public boolean isSelected;

    public TongramLanguageModel(String languageName, String languageCode, boolean isSelected) {
        this.languageName = languageName;
        this.languageCode = languageCode;
        this.isSelected = isSelected;
    }
}
