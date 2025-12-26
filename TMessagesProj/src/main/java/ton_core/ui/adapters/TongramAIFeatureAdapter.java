package ton_core.ui.adapters;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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

    @Override
    public void onBindViewHolder(@NonNull TongramAIFeatureViewHolder holder, int position) {
        final TongramAiFeatureModel feature = features.get(position);
        holder.title.setText(feature.title);
        holder.aiFeatureImage.setImageResource(feature.iconResource);

        if (feature.isComingSoon) {
            holder.comingSoon.setVisibility(View.VISIBLE);
            holder.comingSoon.setTextColor(Theme.getColor(Theme.key_coming_soon));
        } else {
            holder.comingSoon.setVisibility(View.GONE);
        }

        if (feature.isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.rectangle_corner);
            holder.title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            holder.title.setTypeface(AndroidUtilities.bold());

            holder.aiFeatureImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_button_enable), PorterDuff.Mode.SRC_IN));

        } else {
            holder.itemView.setBackgroundResource(R.drawable.rectangle_corner_default);
            GradientDrawable d = (GradientDrawable) holder.itemView.getBackground();
            d.setStroke(1, Theme.getColor(Theme.key_stroke_default));
            holder.title.setTextColor(Theme.getColor(Theme.key_graySectionText));
            holder.title.setTypeface(Typeface.DEFAULT);

            holder.aiFeatureImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_button_disable), PorterDuff.Mode.SRC_IN));
        }

        Drawable background = holder.itemView.getBackground();
        if (background instanceof android.graphics.drawable.GradientDrawable) {
            android.graphics.drawable.GradientDrawable shape = (android.graphics.drawable.GradientDrawable) background;
            int themeColor = Theme.getColor(Theme.key_windowBackgroundWhite);

            shape.setColor(themeColor);
        }

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
