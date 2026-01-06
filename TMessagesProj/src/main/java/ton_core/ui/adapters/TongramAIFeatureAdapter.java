package ton_core.ui.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

import ton_core.ui.models.TongramAiFeatureModel;

public class TongramAIFeatureAdapter extends RecyclerView.Adapter<TongramAIFeatureAdapter.TongramAIFeatureViewHolder> {

    private final List<TongramAiFeatureModel> features;

    public TongramAIFeatureAdapter(List<TongramAiFeatureModel> features) {
        this.features = features;
    }

    @NonNull
    @Override
    public TongramAIFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tongram_ai_item, parent, false);
        return new TongramAIFeatureViewHolder(view);
    }

    private void setItemBackground(View view, boolean isSelected) {
        int strokeWidth = AndroidUtilities.dp(1);
        int[] gradientColors = {
                Color.parseColor("#00D3F0"),
                Color.parseColor("#0034FF")
        };

        int[] defaultColors = {
                Color.parseColor("#474747"),
                Color.parseColor("#474747"),
        };

        GradientDrawable outer = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                isSelected ? gradientColors : defaultColors
        );
        outer.setCornerRadius(AndroidUtilities.dp(20));

        GradientDrawable inner = new GradientDrawable();
        inner.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteShadow));
        inner.setCornerRadius(AndroidUtilities.dp(20) - strokeWidth);

        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{outer, inner}
        );

        layerDrawable.setLayerInset(
                1,
                strokeWidth,
                strokeWidth,
                strokeWidth,
                strokeWidth
        );
        view.setBackground(layerDrawable);
    }

    @Override
    public void onBindViewHolder(@NonNull TongramAIFeatureViewHolder holder, int position) {
        final TongramAiFeatureModel feature = features.get(position);
        holder.title.setText(feature.title);
        holder.aiFeatureImage.setImageResource(feature.iconResource);

        if (feature.isComingSoon) {
            holder.comingSoon.setVisibility(View.VISIBLE);
            holder.comingSoon.setTextColor(Theme.getColor(Theme.key_coming_soon));
            holder.comingSoon.setAlpha(0.5f);
        } else {
            holder.comingSoon.setVisibility(View.GONE);
        }

        if (feature.isSelected) {
            holder.title.setTextColor(Theme.getColor(Theme.key_profile_title));
            holder.title.setTypeface(AndroidUtilities.bold());
            holder.title.setAlpha(1f);
            holder.aiFeatureImage.setAlpha(1f);
        } else {
            GradientDrawable d = (GradientDrawable) holder.itemView.getBackground();
            d.setStroke(1, Theme.getColor(Theme.key_stroke_default));
            holder.title.setTextColor(Theme.getColor(Theme.key_graySectionText));
            holder.title.setTypeface(Typeface.DEFAULT);
            holder.title.setAlpha(0.5f);
            holder.aiFeatureImage.setAlpha(0.5f);
        }

        setItemBackground(holder.itemView, feature.isSelected);
        holder.aiFeatureImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_icon_color), PorterDuff.Mode.SRC_IN));

        holder.itemView.setOnClickListener(view -> {
            if (feature.isComingSoon) {
                return;
            }
            if (feature.isSelected) {
                feature.isSelected = false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    public static class TongramAIFeatureViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView aiFeatureImage;

        TextView comingSoon;

        public TongramAIFeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_ai_feature);
            aiFeatureImage = itemView.findViewById(R.id.iv_ai_feature);
            comingSoon = itemView.findViewById(R.id.tv_coming_soon);
        }
    }
}
