package ton_core.models;

import java.util.List;

public class TranslatedMessageResult {
    private long created;
    private String model;
    private String id;
    private List<TranslatedChoice> choices;
    private boolean fromCache;
    private String object;

    public long getCreated() { return created; }
    public void setCreated(long value) { this.created = value; }

    public String getModel() { return model; }
    public void setModel(String value) { this.model = value; }

    public String getid() { return id; }
    public void setid(String value) { this.id = value; }

    public List<TranslatedChoice> getChoices() { return choices; }
    public void setChoices(List<TranslatedChoice> value) { this.choices = value; }

    public boolean getFromCache() { return fromCache; }
    public void setFromCache(boolean value) { this.fromCache = value; }

    public String getObject() { return object; }
    public void setObject(String value) { this.object = value; }
}
