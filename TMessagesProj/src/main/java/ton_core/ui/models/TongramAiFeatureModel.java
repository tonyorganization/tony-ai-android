package ton_core.ui.models;

public class TongramAiFeatureModel {
    public int iconResource;
    public String title;
    public boolean isComingSoon;
    public boolean isSelected;

    public TongramAiFeatureModel(int iconResource, String title, boolean isComingSoon, boolean isSelected) {
        this.iconResource = iconResource;
        this.title = title;
        this.isComingSoon = isComingSoon;
        this.isSelected = isSelected;
    }
}
