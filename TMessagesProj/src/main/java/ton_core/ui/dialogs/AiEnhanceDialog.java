package ton_core.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ton_core.models.TranslateMessageResponse;
import ton_core.models.TranslatedChoice;
import ton_core.models.TranslatedMessage;
import ton_core.models.TranslatedMessageResult;
import ton_core.repositories.translated_message_repository.ITranslatedMessageRepository;
import ton_core.services.IOnApiCallback;
import ton_core.shared.Constants;
import ton_core.ui.adapters.TongramAIFeatureAdapter;
import ton_core.ui.models.TongramAiFeatureModel;
import ton_core.ui.models.TongramLanguageModel;

public class AiEnhanceDialog extends BottomSheetDialogFragment implements LanguagesDialog.Delegate {

    private static class JumpSpan extends android.text.style.MetricAffectingSpan {
        public float translationY;

        @Override
        public void updateMeasureState(android.text.TextPaint p) {
            p.baselineShift += (int) translationY;
        }

        @Override
        public void updateDrawState(android.text.TextPaint tp) {
            tp.baselineShift += (int) translationY;
        }
    }

    private TextView tvApply;
    private TextView tvResult;
    private TextView tvLanguage;
    private final Theme.ResourcesProvider resourcesProvider;
    private TongramLanguageModel selectedLanguage;
    private final Delegate delegate;

    public CharSequence input;
    private final ITranslatedMessageRepository translatedMessageRepository;
    private final List<TongramLanguageModel> languageModels;
    private static final ExecutorService detectLanguageExecutor =
            Executors.newFixedThreadPool(1);
    private EditText edtInput;
    private ImageView ivAction;
    private android.animation.ValueAnimator resultJumpAnimator;

