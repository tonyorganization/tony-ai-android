package ton_core.models.requests;

public class GenerateTemplateRequest {
    private String template_type;
    public String prompt;

    public GenerateTemplateRequest(String template, String prompt) {
        this.template_type = template;
        this.prompt = prompt;
    }
}
