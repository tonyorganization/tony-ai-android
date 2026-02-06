package ton_core.models.requests;

import java.util.List;

public class SummaryRequest {
    public List<String> content;

    public SummaryRequest(List<String> message) {
        this.content = message;
    }
}
