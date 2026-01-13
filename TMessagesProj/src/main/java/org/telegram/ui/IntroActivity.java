/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Intro;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.Vector;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Components.BottomPagesView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.SimpleThemeDescription;
import org.telegram.ui.Components.voip.CellFlickerDrawable;

import java.util.ArrayList;

public class IntroActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private final static int ICON_WIDTH_DP = 200, ICON_HEIGHT_DP = 150;

    private final Object pagerHeaderTag = new Object(),
            pagerMessageTag = new Object();

    private int currentAccount = UserConfig.selectedAccount;

    private ViewPager viewPager;
    private BottomPagesView bottomPages;
    private TextView switchLanguageTextView;
    private TextView startMessagingButton;
    private FrameLayout frameLayout2;
    private FrameLayout frameContainerView;

    private RLottieDrawable darkThemeDrawable;

    private int lastPage = 0;
    private boolean justCreated = false;
    private boolean startPressed = false;
    private String[] titles;
    private String[] messages;
    private boolean dragging;

    private LocaleController.LocaleInfo localeInfo;

    private boolean destroyed;

    private boolean isOnLogout;

    private ImageView introImageView;
    private final int[] introDrawables = {
            R.drawable.ic_tongram,
            R.drawable.ic_cloud,
            R.drawable.ic_contact,
            R.drawable.ic_folder,
            R.drawable.ic_switch
    };

    @Override
    public boolean onFragmentCreate() {
        MessagesController.getGlobalMainSettings().edit().putLong("intro_crashed_time", System.currentTimeMillis()).apply();

        titles = new String[]{
                LocaleController.getString(R.string.Page1Title),
                "",
                "",
                "",
                "",
        };
        messages = new String[]{
                LocaleController.getString(R.string.Page1Message),
                LocaleController.getString(R.string.Page2Message),
                LocaleController.getString(R.string.Page3Message),
                LocaleController.getString(R.string.Page4Message),
                LocaleController.getString(R.string.Page5Message),
        };
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);

