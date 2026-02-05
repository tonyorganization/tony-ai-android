package ton_core.ui.screens;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;

import ton_core.models.Choice;
import ton_core.models.Message;
import ton_core.models.requests.SummaryRequest;
import ton_core.models.responses.SummaryResponse;
import ton_core.repositories.translated_message_repository.assist_repository.AssistRepository;
import ton_core.repositories.translated_message_repository.assist_repository.IAssistRepository;
import ton_core.services.IOnApiCallback;
import ton_core.ui.adapters.WritingAssistantResultAdapter;
import ton_core.ui.dialogs.LoadingDialog;
import ton_core.ui.models.WritingAssistantResultModel;

public class AISummaryFragment extends Fragment implements WritingAssistantResultAdapter.IWritingAssistantResultDelegate {

    private final List<MessageObject> unreadMessages;
    private final IAssistRepository assistRepository;

    private WritingAssistantResultAdapter resultAdapter;
    private final List<WritingAssistantResultModel> results;
    private final IAIUnreadSummaryDelegate delegate;
    private final LoadingDialog loadingDialog;

    public AISummaryFragment(List<MessageObject> unreadMessages, IAIUnreadSummaryDelegate delegate, WritingAssistantResultModel result) {
        this.unreadMessages = unreadMessages;
        this.delegate = delegate;
        assistRepository = AssistRepository.getInstance();
        results = new ArrayList<>();
        if (result != null) {
            results.add(result);
        }
        final String message = LocaleController.formatString(R.string.AnalyzingSummarizeChat, unreadMessages.size());
        loadingDialog = LoadingDialog.newInstance(message);
    }

    public interface IAIUnreadSummaryDelegate {
        void onSummarized(WritingAssistantResultModel summarized);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onWritingAssistantResultSelected(WritingAssistantResultModel result) {
        results.forEach(e -> e.isSelected = result.id == e.id);
        resultAdapter.notifyDataSetChanged();
    }

    private List<String> getSummarizedText() {
        List<String> messages = new ArrayList<>();
        for (MessageObject message : unreadMessages) {
            if (message.type == 0 && message.messageText != null) {
                messages.add(message.messageText.toString());
            } else if (message.type == 1) {
                messages.add(message.caption.toString());
            }
        }
        return messages;
    }


    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tongram_ai_chat_summary, container, false);

        final LinearLayout llNoUnreadMessages = view.findViewById(R.id.ll_no_unread_messages);
        final TextView tvNoUnreadMessages = view.findViewById(R.id.tv_no_unread_messages);
        tvNoUnreadMessages.setTextColor(Theme.getColor(Theme.key_text_disable));

        final ImageView ivNoUnreadMessage = view.findViewById(R.id.iv_no_unread_message);
        ivNoUnreadMessage.setColorFilter(Theme.getColor(Theme.key_text_disable));

        Button btnSummarize = view.findViewById(R.id.btn_summary);
        btnSummarize.setTextColor(Theme.getColor(Theme.key_view_pager_title_color));

        GradientDrawable db = new GradientDrawable();
        db.setCornerRadius(AndroidUtilities.dp(20));
        db.setColor(Theme.getColor(Theme.key_icon_color));
        btnSummarize.setBackground(db);
        btnSummarize.setOnClickListener(e -> {
            loadingDialog.show(getChildFragmentManager(), LoadingDialog.TAG);
            btnSummarize.setText(LocaleController.getString(R.string.Summarizing));

            assistRepository.summarizeChat(new SummaryRequest(getSummarizedText()), new IOnApiCallback<SummaryResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(SummaryResponse data) {
                    loadingDialog.dismiss();
                    btnSummarize.setText(LocaleController.getString(R.string.Summarize));
                    btnSummarize.setVisibility(View.GONE);
                    llNoUnreadMessages.setVisibility(View.GONE);

                    if (data != null && !data.getSummary().isEmpty()) {
                        final String message = data.getSummary();
                        if (message != null) {
                            final WritingAssistantResultModel result = new WritingAssistantResultModel(1, message, true);
                            results.add(result);
                            resultAdapter.notifyDataSetChanged();
                            delegate.onSummarized(result);
                        }
                    } else {
                        onError("No results found");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    loadingDialog.dismiss();
                    tvNoUnreadMessages.setText(errorMessage);
                    btnSummarize.setText(LocaleController.getString(R.string.Summarize));
                    llNoUnreadMessages.setVisibility(View.VISIBLE);
                }
            });
        });

        resultAdapter = new WritingAssistantResultAdapter(results, this);
        resultAdapter.setType(LocaleController.getString(R.string.ChatSummary));
        RecyclerView rvResults = view.findViewById(R.id.rv_results);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvResults.setLayoutManager(linearLayoutManager);
        rvResults.setNestedScrollingEnabled(false);
        rvResults.setAdapter(resultAdapter);

        if (!results.isEmpty()) {
            resultAdapter.notifyDataSetChanged();
        }

        if (unreadMessages.isEmpty()) {
            llNoUnreadMessages.setVisibility(View.VISIBLE);
            tvNoUnreadMessages.setText(LocaleController.getString(R.string.NoUnreadMessages));
            btnSummarize.setVisibility(View.GONE);
        } else if (results.isEmpty()) {
            llNoUnreadMessages.setVisibility(View.VISIBLE);
            tvNoUnreadMessages.setText(LocaleController.getString(R.string.SummarizeUnreadMessages));
            btnSummarize.setVisibility(View.VISIBLE);
        } else {
            llNoUnreadMessages.setVisibility(View.GONE);
            btnSummarize.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
