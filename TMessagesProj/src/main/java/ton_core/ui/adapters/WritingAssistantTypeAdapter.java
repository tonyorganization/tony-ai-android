package ton_core.ui.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

import ton_core.ui.models.WritingAssistantTypeModel;

public class WritingAssistantTypeAdapter extends RecyclerView.Adapter<WritingAssistantTypeAdapter.WritingAssistantTypeViewHolder> {

    private final List<WritingAssistantTypeModel> list;
    private final IWritingAssistantDelegate delegate;

    public interface IWritingAssistantDelegate {
        void onWritingAssistantTypeSelected(WritingAssistantTypeModel type);
    }

    public WritingAssistantTypeAdapter(List<WritingAssistantTypeModel> list, IWritingAssistantDelegate delegate) {
        this.delegate = delegate;
        this.list = list;
    }

    @NonNull
    @Override
    public WritingAssistantTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.writing_assistant_type_item_layout, parent, false);
        return new WritingAssistantTypeAdapter.WritingAssistantTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WritingAssistantTypeViewHolder holder, int position) {
        final WritingAssistantTypeModel model = list.get(position);
        holder.tvWritingType.setText(model.title);
        holder.tvWritingType.setTextColor(Theme.getColor(Theme.key_profile_title));
        setItemBackground(holder.tvWritingType, model.isSelected);
        if (model.isSelected) {
            holder.tvWritingType.setTypeface(AndroidUtilities.bold());
        } else {
            holder.tvWritingType.setTypeface(Typeface.DEFAULT);
        }

        holder.itemView.setOnClickListener(view -> {
            if (model.isSelected) return;

            if (delegate != null) {
                delegate.onWritingAssistantTypeSelected(model);
            }
        });
    }

    private void setItemBackground(View view, boolean isSelected) {
        int strokeWidth = AndroidUtilities.dp(1);
        int[] gradientColors = {
                Color.parseColor("#2D374F"),
                Color.parseColor("#060911")
        };

        int[] defaultColors = {
                Theme.getColor(Theme.key_input_background),
                Theme.getColor(Theme.key_input_background),
        };

        GradientDrawable outer = new GradientDrawable();
        outer.setColor(Theme.getColor(isSelected ? Theme.key_icon_color : Theme.key_input_background));
        outer.setCornerRadius(AndroidUtilities.dp(20));

        GradientDrawable inner = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                isSelected ? gradientColors : defaultColors
        );
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
    public int getItemCount() {
        return list.size();
    }

    public static class WritingAssistantTypeViewHolder extends RecyclerView.ViewHolder {

        TextView tvWritingType;

        public WritingAssistantTypeViewHolder(View itemView) {
            super(itemView);
            tvWritingType = itemView.findViewById(R.id.tv_writing_type);
        }

    }
}
