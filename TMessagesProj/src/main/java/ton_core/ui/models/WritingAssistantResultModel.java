package ton_core.ui.models;

public class WritingAssistantResultModel {
    public int id;
    public String message;
    public boolean isSelected;

    public WritingAssistantResultModel(int id, String message, boolean isSelected) {
        this.id = id;
        this.message = message;
        this.isSelected = isSelected;
    }
}
