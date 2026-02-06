package ton_core.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import ton_core.shared.Constants;

public class AIFeaturePermissionDialog extends DialogFragment {

    public static final String TAG = "AIFeaturePermissionDialog";

    public AIFeaturePermissionDialog getInstance() {
        return new AIFeaturePermissionDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.enable_ai_tony_permission_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ConstraintLayout clBackground = view.findViewById(R.id.cl_background);

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Constants.TONGRAM_CONFIG, Activity.MODE_PRIVATE);

        GradientDrawable inner = new GradientDrawable();
        inner.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteShadow));
        inner.setCornerRadius(AndroidUtilities.dp(15));
        clBackground.setBackground(inner);

        Button enableAiButton = view.findViewById(R.id.btn_enable_ai);
        enableAiButton.setTextColor(Theme.getColor(Theme.key_text_like_theme));
        GradientDrawable db = new GradientDrawable();
        db.setCornerRadius(AndroidUtilities.dp(7));
        db.setColor(Theme.getColor(Theme.key_icon_color));
        enableAiButton.setBackground(db);
        enableAiButton.setOnClickListener(v -> {
            preferences.edit()
                    .putBoolean(Constants.PERMISSION_ENABLE_APPLIED, true)
                    .putBoolean(Constants.ENABLE_AI_TONY, true)
                    .putBoolean(Constants.ENABLE_AI_TRANSLATION, true)
                    .putBoolean(Constants.ENABLE_AI_WRITING_ASSISTANT, true)
                    .putBoolean(Constants.ENABLE_AI_CHAT_SUMMARY, true)
                    .apply();
            dismiss();
        });

        Button maybeLaterButton = view.findViewById(R.id.btn_maybe_later);
        maybeLaterButton.setTextColor(Theme.getColor(Theme.key_view_pager_title_color));
        GradientDrawable maybeLaterDb = new GradientDrawable();
        maybeLaterDb.setCornerRadius(AndroidUtilities.dp(7));
        maybeLaterDb.setStroke(1, Theme.getColor(Theme.key_icon_color));
        maybeLaterButton.setBackground(maybeLaterDb);
        maybeLaterButton.setOnClickListener(v -> {
            preferences.edit()
                    .putBoolean(Constants.PERMISSION_ENABLE_APPLIED, true)
                    .putBoolean(Constants.ENABLE_AI_TONY, false)
                    .putBoolean(Constants.ENABLE_AI_TRANSLATION, false)
                    .putBoolean(Constants.ENABLE_AI_WRITING_ASSISTANT, false)
                    .putBoolean(Constants.ENABLE_AI_CHAT_SUMMARY, false)
                    .apply();
            dismiss();
        });

        TextView tvBottom = view.findViewById(R.id.tv_bottom);
        tvBottom.setTextColor(Theme.getColor(Theme.key_text_disable));

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setTextColor(Theme.getColor(Theme.key_text_title_color));

        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setTextColor(Theme.getColor(Theme.key_text_title_color));

        TextView tvWritingAssistant = view.findViewById(R.id.tv_writing_assistant);
        tvWritingAssistant.setTextColor(Theme.getColor(Theme.key_text_title_color));

        TextView tvSummary = view.findViewById(R.id.tv_summary);
        tvSummary.setTextColor(Theme.getColor(Theme.key_text_title_color));

        TextView tvTranslation = view.findViewById(R.id.tv_translation);
        tvTranslation.setTextColor(Theme.getColor(Theme.key_text_title_color));

        ImageView ivWritingAssistant = view.findViewById(R.id.iv_writing_assistant);
        ivWritingAssistant.setColorFilter(Theme.getColor(Theme.key_icon_color));

        ImageView ivSummary = view.findViewById(R.id.iv_summary);
        ivSummary.setColorFilter(Theme.getColor(Theme.key_icon_color));

        ImageView ivTranslation = view.findViewById(R.id.iv_translation);
        ivTranslation.setColorFilter(Theme.getColor(Theme.key_icon_color));
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
}
