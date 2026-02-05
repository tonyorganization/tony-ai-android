//package ton_core.ui.adapters;
//
//import static org.telegram.ui.ActionBar.Theme.key_background_selected;
//
//import android.graphics.drawable.GradientDrawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.Theme;
//
//import java.util.List;
//
//import ton_core.ui.models.WritingAssistantResultModel;
//
//public class HistoryDetailAdapter extends RecyclerView.Adapter<HistoryDetailAdapter.WritingAssistantResultViewHolder> {
//
//    private final List<WritingAssistantResultModel> list;
//    private final IWritingAssistantResultDelegate delegate;
//
//    public String type;
//
//    public interface IWritingAssistantResultDelegate {
//        void onWritingAssistantResultSelected(WritingAssistantResultModel result);
//    }
//
//    public HistoryDetailAdapter(List<WritingAssistantResultModel> list, IWritingAssistantResultDelegate delegate) {
//        this.delegate = delegate;
//        this.list = list;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    @NonNull
//    @Override
//    public WritingAssistantResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.history_detail_item_layout, parent, false);
//        return new HistoryDetailAdapter.WritingAssistantResultViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull WritingAssistantResultViewHolder holder, int position) {
//        final WritingAssistantResultModel model = list.get(position);
//        holder.tvResult.setText(model.message);
//        holder.tvResult.setTextColor(Theme.getColor(Theme.key_profile_title));
//        setItemBackground(holder.tvResult, model.isSelected);
//
//        final String resultNumber = LocaleController.formatString(R.string.ResultNumberWithType, position + 1, type);
//        holder.tvResultNumber.setText(resultNumber);
//        holder.tvResultNumber.setTextColor(Theme.getColor(Theme.key_text_disable));
//
//        if (model.isSelected) {
//            holder.tvResultNumber.setAlpha(1f);
//        } else {
//            holder.tvResultNumber.setAlpha(0.5f);
//        }
//
//        holder.itemView.setOnClickListener(view -> {
//            if (model.isSelected) return;
//
//            if (delegate != null) {
//                delegate.onWritingAssistantResultSelected(model);
//            }
//        });
//    }
//
//    private void setItemBackground(View view, boolean isSelected) {
//        GradientDrawable inner = new GradientDrawable();
//        inner.setColor(Theme.getColor(key_background_selected));
//        if (isSelected) {
//            inner.setAlpha(255);
//            view.setAlpha(1);
//        } else {
//            inner.setAlpha(180);
//            view.setAlpha(0.5f);
//        }
//        inner.setCornerRadius(AndroidUtilities.dp(15));
//
//        view.setBackground(inner);
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    public static class WritingAssistantResultViewHolder extends RecyclerView.ViewHolder {
//
//        TextView tvResult;
//        TextView tvResultNumber;
//
//        public WritingAssistantResultViewHolder(View itemView) {
//            super(itemView);
//            tvResult = itemView.findViewById(R.id.tv_result);
//            tvResultNumber = itemView.findViewById(R.id.tv_result_number);
//        }
//
//    }
//}
