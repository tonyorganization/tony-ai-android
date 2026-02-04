package ton_core.ui.screens;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

import ton_core.shared.Constants;

public class AIFeatureSettingsActivity extends BaseFragment {

    private TextCheckCell switchGeneral;

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.AITony));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Constants.TONGRAM_CONFIG, Activity.MODE_PRIVATE);
        final boolean isEnableAITony = preferences.getBoolean(Constants.ENABLE_AI_TONY, true);
        final boolean isEnableTranslation = preferences.getBoolean(Constants.ENABLE_AI_TRANSLATION, true);
        final boolean isEnableWritingAssistant = preferences.getBoolean(Constants.ENABLE_AI_WRITING_ASSISTANT, true);
        final boolean isEnableChatSummary = preferences.getBoolean(Constants.ENABLE_AI_CHAT_SUMMARY, true);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setPadding(dp(15), 0, dp(15), 0);

        LinearLayout listview = new LinearLayout(context);
        listview.setOrientation(LinearLayout.VERTICAL);
        listview.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        switchGeneral = new TextCheckCell(context);
        GradientDrawable gb = new GradientDrawable();
        gb.setCornerRadius(dp(15));
        gb.setColor(Theme.getColor(Theme.key_chats_menuTopBackground));
        switchGeneral.setBackground(gb);
        switchGeneral.setTextAndCheck(LocaleController.getString(R.string.AITony), isEnableAITony, false);
        switchGeneral.setColorfullIcon(Theme.getColor(Theme.key_icon_color), R.drawable.ic_input_ai_enhance, false);

        listview.addView(switchGeneral, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        final TextView general = new TextView(context);
        general.setText(LocaleController.getString(R.string.IndividualSettings));
        general.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        general.setTextSize(14);
        listview.addView(general, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        final LinearLayout llPreferences = new LinearLayout(context);
        llPreferences.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable llBackground = new GradientDrawable();
        llBackground.setCornerRadius(dp(15));
        llBackground.setColor(Theme.getColor(Theme.key_chats_menuTopBackground));
        llPreferences.setBackground(llBackground);

        TextCheckCell switchTranslation = new TextCheckCell(context);
        switchTranslation.setTextAndCheck(LocaleController.getString(R.string.Translation), isEnableAITony && isEnableTranslation, false);
        switchTranslation.setOnClickListener(v -> {
            final boolean isChecked = !switchTranslation.isChecked();
            switchTranslation.setChecked(isChecked);
            preferences.edit().putBoolean(Constants.ENABLE_AI_TRANSLATION, isChecked).apply();
        });

        llPreferences.addView(switchTranslation, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextCheckCell switchWritingAssistant = new TextCheckCell(context);
        switchWritingAssistant.setTextAndCheck(LocaleController.getString(R.string.WritingAssistant), isEnableAITony && isEnableWritingAssistant, false);
        switchWritingAssistant.setOnClickListener(v -> {
            final boolean isChecked = !switchWritingAssistant.isChecked();
            switchWritingAssistant.setChecked(isChecked);
            preferences.edit().putBoolean(Constants.ENABLE_AI_WRITING_ASSISTANT, isChecked).apply();
        });

        llPreferences.addView(switchWritingAssistant, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextCheckCell switchChatSummary = new TextCheckCell(context);
        switchChatSummary.setTextAndCheck(LocaleController.getString(R.string.ChatSummary), isEnableAITony && isEnableChatSummary, false);
        switchChatSummary.setOnClickListener(v -> {
            final boolean isChecked = !switchChatSummary.isChecked();
            switchChatSummary.setChecked(isChecked);
            preferences.edit().putBoolean(Constants.ENABLE_AI_CHAT_SUMMARY, isChecked).apply();
        });

        llPreferences.addView(switchChatSummary, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        listview.addView(llPreferences, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 20, 0, 0));

        final TextView disableAllContent = new TextView(context);
        disableAllContent.setText(LocaleController.getString(R.string.DisableAllAIFeature));
        disableAllContent.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        disableAllContent.setTextSize(14);
        listview.addView(disableAllContent, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        switchGeneral.setOnClickListener(v -> {
            final boolean isChecked = !switchGeneral.isChecked();
            switchGeneral.setChecked(isChecked);
            switchTranslation.setChecked(isChecked);
            switchWritingAssistant.setChecked(isChecked);
            switchChatSummary.setChecked(isChecked);

            preferences.edit()
                    .putBoolean(Constants.ENABLE_AI_TONY, isChecked)
                    .putBoolean(Constants.ENABLE_AI_TRANSLATION, isChecked)
                    .putBoolean(Constants.ENABLE_AI_WRITING_ASSISTANT, isChecked)
                    .putBoolean(Constants.ENABLE_AI_CHAT_SUMMARY, isChecked)
                    .apply();
        });

        frameLayout.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }
}
