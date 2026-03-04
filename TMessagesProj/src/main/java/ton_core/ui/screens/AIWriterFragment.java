package ton_core.ui.screens;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.List;

import ton_core.shared.Constants;
import ton_core.ui.models.TongramAiFeatureModel;
import ton_core.ui.models.WritingAssistantResultModel;

public class AIWriterFragment extends BaseFragment implements AITemplateFragment.IAITemplateDelegate,
        AIImproveFragment.IAIImproveDelegate {

    private final List<TongramAiFeatureModel> aiTabs;
    public final LongSparseArray<List<WritingAssistantResultModel>> transformedList = new LongSparseArray<>();
    private final LongSparseArray<List<WritingAssistantResultModel>> improvedList = new LongSparseArray<>();

    public AIWriterFragment() {
        aiTabs = new ArrayList<>();

        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.IMPROVE.id, Constants.AIImproveId.FIX_GRAMMAR.id, R.drawable.ic_writing_assistant, LocaleController.getString(R.string.FixGrammar), false, true));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.IMPROVE.id, Constants.AIImproveId.MAKE_FORMAL.id, R.drawable.ic_make_formal, LocaleController.getString(R.string.MakeFormal), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.IMPROVE.id, Constants.AIImproveId.MAKE_FRIENDLY.id, R.drawable.ic_make_friendly, LocaleController.getString(R.string.MakeFriendly), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.IMPROVE.id, Constants.AIImproveId.MAKE_POLITE.id, R.drawable.ic_make_polite, LocaleController.getString(R.string.MakePolite), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.TEMPLATE.id, Constants.AITemplateId.SET_MEETING.id, R.drawable.ic_set_meeting, LocaleController.getString(R.string.SetMeeting), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.TEMPLATE.id, Constants.AITemplateId.WRITE_EMAIL.id, R.drawable.ic_write_email, LocaleController.getString(R.string.WriteEmail), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.TEMPLATE.id, Constants.AITemplateId.SAY_HI.id, R.drawable.ic_say_hi, LocaleController.getString(R.string.SayHi), false, false));
        aiTabs.add(new TongramAiFeatureModel(Constants.AITypeId.TEMPLATE.id, Constants.AITemplateId.SAY_THANKS.id, R.drawable.ic_thanks, LocaleController.getString(R.string.ThankForNote), false, false));

    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.ai_writer));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = LayoutInflater.from(context).inflate(R.layout.ai_writer_layout, null);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        if (getContext() instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) getContext();
            ViewPager viewPager = fragmentView.findViewById(R.id.ai_view_pager);

            AiPagerAdapter viewPagerAdapter = new AiPagerAdapter(activity.getSupportFragmentManager());

            viewPager.setAdapter(viewPagerAdapter);
            TabLayout tabLayout = fragmentView.findViewById(R.id.ai_tab_layout);
            tabLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabTextColors(Theme.getColor(Theme.key_text_disable), Theme.getColor(Theme.key_text_enable));
            for (int i = 0; i < aiTabs.size(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TongramAiFeatureModel feature = aiTabs.get(i);
                if (tab != null) {
                    CardView tabView = (CardView) LayoutInflater.from(getContext())
                            .inflate(R.layout.explored_category_item_layout, tabLayout, false);
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
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    final TongramAiFeatureModel unSelectedFeature = aiTabs.get(tab.getPosition());
                    unSelectedFeature.isSelected = false;
                    View v = tab.getCustomView();
                    if (v != null) {
                        tab.setCustomView(setTabDrawable(unSelectedFeature, v));
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        }

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(fragmentView, (v, insets) -> {
            v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        return fragmentView;
    }

    private View setTabDrawable(TongramAiFeatureModel feature, View tabView) {
        TextView tabTitle = tabView.findViewById(R.id.tv_category);
        tabTitle.setText(feature.title);

        CardView cardView = tabView.findViewById(R.id.cv_root);

        if (feature.isSelected) {
            tabTitle.setTypeface(AndroidUtilities.bold());
            tabTitle.setTextColor(Theme.getColor(Theme.key_text_like_theme));
            cardView.setCardBackgroundColor(Theme.getColor(Theme.keyAiFeatureBackgroundTabActive));
        } else {
            tabTitle.setTypeface(Typeface.DEFAULT);
            tabTitle.setTextColor(Theme.getColor(Theme.key_text_title_color));
            cardView.setCardBackgroundColor(Theme.getColor(Theme.keyAiFeatureBackgroundTabDefault));
        }

        return tabView;
    }

    @Override
    public void onImproved(List<WritingAssistantResultModel> results, int typeId) {
        if (improvedList.get(typeId) == null) {
            improvedList.put(typeId, results);
        } else {
            improvedList.replace(typeId, results);
        }
    }

    @Override
    public void onTransformed(List<WritingAssistantResultModel> results, int typeId) {
        if (transformedList.get(typeId) == null) {
            transformedList.put(typeId, results);
        } else {
            transformedList.replace(typeId, results);
        }
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
                return new AITemplateFragment("", AIWriterFragment.this, transformedList.get(feature.subId), feature);
            } else {
                return new AIImproveFragment("", improvedList.get(feature.subId), AIWriterFragment.this, feature);
            }
        }

        @Override
        public int getCount() {
            return aiTabs.size();
        }
    }

}
