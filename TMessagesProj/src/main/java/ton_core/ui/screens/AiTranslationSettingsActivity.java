package ton_core.ui.screens;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ton_core.shared.Constants;
import ton_core.ui.dialogs.LanguagesDialog;
import ton_core.ui.models.TongramLanguageModel;

public class AiTranslationSettingsActivity extends BaseFragment implements LanguagesDialog.Delegate {

    private TextCheckCell switchGeneral;
    private TextView targetLanguageValue;
    private List<TongramLanguageModel> languageModels;

    @Override
    public boolean onFragmentCreate() {
        languageModels = new ArrayList<>();
        ArrayList<LocaleController.LocaleInfo> arrayList = LocaleController.getInstance().languages;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Constants.TONGRAM_CONFIG, Activity.MODE_PRIVATE);
        final String targetLang = preferences.getString(Constants.TARGET_LANG_CODE_KEY, null);
        languageModels.addAll(arrayList.stream()
                .map(e -> new TongramLanguageModel(e.nameEnglish, e.shortName, e.shortName.equals(targetLang)))
                .collect(Collectors.toList()));
        return super.onFragmentCreate();
    }

    private void setTargetValue(String langCode) {
        final TongramLanguageModel selected = languageModels.stream().filter(e -> Objects.equals(e.languageCode, langCode)).findFirst().orElse(null);
        targetLanguageValue.setText(selected != null ? selected.languageName : LocaleController.getCurrentLanguageName());
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.AiTranslationSettings));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Constants.TONGRAM_CONFIG, Activity.MODE_PRIVATE);
        final String targetLang = preferences.getString(Constants.TARGET_LANG_CODE_KEY, null);
        final boolean isEnableTranslation = preferences.getBoolean(Constants.IS_ENABLE_AI_TRANSLATION_KEY, true);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setPadding(dp(15), 0, dp(15), 0);

        LinearLayout listview = new LinearLayout(context);
        listview.setOrientation(LinearLayout.VERTICAL);
        listview.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        final TextView general = new TextView(context);
        general.setText(LocaleController.getString(R.string.General));
        general.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        general.setTextSize(16);
        listview.addView(general, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        switchGeneral = new TextCheckCell(context);
        GradientDrawable gb = new GradientDrawable();
        gb.setCornerRadius(dp(15));
        gb.setColor(Theme.getColor(Theme.key_chats_menuTopBackground));
        switchGeneral.setBackground(gb);
        switchGeneral.setTextAndCheck(LocaleController.getString(R.string.EnableAITranslation), isEnableTranslation, false);
        switchGeneral.setColorfullIcon(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), R.drawable.settings_translation);
        switchGeneral.setOnClickListener(v -> {
            switchGeneral.setChecked(!switchGeneral.isChecked());
            preferences.edit().putBoolean(Constants.IS_ENABLE_AI_TRANSLATION_KEY, switchGeneral.isChecked()).apply();
        });
        listview.addView(switchGeneral, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        final TextView general2 = new TextView(context);
        general2.setText(LocaleController.getString(R.string.AiTranslationContent));
        general2.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        general2.setTextSize(14);
        listview.addView(general2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        final TextView preference = new TextView(context);
        preference.setText(LocaleController.getString(R.string.Preferences));
        preference.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        preference.setTextSize(16);
        listview.addView(preference, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        final LinearLayout llPreferences = new LinearLayout(context);
        llPreferences.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable llBackground = new GradientDrawable();
        llBackground.setCornerRadius(dp(15));
        llBackground.setColor(Theme.getColor(Theme.key_chats_menuTopBackground));
        llPreferences.setBackground(llBackground);


        final LinearLayout targetLanguageLayout = new LinearLayout(context);
        targetLanguageLayout.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout targetLanguageValueLayout = new LinearLayout(context);
        targetLanguageValueLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView targetLanguageTitle = new TextView(context);
        targetLanguageTitle.setText(LocaleController.getString(R.string.TargetLanguage));
        targetLanguageTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        targetLanguageTitle.setTextSize(16);
        targetLanguageValueLayout.addView(targetLanguageTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        targetLanguageValue = new TextView(context);
        setTargetValue(targetLang);
        targetLanguageValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        targetLanguageValue.setTextSize(14);
        targetLanguageValue.setMaxLines(1);
        targetLanguageValueLayout.addView(targetLanguageValue, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        targetLanguageValueLayout.setOnClickListener(v -> {
            if (switchGeneral.isChecked() && getContext() instanceof androidx.fragment.app.FragmentActivity) {
                androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
                LanguagesDialog.newInstance(this, languageModels).show(activity.getSupportFragmentManager(), null);
            }
        });

        targetLanguageLayout.addView(targetLanguageValueLayout, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1, 0, 0, 0, 20, 0));

        final ImageView ivSelectLanguage = new ImageView(context);
        ivSelectLanguage.setImageResource(R.drawable.attach_arrow_right);
        targetLanguageLayout.addView(ivSelectLanguage, LayoutHelper.createLinear(18, 18, Gravity.CENTER));

        llPreferences.addView(targetLanguageLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 20, 10, 20, 0));

        TextCheckCell switchAutoDetectIncoming = new TextCheckCell(context);
        switchAutoDetectIncoming.setTextAndCheck(LocaleController.getString(R.string.EnableAITranslation), false, false);
//        switchAutoDetectIncoming.setOnClickListener(v -> switchAutoDetectIncoming.setChecked(!switchAutoDetectIncoming.isChecked()));

        llPreferences.addView(switchAutoDetectIncoming, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        listview.addView(llPreferences, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 20, 0, 0));

        frameLayout.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        return themeDescriptions;
    }

    @Override
    public void onLanguageSelected(TongramLanguageModel language) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Constants.TONGRAM_CONFIG, Activity.MODE_PRIVATE);
        preferences.edit().putString(Constants.TARGET_LANG_CODE_KEY, language.languageCode).putString(Constants.TARGET_LANG_NAME_KEY, language.languageName).apply();
        setTargetValue(language.languageCode);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.aiTranslationTargetLangUpdated);
    }
}
