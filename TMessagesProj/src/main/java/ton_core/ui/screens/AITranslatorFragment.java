package ton_core.ui.screens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ton_core.models.TranslatedChoice;
import ton_core.models.TranslatedMessage;
import ton_core.models.TranslatedMessageResult;
import ton_core.models.responses.TranslateMessageResponse;
import ton_core.repositories.translated_message_repository.ITranslatedMessageRepository;
import ton_core.repositories.translated_message_repository.TranslatedMessageRepository;
import ton_core.repositories.translated_message_repository.languages.ILanguageRepository;
import ton_core.repositories.translated_message_repository.languages.LanguageRepository;
import ton_core.services.IOnApiCallback;
import ton_core.shared.CustomLifecycleOwner;
import ton_core.ui.adapters.WritingAssistantResultAdapter;
import ton_core.ui.dialogs.LanguagesDialog;
import ton_core.ui.dialogs.LoadingDialog;
import ton_core.ui.models.TongramLanguageModel;
import ton_core.ui.models.WritingAssistantResultModel;

public class AITranslatorFragment extends BaseFragment implements WritingAssistantResultAdapter.IWritingAssistantResultDelegate, LanguagesDialog.Delegate {

    private EditText edtInput;
    private ImageView ivAction;
    public CharSequence input;

    private final List<WritingAssistantResultModel> results;
    private WritingAssistantResultAdapter resultAdapter;

    private LinearLayout llEmpty;
    private TextView tvEmpty;
    private RecyclerView rvResults;
    private final ITranslatedMessageRepository translatedMessageRepository;
    private final LoadingDialog loadingDialog;
    private LinearLayout llResultActions;
    private final List<TongramLanguageModel> sourceLang;
    private final List<TongramLanguageModel> targetLang;
    private boolean isSelectSourceLang = false;
    private TextView tvSourceLang;
    private TextView tvTargetLang;

