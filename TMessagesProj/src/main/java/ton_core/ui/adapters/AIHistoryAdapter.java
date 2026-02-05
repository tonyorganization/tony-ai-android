package ton_core.ui.adapters;

import static org.telegram.ui.ActionBar.Theme.key_background_selected;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;

import ton_core.shared.Constants;
import ton_core.shared.Utils;
import ton_core.ui.models.AIHistoryModel;

public class AIHistoryAdapter extends RecyclerView.Adapter<AIHistoryAdapter.ViewHolder> {

    private final List<AIHistoryModel> historyModels;
    private final IAIHistoryDelegate delegate;

    public interface IAIHistoryDelegate {
        void showDetail(AIHistoryModel model);
    }

    public AIHistoryAdapter(List<AIHistoryModel> historyModels, IAIHistoryDelegate delegate) {
        this.historyModels = historyModels;
        this.delegate = delegate;
    }

    @NonNull
    @Override
    public AIHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ai_history_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AIHistoryAdapter.ViewHolder holder, int position) {
        final AIHistoryModel model = historyModels.get(position);
        holder.ivType.setImageResource(getIconType(model.type));
        holder.ivType.setColorFilter(Theme.getColor(Theme.key_icon_black_blue));
        setItemBackground(holder.llResult, 5);
        setItemBackground(holder.llType, 10);
        holder.tvResult.setText(model.result);
        holder.tvResult.setTextColor(Theme.getColor(Theme.key_text_title_color));
        holder.tvType.setText(getFeatureName(model.type));
        holder.tvType.setTextColor(Theme.getColor(Theme.key_text_title_color));
        holder.tvTime.setText(Utils.formatTime(model.time));

        holder.tvResult.setMaxLines(model.isExpand ? Integer.MAX_VALUE : 1);
        holder.ivExpand.setRotation(model.isExpand ? 180f : 0f);

        holder.ivExpand.setColorFilter(Theme.getColor(Theme.key_icon_black_blue));

        holder.llResult.setOnClickListener(v -> {
            model.isExpand = !model.isExpand;
            holder.tvResult.post(() -> {
                holder.tvResult.setMaxLines(model.isExpand ? Integer.MAX_VALUE : 1);
                animateExpand(holder.tvResult, model.isExpand);
                holder.ivExpand.animate()
                        .rotation(model.isExpand ? 180f : 0f)
                        .setDuration(200)
                        .start();
            });
        });

        holder.root.setOnClickListener(v -> delegate.showDetail(model));
    }

    private void animateExpand(TextView tv, boolean expand) {
        tv.measure(
                View.MeasureSpec.makeMeasureSpec(tv.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
        );

        int start = expand ? tv.getLineHeight() : tv.getMeasuredHeight();
        int end = expand ? tv.getMeasuredHeight() : tv.getLineHeight();

        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(220);
        animator.addUpdateListener(a -> {
            tv.getLayoutParams().height = (int) a.getAnimatedValue();
            tv.requestLayout();
        });
        animator.start();
    }


    private void setItemBackground(View view, int radius) {
        GradientDrawable inner = new GradientDrawable();
        inner.setColor(Theme.getColor(key_background_selected));

        inner.setCornerRadius(AndroidUtilities.dp(radius));

        view.setBackground(inner);
    }

    private int getIconType(int type) {
        if (Constants.AITypeId.TRANSLATION.id == type) {
            return R.drawable.ic_ai_translate;
        } else if (Constants.AIImproveId.MAKE_FORMAL.id == type) {
            return R.drawable.ic_make_formal;

        } else if (Constants.AITypeId.SUMMARY.id == type) {
            return R.drawable.ic_summary;

        } else if (Constants.AIImproveId.MAKE_FRIENDLY.id == type) {
            return R.drawable.ic_make_friendly;

        } else if (Constants.AIImproveId.MAKE_POLITE.id == type) {
            return R.drawable.ic_make_polite;

        } else if (Constants.AIImproveId.FIX_GRAMMAR.id == type) {
            return R.drawable.ic_writing_assistant;

        } else if (Constants.AITemplateId.SET_MEETING.id == type) {
            return R.drawable.ic_set_meeting;

        } else if (Constants.AITemplateId.SAY_HI.id == type) {
            return R.drawable.ic_say_hi;

        } else if (Constants.AITemplateId.SAY_THANKS.id == type) {
            return R.drawable.ic_thanks;

        } else if (Constants.AITemplateId.WRITE_EMAIL.id == type) {
            return R.drawable.ic_write_email;

        }
        return R.drawable.ic_writing_assistant;
    }

    private String getFeatureName(int type) {
        if (Constants.AITypeId.TRANSLATION.id == type) {
            return LocaleController.getString(R.string.Translation);
        } else if (Constants.AIImproveId.MAKE_FORMAL.id == type) {
            return LocaleController.getString(R.string.MakeFormal);

        } else if (Constants.AITypeId.SUMMARY.id == type) {
            return LocaleController.getString(R.string.ChatSummary);

        } else if (Constants.AIImproveId.MAKE_FRIENDLY.id == type) {
            return LocaleController.getString(R.string.MakeFriendly);

        } else if (Constants.AIImproveId.MAKE_POLITE.id == type) {
            return LocaleController.getString(R.string.MakePolite);

        } else if (Constants.AIImproveId.FIX_GRAMMAR.id == type) {
            return LocaleController.getString(R.string.FixGrammar);

        } else if (Constants.AITemplateId.SET_MEETING.id == type) {
            return LocaleController.getString(R.string.SetMeeting);

        } else if (Constants.AITemplateId.SAY_HI.id == type) {
            return LocaleController.getString(R.string.SayHi);

        } else if (Constants.AITemplateId.SAY_THANKS.id == type) {
            return LocaleController.getString(R.string.ThankForNote);

        } else if (Constants.AITemplateId.WRITE_EMAIL.id == type) {
            return LocaleController.getString(R.string.WriteEmail);

        }
        return LocaleController.getString(R.string.WritingAssistant);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return historyModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivType;
        LinearLayout llType;
        LinearLayout llResult;
        TextView tvType;
        TextView tvTime;
        TextView tvResult;
        ImageView ivExpand;
        ConstraintLayout root;

        public ViewHolder(View itemView) {
            super(itemView);

            ivType = itemView.findViewById(R.id.iv_history_type);
            llType = itemView.findViewById(R.id.ll_type);
            tvType = itemView.findViewById(R.id.tv_history_type);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvResult = itemView.findViewById(R.id.tv_result);
            llResult = itemView.findViewById(R.id.ll_result);
            ivExpand = itemView.findViewById(R.id.iv_expand);
            root = itemView.findViewById(R.id.cl_root);
        }
    }
}
