package ton_core.ui.dialogs;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ton_core.ui.adapters.AIHistoryAdapter;
import ton_core.ui.models.AIHistoryModel;

public class HistoryDialog extends DialogFragment implements AIHistoryAdapter.IAIHistoryDelegate {

    public static String TAG = "HistoryFragment";
    private AIHistoryAdapter aiHistoryAdapter;
    private final List<AIHistoryModel> historyModels;

    public HistoryDialog() {
        //TODO: Call api to get history
        historyModels = new ArrayList<>();
        historyModels.add(new AIHistoryModel(0, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 0, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(3, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 3, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(4, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 4, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(5, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 5, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(6, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 6, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(7, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 7, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(8, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 8, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(9, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 9, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(10, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 10, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
        historyModels.add(new AIHistoryModel(11, "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!", 11, Instant.now(), "Hello, could you please send me that file? I need it as soon as possible.\u2028Thank you!"));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ai_history_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ConstraintLayout clBackground = view.findViewById(R.id.cl_background);
        ConstraintLayout clRoot = view.findViewById(R.id.root);
        View dimView = view.findViewById(R.id.dim_view);

        dimView.setAlpha(0f);
        clBackground.setTranslationX(-requireContext().getResources().getDisplayMetrics().widthPixels);

        GradientDrawable inner = new GradientDrawable();
        inner.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteShadow));
        inner.setCornerRadii(new float[]{0, 0, dp(15), dp(15), 0, 0, 0, 0});
        clBackground.setBackground(inner);

        clRoot.setOnClickListener(v -> dismissWithAnimation());

        clBackground.post(() -> {
            int width = clBackground.getWidth();
            dimView.animate().alpha(1f).setDuration(300).start();


            clBackground.setTranslationX(-width);

            clBackground.animate()
                    .translationX(0)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        });

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setTextColor(Theme.getColor(Theme.key_profile_title));
        tvTitle.setTypeface(AndroidUtilities.bold());

        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setColorFilter(Theme.getColor(Theme.key_icon_color));
        ivClose.setOnClickListener(v -> dismissWithAnimation());

        aiHistoryAdapter = new AIHistoryAdapter(historyModels, this);

        RecyclerView rvHistory = view.findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvHistory.setAdapter(aiHistoryAdapter);

        final LinearLayout llNoUnreadMessages = view.findViewById(R.id.ll_no_unread_messages);
        final TextView tvNoUnreadMessages = view.findViewById(R.id.tv_no_unread_messages);
        tvNoUnreadMessages.setTextColor(Theme.getColor(Theme.key_text_disable));

        final ImageView ivNoUnreadMessage = view.findViewById(R.id.iv_no_unread_message);
        ivNoUnreadMessage.setColorFilter(Theme.getColor(Theme.key_text_disable));

        showOrHideEmptyLayout(llNoUnreadMessages);

        Button btnClearHistory = view.findViewById(R.id.btn_clear_history);
        btnClearHistory.setTextColor(Theme.getColor(Theme.key_color_red));
        GradientDrawable db = new GradientDrawable();
        db.setCornerRadius(AndroidUtilities.dp(7));
        db.setColor(Theme.getColor(Theme.key_background_delete_button));
        btnClearHistory.setBackground(db);
        btnClearHistory.setOnClickListener(v -> {
            //TODO: Call api to clear history
            historyModels.clear();
            aiHistoryAdapter.notifyDataSetChanged();
            btnClearHistory.setVisibility(View.GONE);
            showOrHideEmptyLayout(llNoUnreadMessages);
        });
    }

    private void showOrHideEmptyLayout(View v) {
        if (historyModels.isEmpty()) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void dismissWithAnimation() {
        View view = getView();
        if (view == null) {
            dismiss();
            return;
        }

        ConstraintLayout clBackground = view.findViewById(R.id.cl_background);
        ConstraintLayout clRoot = view.findViewById(R.id.root);

        int width = clBackground.getWidth();

        clRoot.animate()
                .alpha(0f)
                .setDuration(200)
                .start();

        clBackground.animate()
                .translationX(-width)
                .setDuration(220)
                .withEndAction(this::dismiss)
                .start();
    }

    @Override
    public void showDetail(AIHistoryModel model) {
//        final Activity activity = Objects.requireNonNull(LaunchActivity.getLastFragment()).getParentActivity();
//        if (activity instanceof androidx.fragment.app.FragmentActivity) {
//            androidx.fragment.app.FragmentManager fragmentManager = ((androidx.fragment.app.FragmentActivity) activity).getSupportFragmentManager();
//            new AIHistoryDetailFragment(model).show(fragmentManager, AIHistoryDetailFragment.TAG);
//        }
//        dismissWithAnimation();
    }
}
