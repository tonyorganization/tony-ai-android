package ton_core.models;

public class TranslatedChoice {
    private long index;
    private TranslatedMessage message;

    public long getIndex() { return index; }
    public void setIndex(long value) { this.index = value; }

    public TranslatedMessage getMessage() { return message; }
    public void setMessage(TranslatedMessage value) { this.message = value; }
}
