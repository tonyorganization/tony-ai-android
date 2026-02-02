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

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ton_core.models.Choice;
import ton_core.models.Message;
import ton_core.models.WritingAssistantRequest;
import ton_core.models.WritingAssistantResponse;
import ton_core.repositories.translated_message_repository.chat_repository.ChatRepository;
import ton_core.repositories.translated_message_repository.chat_repository.IChatRepository;
import ton_core.services.IOnApiCallback;
import ton_core.shared.Constants;
import ton_core.ui.adapters.WritingAssistantResultAdapter;
import ton_core.ui.adapters.WritingAssistantTypeAdapter;
import ton_core.ui.models.WritingAssistantResultModel;
import ton_core.ui.models.WritingAssistantTypeModel;

public class AITemplateFragment extends Fragment implements WritingAssistantTypeAdapter.IWritingAssistantDelegate, WritingAssistantResultAdapter.IWritingAssistantResultDelegate {

    private EditText edtInput;
    private ImageView ivAction;
    public CharSequence input;

    private final List<WritingAssistantTypeModel> listWritingType;
    private final List<WritingAssistantResultModel> results;
    private WritingAssistantTypeAdapter adapter;
    private WritingAssistantResultAdapter resultAdapter;
    private final IAITemplateDelegate delegate;
    private LinearLayout llEmpty;
    private RecyclerView rvResults;
    private final IChatRepository chatRepository;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onWritingAssistantResultSelected(WritingAssistantResultModel result) {
        results.forEach(e -> e.isSelected = result.id == e.id);
        resultAdapter.notifyDataSetChanged();
        delegate.onTransformed(results);
    }

    public interface IAITemplateDelegate {
        void onTransformed(List<WritingAssistantResultModel> results);
    }

    public AITemplateFragment(CharSequence input, IAITemplateDelegate delegate, List<WritingAssistantResultModel> results) {
        this.input = input;
        this.delegate = delegate;

        listWritingType = new ArrayList<>();
        listWritingType.add(new WritingAssistantTypeModel(Constants.AITemplateId.MEETING_REQUEST.id, LocaleController.getString(R.string.MeetingRequest), false));
        listWritingType.add(new WritingAssistantTypeModel(Constants.AITemplateId.WRITE_GREETING.id, LocaleController.getString(R.string.WriteGreeting), false));
        listWritingType.add(new WritingAssistantTypeModel(Constants.AITemplateId.THANK_FOR_NOTE.id, LocaleController.getString(R.string.ThankForNote), false));
        listWritingType.add(new WritingAssistantTypeModel(Constants.AITemplateId.WRITE_EMAIL.id, LocaleController.getString(R.string.WriteEmail), false));

        if (results == null) {
            this.results = new ArrayList<>();
        } else {
            this.results = results;
        }
        chatRepository = ChatRepository.getInstance();
    }

    private WritingAssistantTypeModel getSelectedWritingType() {
        return listWritingType.stream().filter(e -> e.isSelected).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tongram_ai_writing_assistant, container, false);

        adapter = new WritingAssistantTypeAdapter(listWritingType, this);
        RecyclerView recyclerView = view.findViewById(R.id.rv_type);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        LinearLayout llInput = view.findViewById(R.id.ll_input);
        llInput.setBackgroundColor(Theme.getColor(Theme.key_input_background));

        resultAdapter = new WritingAssistantResultAdapter(results, this);
        rvResults = view.findViewById(R.id.rv_results);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvResults.setLayoutManager(linearLayoutManager);
        rvResults.setNestedScrollingEnabled(false);
        rvResults.setAdapter(resultAdapter);

        edtInput = view.findViewById(R.id.edt_input);
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
            edtInput.setText("");
            setSelectedResultType();
            results.clear();
            chatRepository.writeAssistant(new WritingAssistantRequest(edtInput.getText().toString(), "professional"), new IOnApiCallback<WritingAssistantResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(WritingAssistantResponse data) {
                    AndroidUtilities.hideKeyboard(view);
                    if (data != null && !data.getChoices().isEmpty()) {
                        final List<Choice> choices = data.getChoices();
                        for (int i = 0; i < choices.size(); i++) {
                            final Message message = choices.get(i).getMessage();
                            if (message != null && !message.getContent().isEmpty()) {
                                final WritingAssistantResultModel result = new WritingAssistantResultModel(i, message.getContent(), false);
                                results.add(result);
                            }
                        }
                        setResultsVisibility();
                        resultAdapter.notifyDataSetChanged();
                        delegate.onTransformed(results);
                    } else {
                        AndroidUtilities.hideKeyboard(view);
                        onError("No results found");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    setResultsVisibility();
                }
            });
        });

        llEmpty = view.findViewById(R.id.ll_empty);
        final TextView tvEmpty = view.findViewById(R.id.tv_empty);
        tvEmpty.setTextColor(Theme.getColor(Theme.key_text_disable));
        final String emptyText = LocaleController.formatString(R.string.PleaseEnterTextToUseAI, LocaleController.getString(R.string.Template));
        tvEmpty.setText(emptyText);

        final ImageView ivEmpty = view.findViewById(R.id.iv_empty);
        ivEmpty.setColorFilter(Theme.getColor(Theme.key_text_disable));

        setResultsVisibility();

        return view;
    }

    private void setSelectedResultType() {
        final WritingAssistantTypeModel selectedType = getSelectedWritingType();
        if (selectedType != null) {
            resultAdapter.setType(selectedType.title);
        }
    }

    private void setResultsVisibility() {
        if (results.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvResults.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }

    private void setStyleForSendButton() {
        if (ivAction == null) return;
        int colorKey;
        if (edtInput == null || edtInput.getText().toString().isEmpty() || getSelectedWritingType() == null) {
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onWritingAssistantTypeSelected(WritingAssistantTypeModel type) {
        for (WritingAssistantTypeModel e : listWritingType) {
            e.isSelected = Objects.equals(e.id, type.id);
        }
        adapter.notifyDataSetChanged();
        setStyleForSendButton();
    }
}
