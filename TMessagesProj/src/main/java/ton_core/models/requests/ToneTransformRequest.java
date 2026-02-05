package ton_core.models.requests;

public class ToneTransformRequest {
    public String tone;
    public String draft;


    public ToneTransformRequest(String draft, String tone) {
        this.draft = draft;
        this.tone = tone;
    }

    public ToneTransformRequest(String draft) {
        this.draft = draft;
    }
}