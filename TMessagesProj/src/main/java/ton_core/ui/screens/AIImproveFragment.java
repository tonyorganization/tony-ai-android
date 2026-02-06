package ton_core.ui.screens;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;

import ton_core.models.requests.ToneTransformRequest;
import ton_core.models.responses.FixGrammarResponse;
import ton_core.models.responses.ToneTransformResponse;
import ton_core.repositories.translated_message_repository.chat_repository.ChatRepository;
import ton_core.repositories.translated_message_repository.chat_repository.IChatRepository;
import ton_core.services.IOnApiCallback;
import ton_core.shared.Constants;
import ton_core.ui.adapters.WritingAssistantResultAdapter;
import ton_core.ui.dialogs.LoadingDialog;
import ton_core.ui.models.TongramAiFeatureModel;
import ton_core.ui.models.WritingAssistantResultModel;

public class AIImproveFragment extends Fragment implements WritingAssistantResultAdapter.IWritingAssistantResultDelegate {

    private EditText edtInput;
    private ImageView ivAction;
    public CharSequence input;

    private final List<WritingAssistantResultModel> results;
    private WritingAssistantResultAdapter resultAdapter;
    private final IAIImproveDelegate delegate;

    private LinearLayout llEmpty;
    private TextView tvEmpty;
    private RecyclerView rvResults;
    private final IChatRepository chatRepository;
    private final TongramAiFeatureModel feature;
    private final LoadingDialog loadingDialog;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onWritingAssistantResultSelected(WritingAssistantResultModel result) {
        results.forEach(e -> e.isSelected = result.id == e.id);
        resultAdapter.notifyDataSetChanged();
        delegate.onImproved(results, feature.subId);
    }

    public interface IAIImproveDelegate {
        void onImproved(List<WritingAssistantResultModel> results, int typeId);
    }

    public AIImproveFragment(CharSequence input, List<WritingAssistantResultModel> results, IAIImproveDelegate delegate, TongramAiFeatureModel feature) {
        this.input = input;
        this.delegate = delegate;
        this.feature = feature;

        if (results == null) {
            this.results = new ArrayList<>();
        } else {
            this.results = results;
        }
        chatRepository = ChatRepository.getInstance();
        loadingDialog = new LoadingDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tongram_ai_writing_assistant, container, false);

        LinearLayout llInput = view.findViewById(R.id.ll_input);
        llInput.setBackgroundColor(Theme.getColor(Theme.key_input_background));

        resultAdapter = new WritingAssistantResultAdapter(results, this);
        resultAdapter.setType(feature.title);
        rvResults = view.findViewById(R.id.rv_results);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvResults.setLayoutManager(linearLayoutManager);
        rvResults.setNestedScrollingEnabled(false);
        rvResults.setAdapter(resultAdapter);

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
        ivAction.setOnClickListener(v -> {
            loadingDialog.show(getChildFragmentManager(), LoadingDialog.TAG);
            results.clear();
            final String messageRequest = edtInput.getText().toString();
            edtInput.setText("");
            if (feature.subId == Constants.AIImproveId.FIX_GRAMMAR.id) {
                chatRepository.fixGrammar(new ToneTransformRequest(messageRequest), new IOnApiCallback<FixGrammarResponse>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(FixGrammarResponse data) {
                        loadingDialog.dismiss();
                        AndroidUtilities.hideKeyboard(view);
                        if (data != null && !data.getCorrectedText().isEmpty()) {
                            final String message = data.getCorrectedText();
                            final WritingAssistantResultModel result = new WritingAssistantResultModel(1, message, true);
                            results.add(result);
                            resultAdapter.notifyDataSetChanged();
                            setResultsVisibility();
                            delegate.onImproved(results, feature.subId);
                        } else {
                            onError("No results found");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        loadingDialog.dismiss();
                        AndroidUtilities.hideKeyboard(view);
                        setResultsVisibility();
                    }
                });
            } else {

                chatRepository.toneTransform(new ToneTransformRequest(messageRequest, getTone()), new IOnApiCallback<ToneTransformResponse>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(ToneTransformResponse data) {
                        loadingDialog.dismiss();
                        AndroidUtilities.hideKeyboard(view);
                        if (data != null && !data.getTransformedText().isEmpty()) {
                            final String message = data.getTransformedText();
                            if (message != null) {
                                final WritingAssistantResultModel result = new WritingAssistantResultModel(0, message, true);
                                results.add(result);
                            }
                            setResultsVisibility();
                            resultAdapter.notifyDataSetChanged();
                            delegate.onImproved(results, feature.subId);
                        } else {
                            onError("No results found");
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
        });


        llEmpty = view.findViewById(R.id.ll_empty);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvEmpty.setTextColor(Theme.getColor(Theme.key_text_disable));
        final String emptyText = LocaleController.formatString(R.string.PleaseEnterTextToUseAI, LocaleController.getString(R.string.Improve));
        tvEmpty.setText(emptyText);

        final ImageView ivEmpty = view.findViewById(R.id.iv_empty);
        ivEmpty.setColorFilter(Theme.getColor(Theme.key_text_disable));

        setResultsVisibility();

        return view;
    }

    private String getTone() {
        if (feature.subId == Constants.AIImproveId.MAKE_FORMAL.id) {
            return Constants.ToneKey.MAKE_FORMAL.key;
        } else if (feature.subId == Constants.AIImproveId.MAKE_FRIENDLY.id) {
            return Constants.ToneKey.MAKE_FRIENDLY.key;
        } else {
            return Constants.ToneKey.MAKE_POLITE.key;
        }
    }

    private void setStyleForSendButton() {
        if (ivAction == null) return;
        int colorKey;
        if (edtInput == null || edtInput.getText().toString().isEmpty()) {
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
        } else {
            rvResults.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }
}