    public AITranslatorFragment() {
        results = new ArrayList<>();
        sourceLang = new ArrayList<>();
        targetLang = new ArrayList<>();
        translatedMessageRepository = TranslatedMessageRepository.getInstance(getParentActivity());
        loadingDialog = new LoadingDialog();
        ILanguageRepository languageRepository = LanguageRepository.getInstance(ApplicationLoader.applicationContext);
        CustomLifecycleOwner lifecycleOwner = new CustomLifecycleOwner();
        lifecycleOwner.onStart();
        languageRepository.getLanguages().observe(lifecycleOwner, languages -> {
            sourceLang.addAll(languages.stream()
                    .map(e -> new TongramLanguageModel(e.name, e.code, e.nativeName, false))
                    .collect(Collectors.toList()));
            targetLang.addAll(languages.stream()
                    .map(e -> new TongramLanguageModel(e.name, e.code, e.nativeName, false))
                    .collect(Collectors.toList()));
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.translator));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = LayoutInflater.from(context).inflate(R.layout.ai_translator_layout, null);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        CardView cvSourceLang = fragmentView.findViewById(R.id.card_source_lang);
        cvSourceLang.setCardBackgroundColor(Theme.getColor(Theme.keyAiFeatureBackgroundTabDefault));
        tvSourceLang = fragmentView.findViewById(R.id.tv_source_lang);
        tvSourceLang.setTextColor(Theme.getColor(Theme.key_text_title_color));
        ImageView ivSourceLang = fragmentView.findViewById(R.id.iv_source_lang);
        ivSourceLang.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.keyAiFeatureTranslatorTabDefaultIcon), PorterDuff.Mode.SRC_IN));
        cvSourceLang.setOnClickListener(v -> {
            if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
                isSelectSourceLang = true;
                androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
                LanguagesDialog.newInstance(this, sourceLang).show(activity.getSupportFragmentManager(), null);
            }
        });

        CardView cvTargetLang = fragmentView.findViewById(R.id.card_target_lang);
        cvTargetLang.setCardBackgroundColor(Theme.getColor(Theme.keyAiFeatureBackgroundTabActive));
        tvTargetLang = fragmentView.findViewById(R.id.tv_target_lang);
        tvTargetLang.setTextColor(Theme.getColor(Theme.key_text_like_theme));
        ImageView ivTargetLang = fragmentView.findViewById(R.id.iv_target_lang);
        ivTargetLang.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.keyAiFeatureTranslatorTabActiveIcon), PorterDuff.Mode.SRC_IN));
        cvTargetLang.setOnClickListener(v -> {
            if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
                isSelectSourceLang = false;
                androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
                LanguagesDialog.newInstance(this, targetLang).show(activity.getSupportFragmentManager(), null);
            }
        });

        ImageView ivSwap = fragmentView.findViewById(R.id.img_swap_lang);
        ivSwap.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.keyAiFeatureTranslatorTabDefaultIcon), PorterDuff.Mode.SRC_IN));
        ivSwap.setOnClickListener(v -> {
            final TongramLanguageModel source = sourceLang.stream().filter(e -> e.isSelected).findFirst().orElse(null);
            final TongramLanguageModel target = targetLang.stream().filter(e -> e.isSelected).findFirst().orElse(null);

            if (source == null || target == null) return;
            for (TongramLanguageModel e : sourceLang) {
                if (Objects.equals(e.languageCode, target.languageCode)) {
                    e.isSelected = true;
                    tvSourceLang.setText(e.languageName);
                } else {
                    e.isSelected = false;
                }
            }
            for (TongramLanguageModel e : targetLang) {
                if (Objects.equals(e.languageCode, source.languageCode)) {
                    e.isSelected = true;
                    tvTargetLang.setText(e.languageName);
                } else {
                    e.isSelected = false;
                }
            }
        });

        LinearLayout llInput = fragmentView.findViewById(R.id.ll_input);
        llInput.setBackgroundColor(Theme.getColor(Theme.key_input_background));

        resultAdapter = new WritingAssistantResultAdapter(results, this);
        resultAdapter.setType("");
        rvResults = fragmentView.findViewById(R.id.rv_results);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvResults.setLayoutManager(linearLayoutManager);
        rvResults.setNestedScrollingEnabled(false);
        rvResults.setAdapter(resultAdapter);

        edtInput = fragmentView.findViewById(R.id.edt_input);
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

        ivAction = fragmentView.findViewById(R.id.iv_action);
        setStyleForSendButton();
        ivAction.setOnClickListener(v -> translation(fragmentView));

        llEmpty = fragmentView.findViewById(R.id.ll_empty);
        tvEmpty = fragmentView.findViewById(R.id.tv_empty);
        tvEmpty.setTextColor(Theme.getColor(Theme.key_text_disable));
        final String emptyText = LocaleController.formatString(R.string.PleaseEnterTextToUseAI, LocaleController.getString(R.string.Improve));
        tvEmpty.setText(emptyText);

        final ImageView ivEmpty = fragmentView.findViewById(R.id.iv_empty);
        ivEmpty.setColorFilter(Theme.getColor(Theme.key_text_disable));

        llResultActions = fragmentView.findViewById(R.id.ic_result_actions);
        CardView cvUndo = llResultActions.findViewById(R.id.cvUndo);
        cvUndo.setBackground(createActionDrawable());
        cvUndo.setOnClickListener(v -> {
            if (!results.isEmpty()) {
                edtInput.setText(results.get(0).message);
                edtInput.setSelection(edtInput.length());

                results.clear();
                resultAdapter.notifyDataSetChanged();
                setResultsVisibility();
            }
        });
        TextView tvUndo = llResultActions.findViewById(R.id.tv_undo);
        tvUndo.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        CardView cvCopy = llResultActions.findViewById(R.id.cvCopy);
        cvCopy.setBackground(createActionDrawable());
        cvCopy.setOnClickListener(v -> {
            AndroidUtilities.addToClipboard(results.get(1).message);
            Toast.makeText(getParentActivity(), LocaleController.getString(R.string.TextCopied), Toast.LENGTH_SHORT).show();
        });
        TextView tvCopy = llResultActions.findViewById(R.id.tv_copy);
        tvCopy.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        CardView cvShare = fragmentView.findViewById(R.id.cvShare);
        cvShare.setBackground(createActionDrawable());
        cvShare.setOnClickListener(v -> {
            String translatedText = results.get(1).message;
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, translatedText);

                getParentActivity().startActivity(Intent.createChooser(shareIntent, LocaleController.getString(R.string.ShareFile)));
            } catch (Exception ignored) {
            }
        });
        TextView tvShare = llResultActions.findViewById(R.id.tv_share);
        tvShare.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        setResultsVisibility();

        return fragmentView;

    }

    private Drawable createActionDrawable() {
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        drawable.setColor(Theme.getColor(Theme.keyAiFeatureActionCardBackground));
        int strokeWidth = AndroidUtilities.dp(1);
        int strokeColor = Theme.getColor(Theme.keyAiFeatureActionCardStroke);
        drawable.setStroke(strokeWidth, strokeColor);

        drawable.setCornerRadius(AndroidUtilities.dp(100));

        return drawable;
    }

    private void translation(View view) {
        if (edtInput.getText().toString().isEmpty()) return;
        if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
            loadingDialog.show(activity.getSupportFragmentManager(), LoadingDialog.TAG);
        }

        results.clear();
        final String messageRequest = edtInput.getText().toString();
        edtInput.setText("");

        final TongramLanguageModel selectedLanguage = targetLang.stream().filter(e -> e.isSelected).findFirst().orElse(null);
        if (selectedLanguage == null) return;
        translatedMessageRepository.draftTranslate(messageRequest, selectedLanguage.languageCode,
                new IOnApiCallback<TranslateMessageResponse>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(TranslateMessageResponse data) {
                        loadingDialog.dismiss();
                        AndroidUtilities.hideKeyboard(view);
                        final TranslatedMessageResult result = data.getResult();
                        final List<TranslatedChoice> choices = result.getChoices();
                        if (!choices.isEmpty()) {
                            final TranslatedMessage translatedMessage = choices.get(0).getMessage();
                            if (translatedMessage != null) {
                                final WritingAssistantResultModel original = new WritingAssistantResultModel(0, messageRequest, false);
                                final WritingAssistantResultModel resultModel = new WritingAssistantResultModel(1, translatedMessage.getContent(), true);
                                results.add(original);
                                results.add(resultModel);
                                resultAdapter.notifyDataSetChanged();
                                setResultsVisibility();
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        AndroidUtilities.hideKeyboard(view);
                        setResultsVisibility();
                        tvEmpty.setText(errorMessage);
                    }
                });
    }

    private void setStyleForSendButton() {
        if (ivAction == null) return;
        int colorKey;
        if (edtInput == null || edtInput.getText().toString().isEmpty() || targetLang.stream().filter(e -> e.isSelected).findFirst().orElse(null) == null) {
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

    private void setResultsVisibility() {
        if (results.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
            final String emptyText = LocaleController.formatString(R.string.PleaseEnterTextToUseAI, LocaleController.getString(R.string.Improve));
            tvEmpty.setText(emptyText);
            llResultActions.setVisibility(View.GONE);

        } else {
            rvResults.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
            llResultActions.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWritingAssistantResultSelected(WritingAssistantResultModel result) {

    }

    @Override
    public void onLanguageSelected(TongramLanguageModel language) {
        if (isSelectSourceLang) {
            for (TongramLanguageModel languageModel : sourceLang) {
                languageModel.isSelected = languageModel.equals(language);
            }
            tvSourceLang.setText(language.languageName);
        } else {
            for (TongramLanguageModel languageModel : targetLang) {
                languageModel.isSelected = languageModel.equals(language);
            }
            tvTargetLang.setText(language.languageName);
        }
        setStyleForSendButton();
    }
}
