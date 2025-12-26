package ton_core.models;

public class TranslateMessageResponse {
    private TranslatedMessageResult result;
    private String endpointUsed;

    public TranslatedMessageResult getResult() { return result; }
    public void setResult(TranslatedMessageResult value) { this.result = value; }

    public String getEndpointUsed() { return endpointUsed; }
    public void setEndpointUsed(String value) { this.endpointUsed = value; }
}

class Usage {
    private long completionTokens;
    private long promptTokens;
    private CompletionTokensDetails completionTokensDetails;
    private PromptTokensDetails promptTokensDetails;
    private long totalTokens;

    public long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(long value) { this.completionTokens = value; }

    public long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(long value) { this.promptTokens = value; }

    public CompletionTokensDetails getCompletionTokensDetails() { return completionTokensDetails; }
    public void setCompletionTokensDetails(CompletionTokensDetails value) { this.completionTokensDetails = value; }

    public PromptTokensDetails getPromptTokensDetails() { return promptTokensDetails; }
    public void setPromptTokensDetails(PromptTokensDetails value) { this.promptTokensDetails = value; }

    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long value) { this.totalTokens = value; }
}

class CompletionTokensDetails {
    private long acceptedPredictionTokens;
    private long audioTokens;
    private long reasoningTokens;
    private long rejectedPredictionTokens;

    public long getAcceptedPredictionTokens() { return acceptedPredictionTokens; }
    public void setAcceptedPredictionTokens(long value) { this.acceptedPredictionTokens = value; }

    public long getAudioTokens() { return audioTokens; }
    public void setAudioTokens(long value) { this.audioTokens = value; }

    public long getReasoningTokens() { return reasoningTokens; }
    public void setReasoningTokens(long value) { this.reasoningTokens = value; }

    public long getRejectedPredictionTokens() { return rejectedPredictionTokens; }
    public void setRejectedPredictionTokens(long value) { this.rejectedPredictionTokens = value; }
}

class PromptTokensDetails {
    private long audioTokens;
    private long cachedTokens;

    public long getAudioTokens() { return audioTokens; }
    public void setAudioTokens(long value) { this.audioTokens = value; }

    public long getCachedTokens() { return cachedTokens; }
    public void setCachedTokens(long value) { this.cachedTokens = value; }
}
