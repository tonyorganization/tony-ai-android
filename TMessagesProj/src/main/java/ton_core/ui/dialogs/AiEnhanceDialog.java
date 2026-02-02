package ton_core.ui.dialogs;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ton_core.repositories.translated_message_repository.ITranslatedMessageRepository;
import ton_core.shared.Constants;
import ton_core.ui.models.TongramAiFeatureModel;
import ton_core.ui.models.TongramLanguageModel;
import ton_core.ui.models.WritingAssistantResultModel;
import ton_core.ui.screens.AIImproveFragment;
import ton_core.ui.screens.AITranslationFragment;
import ton_core.ui.screens.AIUnreadSummaryFragment;
import ton_core.ui.screens.AITemplateFragment;

public class AiEnhanceDialog extends BottomSheetDialogFragment implements AITranslationFragment.IAITranslationDelegate,
        AITemplateFragment.IAITemplateDelegate,
        AIImproveFragment.IAIImproveDelegate,
        AIUnreadSummaryFragment.IAIUnreadSummaryDelegate {
    private TextView tvApply;
    private final ITranslatedMessageRepository translatedMessageRepository;
    private final List<TongramLanguageModel> languageModels;
    private final Theme.ResourcesProvider resourcesProvider;
    private final ArrayList<MessageObject> unreadMessages;
    private final Delegate delegate;
    private final List<TongramAiFeatureModel> aiTabs;
    private CharSequence translatedResult;
    public CharSequence input;
    public CharSequence transformInput;
    public List<WritingAssistantResultModel> transformed;

    public CharSequence improveInput;
    public List<WritingAssistantResultModel> improved;

    public WritingAssistantResultModel summarized;

    @Override
    public void onImproved(List<WritingAssistantResultModel> results) {
        this.improved = results;
        setTextApply(getImprovedSelectedResult() != null);
    }

    @Override
    public void onTransformed(List<WritingAssistantResultModel> results) {
        this.transformed = results;
        setTextApply(getTransformedSelectedResult() != null);
    }

    private WritingAssistantResultModel getTransformedSelectedResult() {
        if (transformed == null) return null;
        return transformed.stream().filter(e -> e.isSelected).findFirst().orElse(null);
    }

    private WritingAssistantResultModel getImprovedSelectedResult() {
        if (improved == null) return null;
        return improved.stream().filter(e -> e.isSelected).findFirst().orElse(null);
    }

    @Override
    public void onSummarized(WritingAssistantResultModel summarized) {
        this.summarized = summarized;
    }

    public class AiPagerAdapter extends FragmentStatePagerAdapter {
        public AiPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            TongramAiFeatureModel feature = aiTabs.get(i);
            if (feature.id == Constants.AITypeId.TEMPLATE.id) {
                return new AITemplateFragment(transformInput, AiEnhanceDialog.this, transformed);
            } else if (feature.id == Constants.AITypeId.IMPROVE.id) {
                return new AIImproveFragment(improveInput, improved, AiEnhanceDialog.this);
            } else if (feature.id == Constants.AITypeId.SUMMARY.id) {
                return new AIUnreadSummaryFragment(unreadMessages, AiEnhanceDialog.this, summarized);
            } else {
                return new AITranslationFragment(translatedMessageRepository, languageModels, AiEnhanceDialog.this, translatedResult, input);
            }
        }

        @Override
        public int getCount() {
            return aiTabs.size();
        }
    }

    public interface Delegate {
        void onTranslatedApply(String text);

        void onTransformApply(String text);
    }

    public AiEnhanceDialog(Delegate delegate, Theme.ResourcesProvider resourcesProvider, List<TongramLanguageModel> languageModels, ITranslatedMessageRepository translatedMessageRepository, CharSequence input, ArrayList<MessageObject> unreadMessages) {
        this.delegate = delegate;
        this.resourcesProvider = resourcesProvider;
        this.translatedMessageRepository = translatedMessageRepository;
        this.languageModels = languageModels;
        this.unreadMessages = unreadMessages;
        this.input = input;
        this.transformInput = input;
        this.improveInput = input;
        aiTabs = new ArrayList<>();
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.TRANSLATION.id, R.drawable.ic_ai_translate, LocaleController.getString(R.string.PassportTranslation), false, true));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.TEMPLATE.id, R.drawable.ic_template, LocaleController.getString(R.string.Template), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.IMPROVE.id, R.drawable.ic_writing_assistant, LocaleController.getString(R.string.Improve), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.SUMMARY.id, R.drawable.ic_summary, LocaleController.getString(R.string.ChatSummary), false, false));

    }

    public synchronized static AiEnhanceDialog newInstance(Delegate delegate,
                                                           Theme.ResourcesProvider resourcesProvider,
                                                           List<TongramLanguageModel> languageModels,
                                                           ITranslatedMessageRepository translatedMessageRepository,
                                                           CharSequence input,
                                                           ArrayList<MessageObject> unreadMessages) {
        return new AiEnhanceDialog(delegate, resourcesProvider, languageModels, translatedMessageRepository, input, unreadMessages);
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
        tvApply = view.findViewById(R.id.tv_apply);
        tvApply.setTypeface(AndroidUtilities.bold());
        setTextApply(false);
        tvApply.setOnClickListener(v -> {
            final TongramAiFeatureModel selectedFeature = aiTabs.stream().filter(e -> e.isSelected).findFirst().orElse(null);
            if (selectedFeature != null) {
                if (selectedFeature.id == Constants.AITypeId.TEMPLATE.id) {
                    final WritingAssistantResultModel result = getTransformedSelectedResult();
                    if (result == null) {
                        return;
                    }
                    delegate.onTransformApply(result.message);
                } else if (selectedFeature.id == Constants.AITypeId.IMPROVE.id) {
                    final WritingAssistantResultModel result = getImprovedSelectedResult();
                    if (result == null) {
                        return;
                    }
                    delegate.onTransformApply(result.message);
                } else if (selectedFeature.id == Constants.AITypeId.SUMMARY.id) {
                    return;
                } else {
                    if (translatedResult == null || translatedResult.toString().isEmpty() || translatedResult.equals(LocaleController.getString(R.string.ThreeDot)) || translatedResult.equals(LocaleController.getString(R.string.Translating))) {
                        return;
                    }
                    delegate.onTranslatedApply(translatedResult.toString());
                }
            }
            dismiss();
        });

        ViewPager viewPager = view.findViewById(R.id.ai_view_pager);
        AiPagerAdapter viewPagerAdapter = new AiPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout = view.findViewById(R.id.ai_tab_layout);
        tabLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Theme.getColor(Theme.key_text_disable, resourcesProvider), Theme.getColor(Theme.key_text_enable, resourcesProvider));
        for (int i = 0; i < aiTabs.size(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            TongramAiFeatureModel feature = aiTabs.get(i);
            if (tab != null) {
                ConstraintLayout tabView = (ConstraintLayout) LayoutInflater.from(getContext())
                        .inflate(R.layout.custom_tab_item_layout, tabLayout, false);
                tab.setCustomView(setTabDrawable(feature, tabView));
            }
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final TongramAiFeatureModel selectedFeature = aiTabs.get(tab.getPosition());
                selectedFeature.isSelected = true;
                View v = tab.getCustomView();
                if (v != null) {
                    tab.setCustomView(setTabDrawable(selectedFeature, v));
                }
                checkCanApply(selectedFeature);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                final TongramAiFeatureModel unSelectedFeature = aiTabs.get(tab.getPosition());
                unSelectedFeature.isSelected = false;
                View v = tab.getCustomView();
                if (v != null) {
                    tab.setCustomView(setTabDrawable(unSelectedFeature, v));
                }
                checkCanApply(unSelectedFeature);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        return view;
    }

    private void checkCanApply(TongramAiFeatureModel model) {
        if (model.id == Constants.AITypeId.TEMPLATE.id) {
            tvApply.setVisibility(View.VISIBLE);
            setTextApply(getTransformedSelectedResult() != null);
        } else if (model.id == Constants.AITypeId.IMPROVE.id) {
            tvApply.setVisibility(View.VISIBLE);
            setTextApply(getImprovedSelectedResult() != null);
        } else if (model.id == Constants.AITypeId.SUMMARY.id) {
            tvApply.setVisibility(View.GONE);
            setTextApply(false);
        } else {
            tvApply.setVisibility(View.VISIBLE);
            setTextApply(translatedResult != null && !translatedResult.toString().isEmpty());
        }
    }

    private View setTabDrawable(TongramAiFeatureModel feature, View tabView) {
        TextView tabTitle = tabView.findViewById(R.id.tv_ai_feature);

        ImageView tabIcon = tabView.findViewById(R.id.iv_ai_feature);
        tabIcon.setImageResource(feature.iconResource);

        TextView tabComingSoon = tabView.findViewById(R.id.tv_coming_soon);

        if (feature.isComingSoon) {
            tabComingSoon.setVisibility(View.VISIBLE);
            tabComingSoon.setTextColor(Theme.getColor(Theme.key_coming_soon));

            tabComingSoon.setAlpha(0.5f);
        } else {
            tabComingSoon.setVisibility(View.GONE);
        }

        if (feature.isSelected) {
            tabTitle.setTextColor(Theme.getColor(Theme.key_view_pager_title_color));
            tabIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_view_pager_title_color), PorterDuff.Mode.SRC_IN));
            tabTitle.setTypeface(AndroidUtilities.bold());
        } else {
            tabTitle.setTextColor(Theme.getColor(Theme.key_profile_title));
            tabTitle.setTypeface(Typeface.DEFAULT);
            tabIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_icon_color), PorterDuff.Mode.SRC_IN));

        }
        tabTitle.setText(feature.title);

        setItemBackground(tabView, feature.isSelected);
        return tabView;
    }

    private void setItemBackground(View view, boolean isSelected) {
        GradientDrawable inner = new GradientDrawable();
        inner.setColor(Theme.getColor(isSelected ? Theme.key_icon_color : Theme.key_input_background));
        inner.setCornerRadius(AndroidUtilities.dp(10));

        view.setBackground(inner);
    }

    @Override
    public void setTextApply(boolean canApply) {
        if (canApply) {
            tvApply.setTextColor(Theme.getColor(Theme.key_text_enable, resourcesProvider));
        } else {
            tvApply.setTextColor(Theme.getColor(Theme.key_text_disable, resourcesProvider));
        }
    }

    @Override
    public void translated(CharSequence text) {
        translatedResult = text;
    }
}
