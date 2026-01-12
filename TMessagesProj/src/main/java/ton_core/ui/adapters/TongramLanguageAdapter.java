package ton_core.ui.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;
import java.util.Objects;

import ton_core.ui.models.TongramLanguageModel;

public class TongramLanguageAdapter extends RecyclerView.Adapter<TongramLanguageAdapter.TongramLanguageViewHolder> {

    private List<TongramLanguageModel> languages;
    private final ITongramLanguageListener listener;

    public TongramLanguageAdapter(List<TongramLanguageModel> languages, ITongramLanguageListener listener) {
        this.languages = languages;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLanguages(List<TongramLanguageModel> languages) {
        this.languages = languages;
        notifyDataSetChanged();
    }

    public interface ITongramLanguageListener {
        void onLanguageSelected(TongramLanguageModel language);
    }

    @NonNull
    @Override
    public TongramLanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tongram_language_item, parent, false);
        return new TongramLanguageViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TongramLanguageViewHolder holder, int position) {
        final TongramLanguageModel language = languages.get(position);
        holder.cbLanguage.setChecked(language.isSelected);
        holder.cbLanguage.setOnCheckedChangeListener(null);
        holder.tvLanguage.setText(language.languageName);
        holder.tvNativeLanguage.setText(language.nativeLanguage);
        holder.tvLanguage.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        holder.tvNativeLanguage.setTextColor(Theme.getColor(Theme.key_profile_title));

        if (language.isSelected) {
            holder.tvLanguage.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        } else {
            holder.tvLanguage.setTypeface(Typeface.DEFAULT);
        }

        holder.itemView.setOnClickListener(view -> {
            if (language.isSelected) return;

            for (TongramLanguageModel model : languages) {
                model.isSelected = Objects.equals(model.languageCode, language.languageCode);
            }

            holder.itemView.post(() -> {
                if (!holder.itemView.isAttachedToWindow()) return;
                notifyDataSetChanged();
            });

            if (listener != null) {
                listener.onLanguageSelected(language);
            }
        });
        holder.cbLanguage.setOnClickListener(v -> holder.itemView.performClick());
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public static class TongramLanguageViewHolder extends RecyclerView.ViewHolder {

        CheckBox cbLanguage;
        TextView tvLanguage;
        TextView tvNativeLanguage;

        public TongramLanguageViewHolder(View itemView) {
            super(itemView);
            cbLanguage = itemView.findViewById(R.id.cb_language);
            tvLanguage = itemView.findViewById(R.id.tv_language);
            tvNativeLanguage = itemView.findViewById(R.id.tv_native_language);
        }

    }
}
