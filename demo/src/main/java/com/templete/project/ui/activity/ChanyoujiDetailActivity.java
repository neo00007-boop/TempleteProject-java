package com.templete.project.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.gyf.immersionbar.ImmersionBar;
import com.lib.base.adapter.DemoAdapter;
import com.lib.base.glide.transformations.BlurTransformation;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.util.ScreenUtil;
import com.templete.project.R;
import com.templete.project.databinding.ActivityChanyoujiDetailBinding;
import com.templete.project.ui.widget.ChanyoujiAppBarBehavior;

import java.util.ArrayList;
import java.util.List;

/**
 * 蝉游记详情：折叠 + 毛玻璃；顶图触摸转发给列表，与列表同一套滚动/下拉。
 */
public class ChanyoujiDetailActivity extends BaseActivity<ActivityChanyoujiDetailBinding> {

    private static final String COVER_URL = "https://picsum.photos/id/1015/1200/900";
    private static final int COVER_DP = 280;
    private static final int BACK_SIZE_DP = 28;

    private int statusBarHeight;
    private int titleBarH;
    private int coverBaseH;
    private float lastBlurAlpha = -1f;
    private ChanyoujiAppBarBehavior appBarBehavior;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ChanyoujiDetailActivity.class));
    }

    @Override
    protected ActivityChanyoujiDetailBinding viewBinding() {
        return ActivityChanyoujiDetailBinding.inflate(getLayoutInflater());
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
                .init();
    }

    @Override
    public void initView() {
        statusBarHeight = Math.max(0, ScreenUtil.getStatusBarSize());
        titleBarH = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
        coverBaseH = dp(COVER_DP);

        appBarBehavior = new ChanyoujiAppBarBehavior();
        appBarBehavior.setBaseHeight(coverBaseH);
        ViewGroup.LayoutParams alp = mViewBinding.appBar.getLayoutParams();
        if (alp instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) alp).setBehavior(appBarBehavior);
        }

        setupChrome();
        loadImages();
        setupList();
        setupBlur();
        setupImageTouchForward();

        ViewCompat.setOnApplyWindowInsetsListener(mViewBinding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            int top = bars.top > 0 ? bars.top : Math.max(0, ScreenUtil.getStatusBarSize());
            if (top > 0 && top != statusBarHeight) {
                statusBarHeight = top;
                setupChrome();
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(mViewBinding.getRoot());
    }

    private void setupChrome() {
        int bar = statusBarHeight + titleBarH;
        CollapsingToolbarLayout collapsing = mViewBinding.collapsingToolbar;
        ViewGroup.LayoutParams clp = collapsing.getLayoutParams();
        if (clp.height != coverBaseH) {
            clp.height = coverBaseH;
            collapsing.setLayoutParams(clp);
        }
        if (appBarBehavior != null) {
            appBarBehavior.setBaseHeight(coverBaseH);
        }
        collapsing.setMinimumHeight(bar);
        collapsing.setTitleEnabled(false);

        ViewGroup.LayoutParams tlp = mViewBinding.toolbar.getLayoutParams();
        if (tlp.height != bar) {
            tlp.height = bar;
            mViewBinding.toolbar.setLayoutParams(tlp);
        }

        ViewGroup.LayoutParams blp = mViewBinding.titleBlurBar.getLayoutParams();
        if (blp.height != bar) {
            blp.height = bar;
            mViewBinding.titleBlurBar.setLayoutParams(blp);
        }

        ViewGroup.MarginLayoutParams backLp =
                (ViewGroup.MarginLayoutParams) mViewBinding.btnBack.getLayoutParams();
        backLp.topMargin = statusBarHeight + Math.max(0, (titleBarH - dp(BACK_SIZE_DP)) / 2);
        mViewBinding.btnBack.setLayoutParams(backLp);
        mViewBinding.btnBack.setOnClickListener(v -> finish());
        mViewBinding.btnBack.bringToFront();
    }

    /**
     * 顶图区域触摸全部转发给 RecyclerView，折叠/下拉只走列表 NestedScroll 一条路。
     */
    private void setupImageTouchForward() {
        mViewBinding.appBar.setClickable(true);
        mViewBinding.appBar.setOnTouchListener((v, event) -> {
            MotionEvent copy = MotionEvent.obtain(event);
            int[] appLoc = new int[2];
            int[] rvLoc = new int[2];
            v.getLocationOnScreen(appLoc);
            mViewBinding.recyclerView.getLocationOnScreen(rvLoc);
            copy.offsetLocation(appLoc[0] - rvLoc[0], appLoc[1] - rvLoc[1]);
            mViewBinding.recyclerView.dispatchTouchEvent(copy);
            copy.recycle();
            // 始终消费，保证后续 MOVE/UP 仍进到这里（与列表同一手势流）
            return true;
        });
    }

    private void loadImages() {
        Glide.with(this)
                .load(COVER_URL)
                .centerCrop()
                .placeholder(R.drawable.demo)
                .error(R.drawable.demo)
                .into(mViewBinding.ivCover);

        RequestOptions blurOpt = RequestOptions.bitmapTransform(new BlurTransformation(25, 3));
        Glide.with(this)
                .load(COVER_URL)
                .apply(blurOpt)
                .error(Glide.with(this).load(R.drawable.demo).apply(blurOpt))
                .into(mViewBinding.ivBlur);
    }

    private void setupList() {
        mViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mViewBinding.recyclerView.setHasFixedSize(true);
        mViewBinding.recyclerView.setItemAnimator(null);

        DemoAdapter adapter = new DemoAdapter(this);
        List<String> list = new ArrayList<>();
        list.add("行程概览：清水寺 → 二年坂 → 产宁坂 → 祇园");
        list.add("早上从京都站坐 100 路巴士到清水道，人还不算多。");
        list.add("清水寺的木构舞台在晨雾里特别有感觉，适合慢拍。");
        list.add("沿着二年坂往下走，两边小店密集，记得留肚子吃抹茶甜品。");
        list.add("中午在产宁坂随便找了家乌冬，排队不长。");
        list.add("下午逛祇园石板路，偶尔能碰到穿振袖的游客拍照。");
        list.add("傍晚建议提前占位看清水寺夜景，灯光一开氛围完全不同。");
        list.add("交通：巴士一日券够用；走路坡多，鞋子要舒服。");
        list.add("花费：门票 + 吃喝大概人均 300～500 人民币。");
        list.add("Tips：下雨天石板很滑；热门点早点去避人潮。");
        for (int i = 1; i <= 20; i++) {
            list.add("游记段落 " + i + "：补充一些早期蝉游记风格的长文占位，方便验证列表滚动与顶图联动。");
        }
        adapter.setList(list);
        mViewBinding.recyclerView.setAdapter(adapter);
    }

    private void setupBlur() {
        mViewBinding.titleBlurBar.setVisibility(View.GONE);
        mViewBinding.titleBlurBar.setClickable(false);
        mViewBinding.titleBlurBar.setFocusable(false);
        mViewBinding.ivBlur.setClickable(false);
        mViewBinding.tvTitle.setClickable(false);

        mViewBinding.appBar.addOnOffsetChangedListener((appBar, offset) -> {
            int range = appBar.getTotalScrollRange();
            if (range <= 0) {
                return;
            }
            int bar = Math.max(1, statusBarHeight + titleBarH);
            int remain = bar + range - Math.abs(offset);
            float a = Math.max(0f, Math.min(1f, (2f * bar - remain) / (float) bar));
            if (Math.abs(a - lastBlurAlpha) < 0.02f) {
                return;
            }
            lastBlurAlpha = a;
            if (a <= 0.01f) {
                mViewBinding.titleBlurBar.setVisibility(View.GONE);
            } else {
                mViewBinding.titleBlurBar.setVisibility(View.VISIBLE);
                mViewBinding.titleBlurBar.setAlpha(a);
            }
            mViewBinding.expandedMeta.setAlpha(1f - a);

            // 折叠过程中若还顶着 translation，清掉，防止花屏
            if (Math.abs(offset) > 1 && appBarBehavior != null && appBarBehavior.getPullExtra() > 0f) {
                appBarBehavior.resetPull(appBar, mViewBinding.coordinator);
            }
        });
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

    @Override
    public void onDestroy() {
        if (appBarBehavior != null) {
            appBarBehavior.resetPull(mViewBinding.appBar, mViewBinding.coordinator);
        }
        super.onDestroy();
    }
}
