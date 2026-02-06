package ton_core.models.responses;

import ton_core.models.TranslatedMessageResult;

public class TranslateMessageResponse {
    private TranslatedMessageResult result;

    public TranslatedMessageResult getResult() { return result; }
    public void setResult(TranslatedMessageResult value) { this.result = value; }
}
