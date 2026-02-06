package ton_core.models.requests;

import java.util.ArrayList;
import java.util.List;

public class TranslateRequest {
    public String target_language;
    public String text;
    public List<String> glossary;
    public String model;

    public TranslateRequest(String text, String targetLang) {
        this.text = text;
        this.target_language = targetLang;
        this.glossary = new ArrayList<>();
        this.model = "gpt-4.1-mini";
    }
}
