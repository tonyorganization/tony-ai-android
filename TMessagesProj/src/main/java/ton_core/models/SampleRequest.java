package ton_core.models;

import java.util.ArrayList;
import java.util.List;


public class SampleRequest {
    public String target_language;
    public String text;
    public List<String> glossary;
    public String model;

    public SampleRequest(String text) {
        this.text = text;
        this.target_language = "English";
        this.glossary = new ArrayList<>();
        this.model = "gpt-4o-mini";
    }
}
