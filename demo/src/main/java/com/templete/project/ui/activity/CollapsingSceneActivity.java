package com.templete.project.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gyf.immersionbar.ImmersionBar;
import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseCollapsingActivity;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.util.ScreenUtil;
import com.templete.project.R;
import com.templete.project.databinding.ActivityCollapsingSceneHostBinding;
import com.templete.project.ui.fragment.CollapsingTabListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Design 折叠场景宿主。
 * <p>
 * 顶栏：CollapsingToolbar 直接子 View 单个 pin {@code Toolbar}，运行时高度 =
 * statusBar + title_bar_height、paddingTop = statusBar；{@code titleEnabled=false}。
 * 不要用 ViewGroup/include 包 pin（收起高度会变成 0）；不要多个 pin 叠顶。
 * <p>
 * 继承 {@link BaseCollapsingActivity}：锁定头部滚动、一键折叠/展开。
 */
public class CollapsingSceneActivity extends BaseCollapsingActivity<ActivityCollapsingSceneHostBinding> {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_LAYOUT = "extra_layout";

    private static final int BACK_SIZE_DP = 28;
    /** 返回 12+28，再留一点空隙给标题 */
    private static final int BACK_CLEARANCE_DP = 48;

    private int layoutRes;
    private int statusBarHeight;
    private boolean contentBound;
    private boolean shopBound;
    private boolean hotelPriceBound;
    private boolean titleFadeBound;

    public static void start(Context context, String title, int layoutRes) {
        Intent intent = new Intent(context, CollapsingSceneActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_LAYOUT, layoutRes);
        context.startActivity(intent);
    }

