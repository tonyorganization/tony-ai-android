//package ton_core.ui.screens;
//
//import static org.telegram.messenger.AndroidUtilities.dp;
//
//import android.app.Dialog;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.fragment.app.DialogFragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.Theme;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import ton_core.shared.Constants;
//import ton_core.shared.Utils;
//import ton_core.ui.adapters.HistoryDetailAdapter;
//import ton_core.ui.models.AIHistoryModel;
//import ton_core.ui.models.WritingAssistantResultModel;
//
//public class AIHistoryDetailFragment extends DialogFragment implements HistoryDetailAdapter.IWritingAssistantResultDelegate {
//
//    public static String TAG = "AIHistoryDetailFragment";
//    private final AIHistoryModel model;
//
//    public AIHistoryDetailFragment(AIHistoryModel model) {
//        this.model = model;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null && dialog.getWindow() != null) {
//            dialog.getWindow().setLayout(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//            );
//        }
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
//        setCancelable(true);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.ai_history_detail_layout, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        ConstraintLayout clBackground = view.findViewById(R.id.cl_background);
//        View dimView = view.findViewById(R.id.dim_view);
//
//        dimView.setAlpha(0f);
//        clBackground.setTranslationX(-requireContext().getResources().getDisplayMetrics().widthPixels);
//
//        GradientDrawable inner = new GradientDrawable();
//        inner.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteShadow));
//        inner.setCornerRadii(new float[]{0, 0, dp(15), dp(15), 0, 0, 0, 0});
//        clBackground.setBackground(inner);
//
//
//        clBackground.post(() -> {
//            int width = clBackground.getWidth();
//            dimView.animate().alpha(1f).setDuration(300).start();
//
//
//            clBackground.setTranslationX(width);
//
//            clBackground.animate()
//                    .translationX(0)
//                    .setDuration(300)
//                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
//                    .start();
//        });
//
//        TextView tvTitle = view.findViewById(R.id.tv_title);
//        tvTitle.setTextColor(Theme.getColor(Theme.key_profile_title));
//        tvTitle.setTypeface(AndroidUtilities.bold());
//        tvTitle.setText(LocaleController.formatString(R.string.HistoryDetailWithFeature, getFeatureName(model.type)));
//
//        ImageView ivClose = view.findViewById(R.id.iv_close);
//        ivClose.setColorFilter(Theme.getColor(Theme.key_profile_title));
//        ivClose.setOnClickListener(v -> dismissWithAnimation());
//
//        TextView tvTime = view.findViewById(R.id.tv_time);
//        tvTime.setTextColor(Theme.getColor(Theme.key_profile_title));
//        tvTime.setText(Utils.formatTime(model.time));
//
//        Button btnClearHistory = view.findViewById(R.id.btn_clear_history);
//        btnClearHistory.setTextColor(Theme.getColor(Theme.key_color_red));
//        GradientDrawable db = new GradientDrawable();
//        db.setCornerRadius(AndroidUtilities.dp(7));
//        db.setColor(Theme.getColor(Theme.key_background_delete_button));
//        btnClearHistory.setBackground(db);
//        btnClearHistory.setOnClickListener(v -> {
//            //TODO: call api to delete this history
//            dismissWithAnimation();
//        });
//
//        final List<WritingAssistantResultModel> list = new ArrayList<>();
//        list.add(new WritingAssistantResultModel(model.id, model.result, true));
//        HistoryDetailAdapter adapter = new HistoryDetailAdapter(list, this);
//
//        RecyclerView rvHistory = view.findViewById(R.id.rv_history);
//        rvHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        rvHistory.setAdapter(adapter);
//    }
//
//    private String getFeatureName(int type) {
//        if (Constants.AITypeId.TRANSLATION.id == type) {
//            return LocaleController.getString(R.string.Translation);
//        } else if (Constants.AIImproveId.MAKE_FORMAL.id == type) {
//            return LocaleController.getString(R.string.MakeFormal);
//
//        } else if (Constants.AITypeId.SUMMARY.id == type) {
//            return LocaleController.getString(R.string.ChatSummary);
//
//        } else if (Constants.AIImproveId.MAKE_FRIENDLY.id == type) {
//            return LocaleController.getString(R.string.MakeFriendly);
//
//        } else if (Constants.AIImproveId.MAKE_POLITE.id == type) {
//            return LocaleController.getString(R.string.MakePolite);
//
//        } else if (Constants.AIImproveId.FIX_GRAMMAR.id == type) {
//            return LocaleController.getString(R.string.FixGrammar);
//
//        } else if (Constants.AITemplateId.SET_MEETING.id == type) {
//            return LocaleController.getString(R.string.SetMeeting);
//
//        } else if (Constants.AITemplateId.SAY_HI.id == type) {
//            return LocaleController.getString(R.string.SayHi);
//
//        } else if (Constants.AITemplateId.SAY_THANKS.id == type) {
//            return LocaleController.getString(R.string.ThankForNote);
//
//        } else if (Constants.AITemplateId.WRITE_EMAIL.id == type) {
//            return LocaleController.getString(R.string.WriteEmail);
//
//        }
//        return LocaleController.getString(R.string.WritingAssistant);
//    }
//
//    private void dismissWithAnimation() {
//        View view = getView();
//        if (view == null) {
//            dismiss();
//            return;
//        }
//
//        ConstraintLayout clBackground = view.findViewById(R.id.cl_background);
//        ConstraintLayout clRoot = view.findViewById(R.id.root);
//
//        int width = clBackground.getWidth();
//
//        clRoot.animate()
//                .alpha(0f)
//                .setDuration(200)
//                .start();
//
//        clBackground.animate()
//                .translationX(width)
//                .setDuration(220)
//                .withEndAction(this::dismiss)
//                .start();
//    }
//
//    @Override
//    public void onWritingAssistantResultSelected(WritingAssistantResultModel result) {
//    }
//}
