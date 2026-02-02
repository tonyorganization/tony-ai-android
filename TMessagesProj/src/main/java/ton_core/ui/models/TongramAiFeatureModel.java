package ton_core.ui.models;

public class TongramAiFeatureModel {
    public int id;
    public int iconResource;
    public String title;
    public boolean isComingSoon;
    public boolean isSelected;

    public TongramAiFeatureModel(int id, int iconResource, String title, boolean isComingSoon, boolean isSelected) {
        this.id = id;
        this.iconResource = iconResource;
        this.title = title;
        this.isComingSoon = isComingSoon;
        this.isSelected = isSelected;
    }
}
