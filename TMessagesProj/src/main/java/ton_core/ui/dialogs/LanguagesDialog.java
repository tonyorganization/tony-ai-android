package ton_core.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import ton_core.ui.adapters.TongramLanguageAdapter;
import ton_core.ui.models.TongramLanguageModel;

public class LanguagesDialog extends BottomSheetDialogFragment implements TongramLanguageAdapter.ITongramLanguageListener {

    private final List<TongramLanguageModel> tongramLanguages;

    private final Delegate delegate;
    private TongramLanguageAdapter tongramLanguageAdapter;

    public interface Delegate {
        void onLanguageSelected(TongramLanguageModel language);
    }

    public LanguagesDialog(Delegate delegate, List<TongramLanguageModel> tongramLanguages) {
        this.delegate = delegate;
        this.tongramLanguages = tongramLanguages;
    }

    public synchronized static LanguagesDialog newInstance(Delegate delegate, List<TongramLanguageModel> tongramLanguages) {
        return new LanguagesDialog(delegate, tongramLanguages);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.languages_bottom_sheet_dialog, container, false);

        Drawable background = view.findViewById(R.id.cl_root).getBackground();

        if (background != null) {
            background = background.mutate();
            int themeColor = Theme.getColor(Theme.key_windowBackgroundWhiteShadow);
            background.setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
        }

        tongramLanguageAdapter = new TongramLanguageAdapter(tongramLanguages, this);

        RecyclerView rvTongramLanguages = view.findViewById(R.id.rv_languages);
        rvTongramLanguages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvTongramLanguages.setAdapter(tongramLanguageAdapter);

        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_profile_title), PorterDuff.Mode.SRC_IN));
        ivClose.setOnClickListener(v -> dismiss());

        TextView title = view.findViewById(R.id.tv_title);
        final int textColor = Theme.getColor(Theme.key_profile_title);
        title.setTextColor(textColor);

        LinearLayout llSearch = view.findViewById(R.id.ll_search);
        Drawable searchBackground = llSearch.getBackground();
        if (searchBackground != null) {
            searchBackground = searchBackground.mutate();
            searchBackground.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_input_background), PorterDuff.Mode.SRC_ATOP));
            searchBackground.setAlpha(180);
        }

        final int colorAlpha = ColorUtils.setAlphaComponent(textColor, 180);
        EditText edtSearch = view.findViewById(R.id.edt_search);
        edtSearch.setHintTextColor(colorAlpha);
        edtSearch.setTextColor(Theme.getColor(Theme.key_profile_title));
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tongramLanguageAdapter.setLanguages(search(editable.toString()));
            }
        });

        ImageView ivSearch = view.findViewById(R.id.iv_search);
        ivSearch.setColorFilter(new PorterDuffColorFilter(colorAlpha, PorterDuff.Mode.SRC_IN));

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    @Override
    public void onLanguageSelected(TongramLanguageModel language) {
        delegate.onLanguageSelected(language);
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
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

    private List<TongramLanguageModel> search(String searchKey) {
        if (searchKey == null || searchKey.isEmpty()) return tongramLanguages;
        return tongramLanguages
                .stream()
                .filter(e -> e.languageName.toLowerCase(Locale.ROOT).contains(searchKey.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }
}
