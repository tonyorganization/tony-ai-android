package ton_core.ui.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ton_core.models.requests.GenerateImageRequest;
import ton_core.repositories.translated_message_repository.image_repository.IImageRepository;
import ton_core.repositories.translated_message_repository.image_repository.ImageRepository;
import ton_core.services.IOnApiCallback;
import ton_core.ui.adapters.SuggestionAdapter;
import ton_core.ui.dialogs.LoadingDialog;

public class AIGenerateImageFragment extends BaseFragment implements SuggestionAdapter.Delegate {

    private EditText edtInput;
    private ImageView ivAction;
    public CharSequence input;
    private IImageRepository imageRepository;
    private LoadingDialog loadingDialog;
    private ImageView ivResult;
    private RecyclerView rvSuggestion;
    private File cacheFile;
    private LinearLayout llResult;
    private TextView tvSource;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.ImageGenerate));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = LayoutInflater.from(context).inflate(R.layout.ai_image_generate_layout, null);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        imageRepository = ImageRepository.getInstance();
        loadingDialog = new LoadingDialog();

        LinearLayout llInput = fragmentView.findViewById(R.id.ll_input);
        llInput.setBackgroundColor(Theme.getColor(Theme.key_input_background));

        rvSuggestion = fragmentView.findViewById(R.id.rv_suggestions);
        rvSuggestion.setLayoutManager(new LinearLayoutManager(context));
        List<String> suggestions = new ArrayList<>();
        suggestions.add(context.getString(R.string.generate_image_suggestion_1));
        suggestions.add(context.getString(R.string.generate_image_suggestion_2));
        suggestions.add(context.getString(R.string.generate_image_suggestion_3));
        suggestions.add(context.getString(R.string.generate_image_suggestion_4));
        SuggestionAdapter adapter = new SuggestionAdapter(suggestions, this);
        rvSuggestion.setAdapter(adapter);

        ivResult = fragmentView.findViewById(R.id.iv_result);
        llResult = fragmentView.findViewById(R.id.ll_result);
        tvSource = fragmentView.findViewById(R.id.tv_source);
        CardView cvSource = fragmentView.findViewById(R.id.cv_source);
        cvSource.setCardBackgroundColor(Theme.getColor(Theme.keyTonyAiFeatureBackground));
        TextView tvSource = fragmentView.findViewById(R.id.tv_source);
        tvSource.setTextColor(Theme.getColor(Theme.keyAiFeatureTextEnhanceResult));

        CardView cvUndo = fragmentView.findViewById(R.id.cvUndo);
        cvUndo.setBackground(createActionDrawable());
        cvUndo.setOnClickListener(v -> {
            cacheFile = null;
            setResultsVisibility();
        });
        TextView tvUndo = fragmentView.findViewById(R.id.tv_undo);
        tvUndo.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        CardView cvSave = fragmentView.findViewById(R.id.cvSave);
        cvSave.setBackground(createActionDrawable());
        cvSave.setOnClickListener(v -> {
            if (cacheFile == null) return;
            MediaController.saveFile(cacheFile.getAbsolutePath(), getContext(), 0, null, null);
            Toast.makeText(getParentActivity(), LocaleController.getString(R.string.ImageSaved), Toast.LENGTH_SHORT).show();
        });
        TextView tvSave = fragmentView.findViewById(R.id.tv_save);
        tvSave.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        CardView cvCopy = fragmentView.findViewById(R.id.cvCopy);
        cvCopy.setBackground(createActionDrawable());
        cvCopy.setOnClickListener(v -> {
            if (cacheFile == null) return;

            try {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                Uri uri = androidx.core.content.FileProvider.getUriForFile(getContext(),
                        ApplicationLoader.getApplicationId() + ".provider", cacheFile);

                ClipData clip = ClipData.newUri(getContext().getContentResolver(), "Image", uri);

                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getParentActivity(), LocaleController.getString(R.string.ImageCopied), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ignored) {

            }
            Toast.makeText(getParentActivity(), LocaleController.getString(R.string.ImageCopied), Toast.LENGTH_SHORT).show();
        });
        TextView tvCopy = fragmentView.findViewById(R.id.tv_copy);
        tvCopy.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        CardView cvShare = fragmentView.findViewById(R.id.cvShare);
        cvShare.setBackground(createActionDrawable());
        cvShare.setOnClickListener(v -> {
            if (cacheFile == null) return;

            Uri uri = FileProvider.getUriForFile(getContext(),
                    ApplicationLoader.getApplicationId() + ".provider", cacheFile);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            getParentActivity().startActivity(Intent.createChooser(share, LocaleController.getString(R.string.ShareFile)));
        });
        TextView tvShare = fragmentView.findViewById(R.id.tv_share);
        tvShare.setTextColor(Theme.getColor(Theme.keyAiFeatureActionCardText));

        edtInput = fragmentView.findViewById(R.id.edt_input);
        edtInput.requestFocus();
        edtInput.setTextColor(Theme.getColor(Theme.key_profile_title));
        edtInput.setHintTextColor(Theme.getColor(Theme.key_text_disable));
        edtInput.setHint(LocaleController.getString(R.string.hint_generate_image_from_text));
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
        ivAction.setOnClickListener(v -> generateImage());

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
    private void generateImage() {
        if (edtInput.getText().toString().isEmpty()) return;
        if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
            loadingDialog.show(activity.getSupportFragmentManager(), LoadingDialog.TAG);
        }

        final String messageRequest = edtInput.getText().toString();

        imageRepository.generateImage(new GenerateImageRequest(messageRequest), new IOnApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                loadingDialog.dismiss();
                if (data != null && data.length > 0) {
                    cacheFile = new File(AndroidUtilities.getCacheDir(), "generated_ai_image_" + System.currentTimeMillis() + ".png");
                    try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                        fos.write(data);
                        AndroidUtilities.runOnUIThread(() -> {
                            edtInput.setText("");
                            tvSource.setText(messageRequest);
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                            ivResult.setImageBitmap(bitmap);
                            setResultsVisibility();

                        });
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadingDialog.dismiss();
            }
        });
    }

    private void setResultsVisibility() {
        if (cacheFile != null) {
            rvSuggestion.setVisibility(View.GONE);
            llResult.setVisibility(View.VISIBLE);
        } else {
            rvSuggestion.setVisibility(View.VISIBLE);
            llResult.setVisibility(View.GONE);
        }
    }

    private void setStyleForSendButton() {
        if (ivAction == null) return;
        int colorKey;
        if (edtInput == null || edtInput.getText().toString().isEmpty()) {
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

    @Override
    public void onSuggestionClick(String suggestion) {
        edtInput.setText(suggestion);
        setStyleForSendButton();
    }
}