    @Override
    protected ActivityCollapsingSceneHostBinding viewBinding() {
        return ActivityCollapsingSceneHostBinding.inflate(getLayoutInflater());
    }

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_NONE;
    }

    @Override
    public boolean darkStatusBarFont() {
        return false;
    }

    @Override
    public void inits() {
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(false)
                .fitsSystemWindows(false)
                .keyboardEnable(true)
                .init();
    }

    @Override
    public void initView() {
        layoutRes = getIntent().getIntExtra(EXTRA_LAYOUT, 0);
        if (layoutRes == 0) {
            finish();
            return;
        }
        View scene = getLayoutInflater().inflate(layoutRes, mViewBinding.sceneRoot, true);
        mViewBinding.btnBack.setOnClickListener(v -> finish());

        AppBarLayout appBar = scene.findViewById(R.id.appBar);
        if (appBar != null) {
            bindCollapsingAppBar(appBar);
        }
        bindCollapsingControls();

        statusBarHeight = Math.max(0, ScreenUtil.getStatusBarSize());
        setupChrome(scene, statusBarHeight);
        if (!contentBound) {
            bindImages(scene);
            bindPagerOrList(scene);
            bindBodies(scene);
            contentBound = true;
        }

        ViewCompat.setOnApplyWindowInsetsListener(mViewBinding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            int top = bars.top > 0 ? bars.top : Math.max(0, ScreenUtil.getStatusBarSize());
            if (top > 0 && top != statusBarHeight) {
                statusBarHeight = top;
                setupChrome(scene, statusBarHeight);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(mViewBinding.getRoot());
    }

    /** Demo：展开锁头 / 折叠锁头 / 一键折叠展开；中间钮展示当前状态 */
    private void bindCollapsingControls() {
        refreshCollapsingControlLabels();
        AppBarLayout appBar = getCollapsingAppBar();
        if (appBar != null) {
            appBar.addOnOffsetChangedListener((abl, offset) -> refreshCollapsingControlLabels());
        }
        mViewBinding.btnLockExpanded.setOnClickListener(v -> {
            if (!isCollapsingScrollEnabled()) {
                unlockCollapsingHeader();
                refreshCollapsingControlLabels();
                return;
            }
            lockCollapsingHeaderExpanded(true);
            v.postDelayed(this::refreshCollapsingControlLabels, 320);
        });
        mViewBinding.btnLockCollapsed.setOnClickListener(v -> {
            if (!isCollapsingScrollEnabled()) {
                unlockCollapsingHeader();
                refreshCollapsingControlLabels();
                return;
            }
            lockCollapsingHeaderCollapsed(true);
            v.postDelayed(this::refreshCollapsingControlLabels, 320);
        });
        mViewBinding.btnToggleExpand.setOnClickListener(v -> {
            if (!isCollapsingScrollEnabled()) {
                return;
            }
            if (isCollapsingExpanded()) {
                collapseCollapsingHeader(true);
            } else {
                expandCollapsingHeader(true);
            }
            v.postDelayed(this::refreshCollapsingControlLabels, 320);
        });
    }

    private void refreshCollapsingControlLabels() {
        boolean scrollOn = isCollapsingScrollEnabled();
        CollapsingHeaderState state = getCollapsingHeaderState();
        String stateLabel;
        switch (state) {
            case EXPANDED:
                stateLabel = "展开";
                break;
            case COLLAPSED:
                stateLabel = "收起";
                break;
            default:
                stateLabel = "过渡";
                break;
        }
        if (scrollOn) {
            mViewBinding.btnLockExpanded.setText("展开锁头");
            mViewBinding.btnLockCollapsed.setText("折叠锁头");
            mViewBinding.btnToggleExpand.setEnabled(true);
            mViewBinding.btnToggleExpand.setAlpha(1f);
            mViewBinding.btnToggleExpand.setText(stateLabel);
        } else {
            mViewBinding.btnLockExpanded.setText("解锁头部");
            mViewBinding.btnLockCollapsed.setText("解锁头部");
            mViewBinding.btnToggleExpand.setEnabled(false);
            mViewBinding.btnToggleExpand.setAlpha(0.4f);
            mViewBinding.btnToggleExpand.setText(stateLabel);
        }
    }

    private void setupChrome(View scene, int statusBar) {
        int titleH = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
        Toolbar toolbar = scene.findViewById(R.id.toolbar);
        if (toolbar != null && toolbar.getVisibility() == View.VISIBLE) {
            toolbar.setNavigationIcon(null);
            // 单个 pin Toolbar：总高 = 状态栏 + title_bar_height，标题在 padding 下方，不会进状态栏
            ViewGroup.LayoutParams tlp = toolbar.getLayoutParams();
            tlp.height = titleH + statusBar;
            toolbar.setLayoutParams(tlp);
            toolbar.setPadding(0, statusBar, toolbar.getPaddingRight(), 0);
            toolbar.setContentInsetsRelative(dp(BACK_CLEARANCE_DP), dp(16));
            // 与 TitleBar 同字号；折叠场景展开时标题由 bindPinnedTitleOnCollapse 控制，这里不强制写回
            if (!titleFadeBound) {
                CharSequence title = toolbar.getTitle();
                if (title == null || title.length() == 0) {
                    setToolbarTitle(toolbar, fallbackTitle());
                } else {
                    applyToolbarTitleStyle(toolbar);
                }
            } else {
                applyToolbarTitleStyle(toolbar);
            }
        }
        CollapsingToolbarLayout collapsing = scene.findViewById(R.id.collapsingToolbar);
        if (collapsing != null) {
            // 必须关掉，否则折叠标题按整段高度居中，会顶进状态栏
            collapsing.setTitleEnabled(false);
            collapsing.setContentScrimColor(scrimColor());
        }
        ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) mViewBinding.btnBack.getLayoutParams();
        lp.topMargin = statusBar + Math.max(0, (titleH - dp(BACK_SIZE_DP)) / 2);
        mViewBinding.btnBack.setLayoutParams(lp);
        mViewBinding.btnBack.bringToFront();

        bindPinnedTitleOnCollapse(scene);
        bindShopSearch(scene, statusBar);
        bindHotelPriceTransition(scene);
    }

    /**
     * 酒店价条：头图折叠过程中价格做透明度 + 轻微缩放过渡。
     */
    private void bindHotelPriceTransition(View scene) {
        View priceBar = scene.findViewById(R.id.hotelPriceBar);
        View priceText = scene.findViewById(R.id.tvHotelPrice);
        AppBarLayout appBar = scene.findViewById(R.id.appBar);
        if (priceBar == null || appBar == null || hotelPriceBound) {
            return;
        }
        hotelPriceBound = true;
        final float[] last = {-1f};
        appBar.addOnOffsetChangedListener((abl, offset) -> {
            int range = abl.getTotalScrollRange();
            if (range <= 0) {
                return;
            }
            float progress = Math.max(0f, Math.min(1f, Math.abs(offset) / (float) range));
            float ease = progress * progress * (3f - 2f * progress);
            // 幅度收小，避免缩放跳变；只做很轻的透明度过渡
            float alpha = 0.88f + 0.12f * ease;
            if (Math.abs(alpha - last[0]) < 0.008f) {
                return;
            }
            last[0] = alpha;
            priceBar.setAlpha(alpha);
            if (priceText != null) {
                float scale = 1f + 0.02f * ease;
                priceText.setScaleX(scale);
                priceText.setScaleY(scale);
                priceText.setPivotX(0f);
                priceText.setPivotY(Math.max(1, priceText.getHeight()) / 2f);
            }
        });
    }

    /**
     * 展开：只留封面区文案；收起：再显示 Toolbar 标题，避免双标题。
     * 无封面大标题的场景（如笔记）展开时仅返回键，收起后出现标题。
     */
    private void bindPinnedTitleOnCollapse(View scene) {
        Toolbar toolbar = scene.findViewById(R.id.toolbar);
        AppBarLayout appBar = scene.findViewById(R.id.appBar);
        CollapsingToolbarLayout collapsing = scene.findViewById(R.id.collapsingToolbar);
        if (toolbar == null || appBar == null || collapsing == null
                || toolbar.getVisibility() != View.VISIBLE) {
            return;
        }
        final View expanded = scene.findViewById(R.id.tvExpandedTitle);
        final CharSequence pinnedTitle = toolbar.getTitle() != null && toolbar.getTitle().length() > 0
                ? toolbar.getTitle()
                : fallbackTitle();
        setToolbarTitle(toolbar, "");
        if (titleFadeBound) {
            return;
        }
        titleFadeBound = true;
        appBar.addOnOffsetChangedListener((abl, offset) -> {
            int range = abl.getTotalScrollRange();
            float progress = range <= 0 ? 0f : Math.abs(offset) / (float) range;
            float pinAlpha = Math.max(0f, Math.min(1f, (progress - 0.55f) / 0.45f));
            if (expanded != null) {
                expanded.setAlpha(1f - pinAlpha);
            }
            CharSequence cur = toolbar.getTitle();
            if (pinAlpha > 0.85f) {
                if (cur == null || cur.length() == 0) {
                    setToolbarTitle(toolbar, pinnedTitle);
                }
            } else if (cur != null && cur.length() > 0) {
                setToolbarTitle(toolbar, "");
            }
        });
    }

    private void setToolbarTitle(Toolbar toolbar, CharSequence title) {
        toolbar.setTitle(title);
        applyToolbarTitleStyle(toolbar);
    }

    /** Toolbar 默认字号偏大，对齐项目 TitleBar 的 {@code tv_title_size}。 */
    private void applyToolbarTitleStyle(Toolbar toolbar) {
        float sizePx = getResources().getDimension(R.dimen.tv_title_size);
        toolbar.post(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx);
                    tv.setIncludeFontPadding(false);
                    tv.setSingleLine(true);
                }
            }
        });
    }

    /**
     * 店铺搜索：展开为假输入框；点击后折叠，顶部真 EditText 可输入。
     * 吸顶浮层跟滚动进度连续淡入。
     */
    private void bindShopSearch(View scene, int statusBar) {
        View searchBar = scene.findViewById(R.id.searchBar);
        View sticky = scene.findViewById(R.id.searchBarSticky);
        View bannerMeta = scene.findViewById(R.id.shopBannerMeta);
        TextView fakeSearch = scene.findViewById(R.id.tvSearchFake);
        EditText realSearch = scene.findViewById(R.id.etSearchReal);
        AppBarLayout appBar = scene.findViewById(R.id.appBar);
        if (searchBar == null || sticky == null || appBar == null
                || fakeSearch == null || realSearch == null) {
            return;
        }
        final int inset = Math.max(0, statusBar);
        final int row = dp(52);
        final int side = dp(12);
        final int backClearance = dp(BACK_CLEARANCE_DP);
        ViewGroup.LayoutParams slp = sticky.getLayoutParams();
        slp.height = row + inset;
        sticky.setLayoutParams(slp);
        sticky.setPadding(backClearance, inset, side, sticky.getPaddingBottom());
        searchBar.setPadding(side, 0, side, 0);

        sticky.animate().cancel();
        sticky.setAlpha(0f);
        sticky.setVisibility(View.INVISIBLE);
        sticky.bringToFront();
        mViewBinding.btnBack.bringToFront();

        if (shopBound) {
            return;
        }
        shopBound = true;
        final boolean[] lightBar = {false};
        final boolean[] pendingFocus = {false};
        final float[] lastStick = {-1f};
        final float[] lastMeta = {-1f};

        // 真假输入框文案同步
        syncShopSearchText(fakeSearch, realSearch);
        realSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                syncShopSearchText(fakeSearch, realSearch);
            }
        });

        // 假搜索：点击 → 折叠 → 真输入框获焦
        View.OnClickListener openRealSearch = v -> {
            if (!isCollapsingScrollEnabled()) {
                setCollapsingScrollEnabled(true);
            }
            pendingFocus[0] = true;
            appBar.setExpanded(false, true);
            // 已吸顶时直接获焦
            if (lastStick[0] >= 0.92f) {
                pendingFocus[0] = false;
                focusShopSearch(realSearch);
            }
        };
        fakeSearch.setOnClickListener(openRealSearch);
        searchBar.setOnClickListener(openRealSearch);

        realSearch.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(realSearch);
                realSearch.clearFocus();
                return true;
            }
            return false;
        });

        appBar.addOnOffsetChangedListener((abl, offset) -> {
            int range = abl.getTotalScrollRange();
            if (range <= 0) {
                return;
            }
            int abs = Math.abs(offset);
            float progress = Math.max(0f, Math.min(1f, abs / (float) range));

            if (bannerMeta != null) {
                float t = Math.max(0f, (progress - 0.2f) / 0.8f);
                float ease = t * t * (3f - 2f * t);
                float metaAlpha = 1f - ease;
                if (Math.abs(metaAlpha - lastMeta[0]) >= 0.012f) {
                    lastMeta[0] = metaAlpha;
                    bannerMeta.setAlpha(metaAlpha);
                    bannerMeta.setTranslationY(-dp(4) * ease);
                }
            }

            int bar = Math.max(dp(24), statusBarHeight);
            float raw = range <= bar ? progress
                    : Math.max(0f, Math.min(1f, (abs - (range - bar)) / (float) bar));
            float stick = raw * raw * (3f - 2f * raw);
            if (Math.abs(stick - lastStick[0]) >= 0.01f) {
                lastStick[0] = stick;
                sticky.setAlpha(stick);
                searchBar.setAlpha(1f - stick);
                if (stick < 0.02f) {
                    if (sticky.getVisibility() != View.INVISIBLE) {
                        sticky.setVisibility(View.INVISIBLE);
                    }
                    // 展开回来：收起键盘，真输入失焦
                    if (realSearch.hasFocus()) {
                        hideKeyboard(realSearch);
                        realSearch.clearFocus();
                    }
                    pendingFocus[0] = false;
                } else if (sticky.getVisibility() != View.VISIBLE) {
                    sticky.setVisibility(View.VISIBLE);
                    sticky.bringToFront();
                    mViewBinding.btnBack.bringToFront();
                }
                if (pendingFocus[0] && stick >= 0.92f) {
                    pendingFocus[0] = false;
                    focusShopSearch(realSearch);
                }
            }

            boolean wantLight = lightBar[0] ? stick >= 0.72f : stick >= 0.92f;
            if (wantLight != lightBar[0]) {
                lightBar[0] = wantLight;
                applyShopStatusBar(wantLight);
            }
        });
    }

    private void focusShopSearch(EditText realSearch) {
        realSearch.post(() -> {
            realSearch.setFocusable(true);
            realSearch.setFocusableInTouchMode(true);
            realSearch.requestFocus();
            // 光标放到末尾，保留已同步内容
            CharSequence text = realSearch.getText();
            if (text != null) {
                realSearch.setSelection(text.length());
            }
            showKeyboard(realSearch);
        });
    }

    /** 真输入有字 → 假框显示同文；空则恢复占位「搜本店商品」 */
    private void syncShopSearchText(TextView fakeSearch, EditText realSearch) {
        CharSequence text = realSearch.getText();
        if (text != null && text.length() > 0) {
            fakeSearch.setText(text);
            fakeSearch.setTextColor(0xFF333333);
        } else {
            fakeSearch.setText("搜本店商品");
            fakeSearch.setTextColor(0xFF999999);
        }
    }

    /**
     * 吸顶白底 → 深色图标；展开 → 透明 + 浅色图标。
     * ImmersionBar + 系统 flag 双保险（仅 Genymotion 上 Controller 不够稳）。
     */
    private void applyShopStatusBar(boolean stuck) {
        if (stuck) {
            ImmersionBar.with(this)
                    .statusBarColor(android.R.color.white)
                    .statusBarDarkFont(true, 0.2f)
                    .fitsSystemWindows(false)
                    .keyboardEnable(false)
                    .init();
        } else {
            ImmersionBar.with(this)
                    .transparentStatusBar()
                    .statusBarDarkFont(false)
                    .fitsSystemWindows(false)
                    .keyboardEnable(false)
                    .init();
        }
        Window window = getWindow();
        window.setStatusBarColor(stuck ? 0xFFFFFFFF : 0x00000000);
        View decor = window.getDecorView();
        WindowCompat.getInsetsController(window, decor).setAppearanceLightStatusBars(stuck);
        int flags = decor.getSystemUiVisibility();
        if (stuck) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    private void bindPagerOrList(View scene) {
        ViewPager2 vp = scene.findViewById(R.id.viewPager);
        TabLayout tabs = scene.findViewById(R.id.tabLayout);
        if (vp != null && tabs != null) {
            final String[] titles = tabTitles();
            final String prefix = listPrefix();
            vp.setAdapter(new FragmentStateAdapter(this) {
                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    return CollapsingTabListFragment.newInstance(prefix, titles[position]);
                }

                @Override
                public int getItemCount() {
                    return titles.length;
                }
            });
            new TabLayoutMediator(tabs, vp, (tab, pos) -> tab.setText(titles[pos])).attach();
            return;
        }
        RecyclerView rv = scene.findViewById(R.id.recyclerView);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            DemoAdapter adapter = new DemoAdapter(this);
            rv.setAdapter(adapter);
            List<String> list = new ArrayList<>();
            String prefix = listPrefix();
            int count = layoutRes == R.layout.collapsing_scene_live ? 40 : 30;
            for (int i = 0; i < count; i++) {
                list.add(prefix + (i + 1));
            }
            adapter.setList(list);
        }
    }

    private void bindBodies(View scene) {
        TextView article = scene.findViewById(R.id.tvArticleBody);
        if (article != null) {
            article.setText(longText("资讯正文段落", 28));
        }
        TextView hotel = scene.findViewById(R.id.tvHotelBody);
        if (hotel != null) {
            hotel.setText(longText("酒店设施与周边介绍", 22));
        }
    }

    private void bindImages(View scene) {
        ImageView cover = scene.findViewById(R.id.ivCover);
        if (cover != null) {
            Glide.with(this).load(coverUrl()).centerCrop()
                    .placeholder(R.drawable.demo).error(R.drawable.demo).into(cover);
        }
        ImageView avatar = scene.findViewById(R.id.ivAvatar);
        if (avatar != null) {
            Glide.with(this).load(avatarUrl()).transform(new CircleCrop())
                    .placeholder(R.drawable.demo).error(R.drawable.demo).into(avatar);
        }
    }

    private String[] tabTitles() {
        if (layoutRes == R.layout.collapsing_scene_creator) {
            return new String[]{"作品", "喜欢", "收藏"};
        }
        return new String[]{"热门", "专辑", "视频", "简介"};
    }

    private String fallbackTitle() {
        if (layoutRes == R.layout.collapsing_scene_profile) return "阿木的主页";
        if (layoutRes == R.layout.collapsing_scene_music) return "歌手";
        if (layoutRes == R.layout.collapsing_scene_creator) return "作者主页";
        if (layoutRes == R.layout.collapsing_scene_news) return "深空探索简报";
        if (layoutRes == R.layout.collapsing_scene_live) return "直播间";
        if (layoutRes == R.layout.collapsing_scene_food) return "商家";
        if (layoutRes == R.layout.collapsing_scene_note) return "笔记";
        if (layoutRes == R.layout.collapsing_scene_hotel) return "酒店详情";
        return "";
    }

    private int scrimColor() {
        if (layoutRes == R.layout.collapsing_scene_music) return 0xFFC62828;
        if (layoutRes == R.layout.collapsing_scene_creator) return 0xFF111111;
        if (layoutRes == R.layout.collapsing_scene_news) return 0xFF212121;
        if (layoutRes == R.layout.collapsing_scene_live) return 0xFF1A1A2E;
        if (layoutRes == R.layout.collapsing_scene_food) return 0xFFFF8A65;
        if (layoutRes == R.layout.collapsing_scene_note) return 0xFFF48FB1;
        if (layoutRes == R.layout.collapsing_scene_hotel) return 0xFF00897B;
        return 0xFF1A73E8;
    }

    private String coverUrl() {
        if (layoutRes == R.layout.collapsing_scene_music) return "https://picsum.photos/id/1016/1200/800";
        if (layoutRes == R.layout.collapsing_scene_shop) return "https://picsum.photos/id/1011/1200/700";
        if (layoutRes == R.layout.collapsing_scene_creator) return "https://picsum.photos/id/1025/1200/900";
        if (layoutRes == R.layout.collapsing_scene_news) return "https://picsum.photos/id/1002/1200/800";
        if (layoutRes == R.layout.collapsing_scene_live) return "https://picsum.photos/id/1039/1200/700";
        if (layoutRes == R.layout.collapsing_scene_food) return "https://picsum.photos/id/292/1200/800";
        if (layoutRes == R.layout.collapsing_scene_note) return "https://picsum.photos/id/1018/1200/1000";
        if (layoutRes == R.layout.collapsing_scene_hotel) return "https://picsum.photos/id/1040/1200/800";
        return "https://picsum.photos/id/1015/1200/800";
    }

    private String avatarUrl() {
        return layoutRes == R.layout.collapsing_scene_note
                ? "https://picsum.photos/id/64/200/200"
                : "https://picsum.photos/id/91/200/200";
    }

    private String listPrefix() {
        if (layoutRes == R.layout.collapsing_scene_music) return "曲目 ";
        if (layoutRes == R.layout.collapsing_scene_shop) return "商品 ";
        if (layoutRes == R.layout.collapsing_scene_creator) return "作品 ";
        if (layoutRes == R.layout.collapsing_scene_live) return "弹幕 ";
        if (layoutRes == R.layout.collapsing_scene_food) return "菜品 ";
        if (layoutRes == R.layout.collapsing_scene_note) return "评论 ";
        if (layoutRes == R.layout.collapsing_scene_profile) return "动态 ";
        return "条目 ";
    }

    private static String longText(String lead, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append("\n\n");
            sb.append(lead).append(i + 1)
                    .append("：这是 Design 嵌套滚动 Demo 的占位文案，用来验证 CollapsingToolbar / AppBar "
                            + "在长内容下的收起与展开手感。");
        }
        return sb.toString();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }
}