//        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
//        if (!preferences.contains("theme_name") && !preferences.contains("isDark")) {
//            Theme.ThemeInfo nightTheme = Theme.getTheme("Night");
//            if (nightTheme != null) {
//                Theme.applyTheme(nightTheme);
//            }
//        } else {
//            Theme.applyDayNightThemeMaybe(true);
//        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);

        RLottieImageView themeIconView = new RLottieImageView(context);
        FrameLayout themeFrameLayout = new FrameLayout(context);
        themeFrameLayout.addView(themeIconView, LayoutHelper.createFrame(28, 28, Gravity.CENTER));

        int themeMargin = 4;
        frameContainerView = new FrameLayout(context) {

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);

                int width = right - left;
                int height = bottom - top;
                int oneFourth = height / 4;

                viewPager.layout(0, 0, viewPager.getMeasuredWidth(), viewPager.getMeasuredHeight());

                int yIcon = (oneFourth * 3 - AndroidUtilities.dp(275)) / 2;
                frameLayout2.layout(0, yIcon, frameLayout2.getMeasuredWidth(), yIcon + frameLayout2.getMeasuredHeight());

                int yButton = height - startMessagingButton.getMeasuredHeight() - AndroidUtilities.dp(48);
                int xButton = (width - startMessagingButton.getMeasuredWidth()) / 2;
                startMessagingButton.layout(xButton, yButton, xButton + startMessagingButton.getMeasuredWidth(), yButton + startMessagingButton.getMeasuredHeight());

                int yIndicators = yButton - bottomPages.getMeasuredHeight() - AndroidUtilities.dp(30);
                int xIndicators = (width - bottomPages.getMeasuredWidth()) / 2;
                bottomPages.layout(xIndicators, yIndicators, xIndicators + bottomPages.getMeasuredWidth(), yIndicators + bottomPages.getMeasuredHeight());

                int yLang = yIndicators - AndroidUtilities.dp(12);
                int xLang = (width - switchLanguageTextView.getMeasuredWidth()) / 2;
                switchLanguageTextView.layout(xLang, yLang - switchLanguageTextView.getMeasuredHeight(), xLang + switchLanguageTextView.getMeasuredWidth(), yLang);

                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) themeFrameLayout.getLayoutParams();
                int newTopMargin = AndroidUtilities.dp(themeMargin) + (AndroidUtilities.isTablet() ? 0 : AndroidUtilities.statusBarHeight);
                if (marginLayoutParams.topMargin != newTopMargin) {
                    marginLayoutParams.topMargin = newTopMargin;
                    themeFrameLayout.requestLayout();
                }
            }
        };
        scrollView.addView(frameContainerView, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));

        darkThemeDrawable = new RLottieDrawable(R.raw.sun, String.valueOf(R.raw.sun), AndroidUtilities.dp(28), AndroidUtilities.dp(28), true, null);
        darkThemeDrawable.setPlayInDirectionOfCustomEndFrame(true);
        darkThemeDrawable.beginApplyLayerColors();
        darkThemeDrawable.commitApplyLayerColors();

        darkThemeDrawable.setCustomEndFrame(Theme.getCurrentTheme().isDark() ? darkThemeDrawable.getFramesCount() - 1 : 0);
        darkThemeDrawable.setCurrentFrame(Theme.getCurrentTheme().isDark() ? darkThemeDrawable.getFramesCount() - 1 : 0, false);
        themeIconView.setContentDescription(LocaleController.getString(Theme.getCurrentTheme().isDark() ? R.string.AccDescrSwitchToDayTheme : R.string.AccDescrSwitchToNightTheme));

        themeIconView.setAnimation(darkThemeDrawable);
        themeFrameLayout.setOnClickListener(v -> {
            if (DrawerProfileCell.switchingTheme) return;
            DrawerProfileCell.switchingTheme = true;

            // TODO: Generify this part, currently it's a clone of another theme switch toggle
            String dayThemeName = "Blue";
            String nightThemeName = "Night";

            Theme.ThemeInfo themeInfo;
            boolean toDark;
            if (toDark = !Theme.isCurrentThemeDark()) {
                themeInfo = Theme.getTheme(nightThemeName);
            } else {
                themeInfo = Theme.getTheme(dayThemeName);
            }

            Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_NONE;
            Theme.saveAutoNightThemeConfig();
            Theme.cancelAutoNightThemeCallbacks();

            darkThemeDrawable.setCustomEndFrame(toDark ? darkThemeDrawable.getFramesCount() - 1 : 0);
            themeIconView.playAnimation();

            int[] pos = new int[2];
            themeIconView.getLocationInWindow(pos);
            pos[0] += themeIconView.getMeasuredWidth() / 2;
            pos[1] += themeIconView.getMeasuredHeight() / 2;
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, themeInfo, false, pos, -1, toDark, themeIconView);
            themeIconView.setContentDescription(LocaleController.getString(toDark ? R.string.AccDescrSwitchToDayTheme : R.string.AccDescrSwitchToNightTheme));
        });

        frameLayout2 = new FrameLayout(context);
        frameContainerView.addView(frameLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 78, 0, 0));

        introImageView = new ImageView(context);
        introImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        frameLayout2.addView(introImageView, LayoutHelper.createFrame(ICON_WIDTH_DP, ICON_HEIGHT_DP, Gravity.CENTER));
        introImageView.setImageResource(introDrawables[0]);

        viewPager = new ViewPager(context);
        viewPager.setAdapter(new IntroAdapter());
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);
        frameContainerView.addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomPages.setPageOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int i) {
                if (introImageView != null) {
                    introImageView.setImageResource(introDrawables[i]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_DRAGGING) {
                    dragging = true;
                } else if (i == ViewPager.SCROLL_STATE_IDLE || i == ViewPager.SCROLL_STATE_SETTLING) {
                    if (dragging) {
                        dragging = false;
                    }
                    if (lastPage != viewPager.getCurrentItem()) {
                        lastPage = viewPager.getCurrentItem();
                    }
                }
            }
        });

        startMessagingButton = new TextView(context) {
            CellFlickerDrawable cellFlickerDrawable;

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (cellFlickerDrawable == null) {
                    cellFlickerDrawable = new CellFlickerDrawable();
                    cellFlickerDrawable.drawFrame = false;
                    cellFlickerDrawable.repeatProgress = 2f;
                }
                cellFlickerDrawable.setParentWidth(getMeasuredWidth());
                AndroidUtilities.rectTmp.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                cellFlickerDrawable.draw(canvas, AndroidUtilities.rectTmp, AndroidUtilities.dp(4), null);
                invalidate();
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int size = MeasureSpec.getSize(widthMeasureSpec);
                if (size > AndroidUtilities.dp(260)) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(320), MeasureSpec.EXACTLY), heightMeasureSpec);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        };
        startMessagingButton.setText(LocaleController.getString(R.string.StartMessaging));
        startMessagingButton.setGravity(Gravity.CENTER);
        startMessagingButton.setTypeface(AndroidUtilities.bold());
        startMessagingButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        startMessagingButton.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        frameContainerView.addView(startMessagingButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 16, 0, 16, 76));
        startMessagingButton.setOnClickListener(view -> {
            if (startPressed) {
                return;
            }
            startPressed = true;

            presentFragment(new LoginActivity().setIntroView(frameContainerView, startMessagingButton), true);
            destroyed = true;
        });

        bottomPages = new BottomPagesView(context, viewPager, 5);
        frameContainerView.addView(bottomPages, LayoutHelper.createFrame(66, 5, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, ICON_HEIGHT_DP + 200, 0, 0));

        switchLanguageTextView = new TextView(context);
        switchLanguageTextView.setGravity(Gravity.CENTER);
        switchLanguageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        frameContainerView.addView(switchLanguageTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 30, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 20));
        switchLanguageTextView.setOnClickListener(v -> {
            if (startPressed || localeInfo == null) {
                return;
            }
            startPressed = true;

            AlertDialog loaderDialog = new AlertDialog(v.getContext(), AlertDialog.ALERT_TYPE_SPINNER);
            loaderDialog.setCanCancel(false);
            loaderDialog.showDelayed(1000);

            NotificationCenter.getGlobalInstance().addObserver(new NotificationCenter.NotificationCenterDelegate() {
                @Override
                public void didReceivedNotification(int id, int account, Object... args) {
                    if (id == NotificationCenter.reloadInterface) {
                        loaderDialog.dismiss();

                        NotificationCenter.getGlobalInstance().removeObserver(this, id);
                        AndroidUtilities.runOnUIThread(() -> {
                            presentFragment(new LoginActivity().setIntroView(frameContainerView, startMessagingButton), true);
                            destroyed = true;
                        }, 100);
                    }
                }
            }, NotificationCenter.reloadInterface);
            LocaleController.getInstance().applyLanguage(localeInfo, true, false, currentAccount);
        });

        frameContainerView.addView(themeFrameLayout, LayoutHelper.createFrame(64, 64, Gravity.TOP | Gravity.RIGHT, 0, themeMargin, themeMargin, 0));

        fragmentView = scrollView;

        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.suggestedLangpack);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.configLoaded);
        ConnectionsManager.getInstance(currentAccount).updateDcSettings();
        LocaleController.getInstance().loadRemoteLanguages(currentAccount);
        checkContinueText();
        justCreated = true;

        updateColors(false);

        return fragmentView;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onResume() {
        super.onResume();
        if (justCreated) {
            if (LocaleController.isRTL) {
                viewPager.setCurrentItem(6);
                lastPage = 6;
            } else {
                viewPager.setCurrentItem(0);
                lastPage = 0;
            }
            justCreated = false;
        }
        if (!AndroidUtilities.isTablet()) {
            Activity activity = getParentActivity();
            if (activity != null) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!AndroidUtilities.isTablet()) {
            Activity activity = getParentActivity();
            if (activity != null) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
    }

    @Override
    public boolean hasForceLightStatusBar() {
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        destroyed = true;
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.configLoaded);
        MessagesController.getGlobalMainSettings().edit().putLong("intro_crashed_time", 0).apply();
    }

    private void checkContinueText() {
        LocaleController.LocaleInfo englishInfo = null;
        LocaleController.LocaleInfo systemInfo = null;
        LocaleController.LocaleInfo currentLocaleInfo = LocaleController.getInstance().getCurrentLocaleInfo();
        String systemLang = MessagesController.getInstance(currentAccount).suggestedLangCode;
        if (systemLang == null || systemLang.equals("en") && LocaleController.getInstance().getSystemDefaultLocale().getLanguage() != null && !LocaleController.getInstance().getSystemDefaultLocale().getLanguage().equals("en")) {
            systemLang = LocaleController.getInstance().getSystemDefaultLocale().getLanguage();
            if (systemLang == null) {
                systemLang = "en";
            }
        }

        String arg = systemLang.contains("-") ? systemLang.split("-")[0] : systemLang;
        String alias = LocaleController.getLocaleAlias(arg);
        for (int a = 0; a < LocaleController.getInstance().languages.size(); a++) {
            LocaleController.LocaleInfo info = LocaleController.getInstance().languages.get(a);
            if (info.shortName.equals("en")) {
                englishInfo = info;
            }
            if (info.shortName.replace("_", "-").equals(systemLang) || info.shortName.equals(arg) || info.shortName.equals(alias)) {
                systemInfo = info;
            }
            if (englishInfo != null && systemInfo != null) {
                break;
            }
        }
        if (englishInfo == null || systemInfo == null || englishInfo == systemInfo) {
            return;
        }
        TLRPC.TL_langpack_getStrings req = new TLRPC.TL_langpack_getStrings();
        if (systemInfo != currentLocaleInfo) {
            req.lang_code = systemInfo.getLangCode();
            localeInfo = systemInfo;
        } else {
            req.lang_code = englishInfo.getLangCode();
            localeInfo = englishInfo;
        }
        req.keys.add("ContinueOnThisLanguage");
        String finalSystemLang = systemLang;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response instanceof Vector) {
                Vector vector = (Vector) response;
                if (vector.objects.isEmpty()) {
                    return;
                }
                final TLRPC.LangPackString string = (TLRPC.LangPackString) vector.objects.get(0);
                if (string instanceof TLRPC.TL_langPackString) {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (!destroyed) {
                            switchLanguageTextView.setText(string.value);
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            preferences.edit().putString("language_showed2", finalSystemLang.toLowerCase()).apply();
                        }
                    });
                }
            }
        }, ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.suggestedLangpack || id == NotificationCenter.configLoaded) {
            checkContinueText();
        }
    }

    public IntroActivity setOnLogout() {
        isOnLogout = true;
        return this;
    }

    @Override
    public AnimatorSet onCustomTransitionAnimation(boolean isOpen, Runnable callback) {
        if (isOnLogout) {
            AnimatorSet set = new AnimatorSet().setDuration(50);
            set.playTogether(ValueAnimator.ofFloat());
            return set;
        }
        return null;
    }

    private class IntroAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return titles.length;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TextView headerTextView = new TextView(container.getContext());
            headerTextView.setTag(pagerHeaderTag);
            TextView messageTextView = new TextView(container.getContext());
            messageTextView.setTag(pagerMessageTag);

            FrameLayout frameLayout = new FrameLayout(container.getContext()) {
                @Override
                protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                    int oneFourth = (bottom - top) / 4;
                    int y = (oneFourth * 3 - AndroidUtilities.dp(275)) / 2;
                    y += AndroidUtilities.dp(ICON_HEIGHT_DP);
                    y += AndroidUtilities.dp(16);
                    int x = AndroidUtilities.dp(18);
                    headerTextView.layout(x, y, x + headerTextView.getMeasuredWidth(), y + headerTextView.getMeasuredHeight());

                    y += headerTextView.getTextSize();
                    y += AndroidUtilities.dp(16);
                    x = AndroidUtilities.dp(16);
                    messageTextView.layout(x, y, x + messageTextView.getMeasuredWidth(), y + messageTextView.getMeasuredHeight());
                }
            };

            headerTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
            headerTextView.setGravity(Gravity.CENTER);
            frameLayout.addView(headerTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 18, 244, 18, 0));

            messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            messageTextView.setGravity(Gravity.CENTER);
            frameLayout.addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 16, 286, 16, 0));

            container.addView(frameLayout, 0);

            headerTextView.setText(titles[position]);
            messageTextView.setText(AndroidUtilities.replaceTags(messages[position]));

            return frameLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.setPrimaryItem(container, position, object);
            bottomPages.setCurrentPage(position);
        }

        @Override
        public boolean isViewFromObject(View view, @NonNull Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        return SimpleThemeDescription.createThemeDescriptions(() -> updateColors(true), Theme.key_windowBackgroundWhite,
                Theme.key_windowBackgroundWhiteBlueText4, Theme.key_chats_actionBackground, Theme.key_chats_actionPressedBackground,
                Theme.key_featuredStickers_buttonText, Theme.key_windowBackgroundWhiteBlackText, Theme.key_windowBackgroundWhiteGrayText3
        );
    }

    private void updateColors(boolean fromTheme) {
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        switchLanguageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        startMessagingButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        startMessagingButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_changephoneinfo_image2), Theme.getColor(Theme.key_chats_actionPressedBackground)));
        darkThemeDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_changephoneinfo_image2), PorterDuff.Mode.SRC_IN));
        bottomPages.invalidate();
        if (fromTheme) {
            for (int i = 0; i < viewPager.getChildCount(); i++) {
                View ch = viewPager.getChildAt(i);
                TextView headerTextView = ch.findViewWithTag(pagerHeaderTag);
                headerTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                TextView messageTextView = ch.findViewWithTag(pagerMessageTag);
                messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            }
        } else Intro.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
    }

    @Override
    public boolean isLightStatusBar() {
        int color = Theme.getColor(Theme.key_windowBackgroundWhite, null, true);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }
}
