package ton_core.models;

public class WritingAssistantRequest {
    public String text;
    public String tone;

    public WritingAssistantRequest(String text, String tone) {
        this.text = text;
        this.tone = tone;
    }
}