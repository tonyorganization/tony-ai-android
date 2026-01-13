package ton_core.models;

import java.util.List;

public class TranslatedMessage {
    private String role;
    private List<Object> annotations;
    private String content;

    public String getRole() { return role; }
    public void setRole(String value) { this.role = value; }

    public List<Object> getAnnotations() { return annotations; }
    public void setAnnotations(List<Object> value) { this.annotations = value; }

    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
}
