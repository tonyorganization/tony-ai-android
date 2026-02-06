package ton_core.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import org.telegram.messenger.R;

public class LoadingDialog extends DialogFragment {

    public static final String TAG = "LoadingDialog";
    public static final String CONTENT = "CONTENT";

    public static LoadingDialog newInstance(String content) {
        LoadingDialog fragment = new LoadingDialog();
        Bundle args = new Bundle();
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
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
        return inflater.inflate(R.layout.loading_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView ivLoading = view.findViewById(R.id.iv_loading);
        TextView tvContent = view.findViewById(R.id.tv_content);

        if (getArguments() != null) {
            final String content = getArguments().getString(CONTENT, null);
            tvContent.setText(content);
        }

        Glide.with(this)
                .asGif()
                .load(R.drawable.star_loading)
                .into(ivLoading);

        setCancelable(false);
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
