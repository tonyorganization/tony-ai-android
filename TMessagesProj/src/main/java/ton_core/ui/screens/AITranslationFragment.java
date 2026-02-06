package ton_core.ui.screens;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ton_core.models.responses.TranslateMessageResponse;
import ton_core.models.TranslatedChoice;
import ton_core.models.TranslatedMessage;
import ton_core.models.TranslatedMessageResult;
import ton_core.repositories.translated_message_repository.ITranslatedMessageRepository;
import ton_core.services.IOnApiCallback;
import ton_core.shared.Constants;
import ton_core.ui.dialogs.LanguagesDialog;
import ton_core.ui.models.TongramLanguageModel;

public class AITranslationFragment extends Fragment implements LanguagesDialog.Delegate {

    public interface IAITranslationDelegate {
        void setTextApply(boolean canApply);

        void translated(CharSequence text);
    }

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

    private final IAITranslationDelegate delegate;
    private TextView tvLanguage;
    private TextView tvResult;
    private TongramLanguageModel selectedLanguage;
    public CharSequence input;
    private static final ExecutorService detectLanguageExecutor =
            Executors.newFixedThreadPool(1);
    private android.animation.ValueAnimator resultJumpAnimator;
    private final ITranslatedMessageRepository translatedMessageRepository;
    private final List<TongramLanguageModel> languageModels;
    private EditText edtInput;
    private ImageView ivAction;
    private final CharSequence translatedResult;

    public AITranslationFragment(ITranslatedMessageRepository translatedMessageRepository, List<TongramLanguageModel> languageModels, IAITranslationDelegate delegate, CharSequence translatedResult, CharSequence input) {
        this.translatedMessageRepository = translatedMessageRepository;
        this.languageModels = languageModels;
        this.delegate = delegate;
        this.translatedResult = translatedResult;
        this.input = input;
    }

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
            ivAction.setEnabled(false);
        } else {
            colorKey = Theme.key_button_enable;
            ivAction.setAlpha(1f);
            ivAction.setEnabled(true);
        }
        int color = Theme.getColor(colorKey);

        Drawable background = ivAction.getBackground();
        if (background != null) {
            background.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
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
                delegate.setTextApply(true);
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
                                delegate.setTextApply(true);
                                delegate.translated(translatedMessage.getContent());
                                return;
                            }
                        }
                        handleTranslateError(getString(R.string.UnablePerformTranslation));
                        delegate.setTextApply(false);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        delegate.setTextApply(false);
                        handleTranslateError(errorMessage);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tongram_ai_translation_layout, container, false);

        View divider = view.findViewById(R.id.v_divider);
        divider.setBackgroundColor(Theme.getColor(Theme.key_divider));

        tvResult = view.findViewById(R.id.tv_result);
        tvResult.setTextColor(Theme.getColor(Theme.key_profile_title));
        tvResult.setText(translatedResult != null ? translatedResult : LocaleController.getString(R.string.ThreeDot));

        LinearLayout llInput = view.findViewById(R.id.ll_input);
        llInput.setBackgroundColor(Theme.getColor(Theme.key_input_background));

        tvLanguage = view.findViewById(R.id.tv_language);
        tvLanguage.setTextColor(Theme.getColor(Theme.key_text_enable));
        setLanguage();

        LinearLayout llChooseLanguage = view.findViewById(R.id.ll_choose_language);
        llChooseLanguage.setOnClickListener(v -> {
            if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
                androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
                LanguagesDialog.newInstance(this, languageModels).show(activity.getSupportFragmentManager(), null);
            }
        });

        edtInput = view.findViewById(R.id.edt_input);
        edtInput.requestFocus();
        edtInput.setTextColor(Theme.getColor(Theme.key_profile_title));
        edtInput.setHintTextColor(Theme.getColor(Theme.key_text_disable));
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
        ivAction.setOnClickListener(v -> handleTranslateMessage());
        handleTranslateMessage();

        return view;
    }

    private void handleTranslateMessage() {
        if (edtInput != null && !edtInput.getText().toString().isEmpty() && selectedLanguage != null) {
            delegate.setTextApply(false);
            translateMessage();
        }
    }

    @Override
    public void onDestroy() {
        clearResult();
        stopResultJumpAnimation();
        edtInput.setText(null);
        setStyleForSendButton();
        super.onDestroy();
    }
}
