package ton_core.ui.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private final List<String> suggestions;
    private final Delegate delegate;

    public SuggestionAdapter(List<String> suggestions, Delegate delegate) {
        this.suggestions = suggestions;
        this.delegate = delegate;
    }

    public interface Delegate {
        void onSuggestionClick(String suggestion);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_item_layout, parent, false);
        return new SuggestionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = suggestions.get(position);
        holder.tvTitle.setText(item);
        holder.tvTitle.setTextColor(Theme.getColor(Theme.keyAiFeatureTextEnhanceResult));
        holder.cvSuggestion.setCardBackgroundColor(Theme.getColor(Theme.keyAiFeatureBackgroundEnhanceResult));

        holder.cvSuggestion.setOnClickListener(v -> delegate.onSuggestionClick(item));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CardView cvSuggestion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            cvSuggestion = itemView.findViewById(R.id.cv_suggestion);
        }
    }
}