    @Override
    public void onLanguageSelected(TongramLanguageModel language) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Constants.TONGRAM_CONFIG, Activity.MODE_PRIVATE);
        preferences.edit().putString(Constants.OUT_MESSAGE_LANG_CODE_KEY, language.languageCode).putString(Constants.OUT_MESSAGE_LANG_NAME_KEY, language.languageName).apply();

        selectedLanguage = language;
        tvLanguage.setText(language.languageName);
        for (TongramLanguageModel languageModel : languageModels) {
            languageModel.isSelected = languageModel.equals(language);
        }
        setStyleForSendButton();
    }

    public interface Delegate {
        void onTranslatedApply(String text);
    }

    public AiEnhanceDialog(Delegate delegate, Theme.ResourcesProvider resourcesProvider, List<TongramLanguageModel> languageModels, ITranslatedMessageRepository translatedMessageRepository, CharSequence input) {
        this.delegate = delegate;
        this.resourcesProvider = resourcesProvider;
        this.languageModels = languageModels;
        this.translatedMessageRepository = translatedMessageRepository;
        this.input = input;
    }

    public synchronized static AiEnhanceDialog newInstance(Delegate delegate, Theme.ResourcesProvider resourcesProvider, List<TongramLanguageModel> languageModels, ITranslatedMessageRepository translatedMessageRepository, CharSequence input) {
        return new AiEnhanceDialog(delegate, resourcesProvider, languageModels, translatedMessageRepository, input);
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
                behavior.setHideable(false);

                ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(params);
            }
        }
    }

    @Override
    public void dismiss() {
        clearResult();
        stopResultJumpAnimation();
        edtInput.setText(null);
        setStyleForSendButton();
        super.dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tongram_ai_layout, container, false);

        Drawable background = view.findViewById(R.id.cl_root).getBackground();

        if (background != null) {
            int themeColor = Theme.getColor(Theme.key_windowBackgroundWhiteShadow);
            background.setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
        }

        LinearLayout llInput = view.findViewById(R.id.ll_input);
        llInput.setBackgroundColor(Theme.getColor(Theme.key_input_background, resourcesProvider));

        TextView tvTitle = view.findViewById(R.id.tv_tongram_ai);
        tvTitle.setTypeface(AndroidUtilities.bold());
        tvTitle.setTextColor(Theme.getColor(Theme.key_profile_title, resourcesProvider));

        ImageView ivBack = view.findViewById(R.id.iv_back);
        Drawable ivBackDrawable = ivBack.getDrawable();
        if (ivBackDrawable != null) {
            int themeColor = Theme.getColor(Theme.key_profile_title);
            ivBack.setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
        }
        ivBack.setOnClickListener(v -> dismiss());

        View divider = view.findViewById(R.id.v_divider);
        divider.setBackgroundColor(Theme.getColor(Theme.key_divider, resourcesProvider));

        tvResult = view.findViewById(R.id.tv_result);
        tvResult.setTextColor(Theme.getColor(Theme.key_profile_title, resourcesProvider));
        tvResult.setText(LocaleController.getString(R.string.ThreeDot));

        tvApply = view.findViewById(R.id.tv_apply);
        tvApply.setTypeface(AndroidUtilities.bold());
        setTextApply(false);
        tvApply.setOnClickListener(v -> {
            if (tvResult.getText() == null || tvResult.getText().toString().isEmpty() || tvResult.getText().equals(LocaleController.getString(R.string.ThreeDot)) || tvResult.getText().equals(LocaleController.getString(R.string.Translating))) {
                return;
            }
            delegate.onTranslatedApply(tvResult.getText().toString());
            dismiss();
        });

        tvLanguage = view.findViewById(R.id.tv_language);
        tvLanguage.setTextColor(Theme.getColor(Theme.key_text_enable, resourcesProvider));
        setLanguage();

        LinearLayout llChooseLanguage = view.findViewById(R.id.ll_choose_language);
        llChooseLanguage.setOnClickListener(v -> {
            if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
                androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
                LanguagesDialog.newInstance(this, languageModels).show(activity.getSupportFragmentManager(), null);
            }
        });

        List<TongramAiFeatureModel> tongramAiFeatures = new ArrayList<>();
        tongramAiFeatures.add(new TongramAiFeatureModel(R.drawable.ic_ai_translate, LocaleController.getString(R.string.PassportTranslation), false, true));
        tongramAiFeatures.add(new TongramAiFeatureModel(R.drawable.ic_writing_assistant, LocaleController.getString(R.string.WritingAssistant), true, false));

        TongramAIFeatureAdapter tongramAiFeatureAdapter = new TongramAIFeatureAdapter(tongramAiFeatures);
        RecyclerView rvFeatures = view.findViewById(R.id.rv_tongram_feature);
        rvFeatures.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatures.setAdapter(tongramAiFeatureAdapter);

        edtInput = view.findViewById(R.id.edt_input);
        edtInput.setTextColor(Theme.getColor(Theme.key_profile_title, resourcesProvider));
        edtInput.setText(input);
        edtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setStyleForSendButton();
            }
        });

        ivAction = view.findViewById(R.id.iv_action);
        setStyleForSendButton();
        ivAction.setOnClickListener(v -> {
            if (edtInput != null && !edtInput.getText().toString().isEmpty() && selectedLanguage != null) {
                setTextApply(false);
                translateMessage();
            }
        });

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        return view;
    }

    private void setLanguage() {
        for (TongramLanguageModel language : languageModels) {
            if (language.isSelected) {
                selectedLanguage = language;
            }
        }
        if (selectedLanguage != null) {
            tvLanguage.setText(selectedLanguage.languageName);
            setStyleForSendButton();
        }
    }

    private void setStyleForSendButton() {
        if (ivAction == null) return;
        int colorKey;
        if (edtInput == null || edtInput.getText().toString().isEmpty() || selectedLanguage == null) {
            colorKey = Theme.key_button_disable;
            ivAction.setAlpha(0.5f);
        } else {
            colorKey = Theme.key_button_enable;
            ivAction.setAlpha(1f);
        }
        int color = Theme.getColor(colorKey);

        Drawable background = ivAction.getBackground();
        if (background != null) {
            background.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
    }

    private void setTextApply(boolean canApply) {
        if (canApply) {
            tvApply.setTextColor(Theme.getColor(Theme.key_text_enable, resourcesProvider));
        } else {
            tvApply.setTextColor(Theme.getColor(Theme.key_text_disable, resourcesProvider));
        }
    }

    public void clearResult() {
        tvResult.setText(LocaleController.getString(R.string.ThreeDot));
    }

    private void handleTranslateError(String message) {
        BaseFragment lastFragment = LaunchActivity.getLastFragment();
        BulletinFactory.of(lastFragment).createCopyBulletin(message).show();
        clearResult();
        stopResultJumpAnimation();
    }

    private void translateMessage() {
        if (edtInput == null || edtInput.getText().toString().isEmpty()) return;
        input = edtInput.getText();
        edtInput.setText("");
        setStyleForSendButton();
        startResultJumpAnimation();
        detectLanguageExecutor.execute(() -> LanguageDetector.detectLanguage(input.toString(), lng -> AndroidUtilities.runOnUIThread(() -> {
            if (lng.equals(selectedLanguage.languageCode)) {
                tvResult.setText(input);
                stopResultJumpAnimation();
                setTextApply(true);
            } else if (lng.equals("und")) {
                handleTranslateError(getString(R.string.UnableDetectLanguage));
            } else {
                translate();
            }
        }), err -> AndroidUtilities.runOnUIThread(() -> handleTranslateError(err.getMessage()))));
    }

    private void startResultJumpAnimation() {
        stopResultJumpAnimation();

        final String text = LocaleController.getString(R.string.Translating);
        final android.text.SpannableString spannable = new android.text.SpannableString(text);
        final JumpSpan[] spans = new JumpSpan[text.length()];

        for (int i = 0; i < text.length(); i++) {
            spans[i] = new JumpSpan();
            spannable.setSpan(spans[i], i, i + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        resultJumpAnimator = android.animation.ValueAnimator.ofFloat(0, 1f);
        resultJumpAnimator.setDuration(1000);
        resultJumpAnimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        resultJumpAnimator.addUpdateListener(animation -> {
            float phase = (float) animation.getAnimatedValue();
            for (int i = 0; i < spans.length; i++) {
                float startAt = i * 0.05f;
                float endAt = startAt + 0.3f;

                float charProgress = 0f;
                if (phase >= startAt && phase <= endAt) {
                    charProgress = (phase - startAt) / (endAt - startAt);
                }

                if (charProgress > 0) {
                    spans[i].translationY = (float) Math.sin(charProgress * Math.PI) * -AndroidUtilities.dp(4);
                } else {
                    spans[i].translationY = 0;
                }
            }
            tvResult.setText(spannable);
        });
        resultJumpAnimator.start();
    }

    private void stopResultJumpAnimation() {
        if (resultJumpAnimator != null) {
            resultJumpAnimator.cancel();
            resultJumpAnimator = null;
        }
    }

    private void translate() {
        if (selectedLanguage == null) return;
        translatedMessageRepository.draftTranslate(input.toString(), selectedLanguage.languageCode,
                new IOnApiCallback<TranslateMessageResponse>() {
                    @Override
                    public void onSuccess(TranslateMessageResponse data) {
                        stopResultJumpAnimation();
                        final TranslatedMessageResult result = data.getResult();
                        final List<TranslatedChoice> choices = result.getChoices();
                        if (!choices.isEmpty()) {
                            final TranslatedMessage translatedMessage = choices.get(0).getMessage();
                            if (translatedMessage != null) {
                                tvResult.setText(translatedMessage.getContent());
                                setTextApply(true);
                                return;
                            }
                        }
                        handleTranslateError(getString(R.string.UnablePerformTranslation));
                        setTextApply(false);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setTextApply(false);
                        handleTranslateError(errorMessage);
                    }
                });
    }
}
