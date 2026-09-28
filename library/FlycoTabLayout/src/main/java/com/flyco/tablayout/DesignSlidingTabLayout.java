package com.flyco.tablayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.listener.OnTabSelectListener;
import com.flyco.tablayout.utils.UnreadMsgUtils;
import com.flyco.tablayout.widget.MsgView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 设计稿样式 SlidingTabLayout：
 * - 底部角标使用 {@code ic_tab_indicator}（可配置）
 * - 整个 bar 内容垂直居中
 * - 单个 tab 的文字与角标作为整体垂直居中
 * - 选中放大 / 滑动指示器动画与 {@link SlidingTabLayout} 保持一致
 * <p>
 * 独立封装，不影响原有 {@link SlidingTabLayout}。
 */
public class DesignSlidingTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {

    private static final int TEXT_BOLD_NONE = 0;
    private static final int TEXT_BOLD_WHEN_SELECT = 1;
    private static final int TEXT_BOLD_BOTH = 2;
    private static final int TEXT_SCALE_DURATION = 50;

    /** 设计稿选中绿 */
    private static final String DEFAULT_SELECT_COLOR = "#4CAF50";
    /** 设计稿未选中灰黑 */
    private static final String DEFAULT_UNSELECT_COLOR = "#333333";

    private Context mContext;
    private ViewPager mViewPager;
    private ArrayList<String> mTitleList = new ArrayList<>();
    private LinearLayout mTabsContainer;
    private int mCurrentTab;
    private float mCurrentPositionOffset;
    private int mTabCount;

    private final Rect mIndicatorRect = new Rect();
    private final Rect mTabRect = new Rect();
    private Drawable mIndicatorDrawable;

    private final Paint mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float mTabPadding;
    private boolean mTabSpaceEqual;
    private float mTabWidth;

    private float mIndicatorHeight;
    private float mIndicatorWidth;
    private float mIndicatorMarginTop;
    private float mIndicatorMarginBottom;
    private boolean mIndicatorWidthEqualTitle;
    private int mIndicatorBackgroundResId;

    private int mUnderlineColor;
    private float mUnderlineHeight;

    private float mTextSize;
    private int mTextSelectColor;
    private int mTextUnSelectColor;
    private int mTextBold;
    private boolean mTextAllCaps;
    private float mTextScale = 1.25f;

    private int mLastScrollX;
    private int mHeight;
    private boolean mSnapOnTabClick;
    private OnTabSelectListener mListener;
    private OnScrollListener scrollListener;
    private final SparseArray<Boolean> mInitSetMap = new SparseArray<>();

    public DesignSlidingTabLayout(Context context) {
        this(context, null, 0);
    }

    public DesignSlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DesignSlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFillViewport(true);
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);

        this.mContext = context;
        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabsContainer.setGravity(Gravity.CENTER_VERTICAL);
        // 宽度 WRAP_CONTENT 以支持横向滚动；高度 MATCH_PARENT + 垂直居中，保证 bar 内容竖直居中
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER_VERTICAL;
        addView(mTabsContainer, lp);

        obtainAttributes(context, attrs);
        ensureIndicatorDrawable();

        if (attrs != null) {
            String height = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");
            if (height != null
                    && !height.equals(ViewGroup.LayoutParams.MATCH_PARENT + "")
                    && !height.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")) {
                int[] systemAttrs = {android.R.attr.layout_height};
                TypedArray a = context.obtainStyledAttributes(attrs, systemAttrs);
                mHeight = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                a.recycle();
            }
        }

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onScroll();
                return false;
            }
        });
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DesignSlidingTabLayout);

        mIndicatorHeight = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_indicator_height, dp2px(11));
        mIndicatorWidth = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_indicator_width, dp2px(-1));
        mIndicatorMarginTop = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_indicator_margin_top, dp2px(2));
        mIndicatorMarginBottom = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_indicator_margin_bottom, dp2px(0));
        mIndicatorWidthEqualTitle = ta.getBoolean(R.styleable.DesignSlidingTabLayout_dstl_indicator_width_equal_title, true);
        mIndicatorBackgroundResId = ta.getResourceId(
                R.styleable.DesignSlidingTabLayout_dstl_indicator_background, R.drawable.ic_tab_indicator);

        mUnderlineColor = ta.getColor(R.styleable.DesignSlidingTabLayout_dstl_underline_color, Color.parseColor("#EEEEEE"));
        mUnderlineHeight = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_underline_height, dp2px(0.5f));

        mTextSize = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_textsize, sp2px(16));
        mTextSelectColor = ta.getColor(R.styleable.DesignSlidingTabLayout_dstl_textSelectColor,
                Color.parseColor(DEFAULT_SELECT_COLOR));
        mTextUnSelectColor = ta.getColor(R.styleable.DesignSlidingTabLayout_dstl_textUnselectColor,
                Color.parseColor(DEFAULT_UNSELECT_COLOR));
        mTextBold = ta.getInt(R.styleable.DesignSlidingTabLayout_dstl_textBold, TEXT_BOLD_WHEN_SELECT);
        mTextAllCaps = ta.getBoolean(R.styleable.DesignSlidingTabLayout_dstl_textAllCaps, false);
        mTextScale = ta.getFloat(R.styleable.DesignSlidingTabLayout_dstl_textScale, 1.25f);

        mTabSpaceEqual = ta.getBoolean(R.styleable.DesignSlidingTabLayout_dstl_tab_space_equal, false);
        mTabWidth = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_tab_width, dp2px(-1));
        mTabPadding = ta.getDimension(R.styleable.DesignSlidingTabLayout_dstl_tab_padding,
                mTabSpaceEqual || mTabWidth > 0 ? dp2px(0) : dp2px(16));

        ta.recycle();
    }

    private void ensureIndicatorDrawable() {
        if (mIndicatorBackgroundResId != 0) {
            mIndicatorDrawable = ContextCompat.getDrawable(getContext(), mIndicatorBackgroundResId);
        }
        if (mIndicatorDrawable != null && mIndicatorHeight <= 0) {
            int intrinsicH = mIndicatorDrawable.getIntrinsicHeight();
            if (intrinsicH > 0) {
                mIndicatorHeight = intrinsicH;
            }
        }
    }

    public void setTitles(String[] titles) {
        if (titles == null || titles.length == 0) {
            return;
        }
        List<String> dataList = new ArrayList<>();
        Collections.addAll(dataList, titles);
        setTitles(dataList);
    }

    public void setTitles(List<String> titles) {
        if (titles == null || titles.isEmpty()) {
            return;
        }
        mTitleList.clear();
        mTitleList.addAll(titles);
        notifyDataSetChanged();
    }

    public void setViewPager(ViewPager vp, String[] titleArr) {
        List<String> dataList = new ArrayList<>();
        Collections.addAll(dataList, titleArr);
        setViewPager(vp, dataList);
    }

    public void setViewPager(ViewPager vp, List<String> titles) {
        if (vp == null || vp.getAdapter() == null) {
            return;
        }
        if (titles == null || titles.isEmpty()) {
            return;
        }
        if (titles.size() != vp.getAdapter().getCount()) {
            return;
        }
        this.mViewPager = vp;
        mTitleList.clear();
        mTitleList.addAll(titles);
        this.mViewPager.removeOnPageChangeListener(this);
        this.mViewPager.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabsContainer.removeAllViews();
        this.mTabCount = getTitleCount();
        for (int i = 0; i < mTabCount; i++) {
            View tabView = View.inflate(mContext, R.layout.layout_design_tab, null);
            CharSequence pageTitle = getTitle(i);
            addTab(i, pageTitle.toString(), tabView);
        }
        updateTabStyles();
    }

    private void addTab(final int position, String title, View tabView) {
        TextView tabTv = tabView.findViewById(R.id.tv_tab_title);
        if (tabTv != null && title != null) {
            if (title.contains("\n") || title.contains("<font") || title.contains("<br/>")) {
                tabTv.setSingleLine(false);
                tabTv.setText(Html.fromHtml(title));
            } else {
                tabTv.setText(title);
            }
        }
        applyTabContentLayout(tabView, position == mCurrentTab);

        tabView.setOnClickListener(v -> {
            int position1 = mTabsContainer.indexOfChild(v);
            if (position1 < 0) {
                return;
            }
            if (mViewPager != null) {
                if (mViewPager.getCurrentItem() != position1) {
                    if (mSnapOnTabClick) {
                        mViewPager.setCurrentItem(position1, false);
                    } else {
                        mViewPager.setCurrentItem(position1);
                    }
                    if (mListener != null) {
                        mListener.onTabSelect(position1);
                    }
                } else if (mListener != null) {
                    mListener.onTabReselect(position1);
                }
            } else {
                int lastPosition = mCurrentTab;
                mCurrentTab = position1;
                updateTabSelection(position1);
                scrollToCurrentTab();
                invalidate();
                if (mListener != null) {
                    if (lastPosition == position1) {
                        mListener.onTabReselect(position1);
                    } else {
                        mListener.onTabSelect(position1);
                    }
                }
            }
        });

        LinearLayout.LayoutParams tabLayoutParams = mTabSpaceEqual
                ? new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f)
                : new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        if (mTabWidth > 0) {
            tabLayoutParams = new LinearLayout.LayoutParams((int) mTabWidth, LayoutParams.MATCH_PARENT);
        }
        tabView.setPadding((int) mTabPadding, 0, (int) mTabPadding, 0);
        mTabsContainer.addView(tabView, position, tabLayoutParams);
    }

    /**
     * 角标占位高度（含上下间距）。仅选中 tab 占用该高度，使「文字+角标」整体垂直居中；
     * 未选中 tab 不占位，文字中线与选中整体中线对齐（而不是与选中 TextView 对齐）。
     */
    private int getIndicatorSpaceHeight() {
        return (int) (mIndicatorMarginTop + Math.max(mIndicatorHeight, 0) + mIndicatorMarginBottom);
    }

    private void applyTabContentLayout(View tabView, boolean selected) {
        if (tabView == null) {
            return;
        }
        View indicatorSpace = tabView.findViewById(R.id.v_indicator_space);
        View content = tabView.findViewById(R.id.ll_tab_content);
        if (indicatorSpace != null) {
            ViewGroup.LayoutParams spaceLp = indicatorSpace.getLayoutParams();
            int target = selected ? getIndicatorSpaceHeight() : 0;
            if (spaceLp.height != target) {
                spaceLp.height = target;
                indicatorSpace.setLayoutParams(spaceLp);
            }
        }
        if (content != null && content.getLayoutParams() instanceof LayoutParams) {
            LayoutParams flp = (LayoutParams) content.getLayoutParams();
            if (flp.gravity != Gravity.CENTER) {
                flp.gravity = Gravity.CENTER;
                content.setLayoutParams(flp);
            }
        }
    }

    private void updateTabStyles() {
        for (int i = 0; i < mTabCount; i++) {
            View titleView = mTabsContainer.getChildAt(i);
            final boolean isSelect = i == mCurrentTab;
            TextView tabTv = titleView.findViewById(R.id.tv_tab_title);
            if (tabTv == null) {
                return;
            }
            applyTabContentLayout(titleView, isSelect);
            tabTv.setTextColor(isSelect ? mTextSelectColor : mTextUnSelectColor);
            tabTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            if (mTextAllCaps) {
                tabTv.setText(tabTv.getText().toString().toUpperCase());
            }
            if (mTextBold == TEXT_BOLD_BOTH) {
                tabTv.getPaint().setFakeBoldText(true);
            } else if (mTextBold == TEXT_BOLD_NONE) {
                tabTv.getPaint().setFakeBoldText(false);
            } else {
                tabTv.getPaint().setFakeBoldText(isSelect);
            }
            if (isSelect) {
                scaleIn(tabTv);
            } else {
                scaleOut(tabTv);
            }
            tabTv.setSelected(isSelect);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.mCurrentTab = position;
        this.mCurrentPositionOffset = positionOffset;
        scrollToCurrentTab();
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        updateTabSelection(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void scrollToCurrentTab() {
        if (mTabCount <= 0) {
            return;
        }
        View current = mTabsContainer.getChildAt(mCurrentTab);
        if (current == null) {
            return;
        }
        int offset = (int) (mCurrentPositionOffset * current.getWidth());
        int newScrollX = current.getLeft() + offset;

        if (mCurrentTab > 0 || offset > 0) {
            newScrollX -= getWidth() / 2 - getPaddingLeft();
            calcIndicatorRect();
            newScrollX += ((mTabRect.right - mTabRect.left) / 2);
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
        onScroll();
    }

    private void updateTabSelection(int position) {
        for (int i = 0; i < mTabCount; ++i) {
            View tabView = mTabsContainer.getChildAt(i);
            final boolean isSelect = i == position;
            TextView tabTv = tabView.findViewById(R.id.tv_tab_title);
            if (tabTv == null) {
                return;
            }
            // 选中：文字+角标整体居中；未选中：仅文字，中线对齐选中整体中心
            applyTabContentLayout(tabView, isSelect);
            tabTv.setTextColor(isSelect ? mTextSelectColor : mTextUnSelectColor);
            if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                tabTv.getPaint().setFakeBoldText(isSelect);
                tabTv.invalidate();
            }
            if (isSelect) {
                scaleIn(tabTv);
            } else {
                scaleOut(tabTv);
            }
            tabTv.setSelected(isSelect);
        }
        requestLayout();
        invalidate();
    }

    private void scaleIn(View view) {
        if (mTextScale > 0) {
            ObjectAnimator scaleInXAnimator = ObjectAnimator.ofFloat(view, "scaleX", mTextScale);
            ObjectAnimator scaleInYAnimator = ObjectAnimator.ofFloat(view, "scaleY", mTextScale);
            AnimatorSet scaleInAnimSet = new AnimatorSet();
            scaleInAnimSet.setDuration(TEXT_SCALE_DURATION);
            scaleInAnimSet.setInterpolator(new LinearInterpolator());
            scaleInAnimSet.play(scaleInXAnimator).with(scaleInYAnimator);
            scaleInAnimSet.start();
        }
    }

    private void scaleOut(View view) {
        ObjectAnimator scaleOutXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.0f);
        ObjectAnimator scaleOutYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.0f);
        AnimatorSet scaleOutAnimSet = new AnimatorSet();
        scaleOutAnimSet.setDuration(TEXT_SCALE_DURATION);
        scaleOutAnimSet.setInterpolator(new LinearInterpolator());
        scaleOutAnimSet.play(scaleOutXAnimator).with(scaleOutYAnimator);
        scaleOutAnimSet.start();
    }

    /**
     * 计算角标矩形：水平居中于文字；垂直按「文字+角标」整体在 bar 内居中后落在文字下方。
     * 这样增大 paddingTop 时角标下移，选中文字上移，整体仍居中；未选中文字对齐该整体中线。
     */
    private void calcIndicatorRect() {
        View currentTabView = mTabsContainer.getChildAt(this.mCurrentTab);
        if (currentTabView == null) {
            return;
        }
        View content = currentTabView.findViewById(R.id.ll_tab_content);
        TextView tabTitle = currentTabView.findViewById(R.id.tv_tab_title);
        if (content == null || tabTitle == null) {
            return;
        }

        float left = currentTabView.getLeft();
        float right = currentTabView.getRight();

        float indicatorWidth = resolveIndicatorWidth(tabTitle, left, right);
        float centerX = currentTabView.getLeft() + content.getLeft() + content.getWidth() / 2f;
        float indicatorLeft = centerX - indicatorWidth / 2f;
        float indicatorRight = indicatorLeft + indicatorWidth;

        if (this.mCurrentTab < mTabCount - 1) {
            View nextTabView = mTabsContainer.getChildAt(this.mCurrentTab + 1);
            View nextContent = nextTabView.findViewById(R.id.ll_tab_content);
            TextView nextTitle = nextTabView.findViewById(R.id.tv_tab_title);
            float nextLeft = nextTabView.getLeft();
            float nextRight = nextTabView.getRight();

            float nextIndicatorWidth = resolveIndicatorWidth(nextTitle, nextLeft, nextRight);
            float nextCenterX = nextTabView.getLeft()
                    + (nextContent != null ? nextContent.getLeft() + nextContent.getWidth() / 2f
                    : nextTabView.getWidth() / 2f);
            float nextIndicatorLeft = nextCenterX - nextIndicatorWidth / 2f;
            float nextIndicatorRight = nextIndicatorLeft + nextIndicatorWidth;

            indicatorLeft = indicatorLeft + mCurrentPositionOffset * (nextIndicatorLeft - indicatorLeft);
            indicatorRight = indicatorRight + mCurrentPositionOffset * (nextIndicatorRight - indicatorRight);

            left = left + mCurrentPositionOffset * (nextLeft - left);
            right = right + mCurrentPositionOffset * (nextRight - right);
        }

        mTabRect.left = (int) left;
        mTabRect.right = (int) right;

        mIndicatorRect.left = (int) indicatorLeft;
        mIndicatorRect.right = (int) indicatorRight;

        // 按整体居中计算角标 Y，避免依赖各 tab 是否已带占位
        float textHeight = tabTitle.getHeight() > 0 ? tabTitle.getHeight() : tabTitle.getMeasuredHeight();
        if (textHeight <= 0) {
            mTextPaint.setTextSize(mTextSize);
            textHeight = mTextPaint.descent() - mTextPaint.ascent();
        }
        float spaceHeight = getIndicatorSpaceHeight();
        float groupTop = (getHeight() - textHeight - spaceHeight) / 2f;
        int indicatorTop = Math.round(groupTop + textHeight + mIndicatorMarginTop);
        mIndicatorRect.top = indicatorTop;
        mIndicatorRect.bottom = (int) (indicatorTop + mIndicatorHeight);
    }

    private float resolveIndicatorWidth(TextView tabTitle, float tabLeft, float tabRight) {
        if (mIndicatorWidth > 0) {
            return mIndicatorWidth;
        }
        if (mIndicatorWidthEqualTitle && tabTitle != null) {
            mTextPaint.setTextSize(tabTitle.getTextSize());
            return mTextPaint.measureText(tabTitle.getText().toString());
        }
        if (mIndicatorDrawable != null && mIndicatorDrawable.getIntrinsicWidth() > 0
                && mIndicatorDrawable.getIntrinsicHeight() > 0 && mIndicatorHeight > 0) {
            return mIndicatorHeight * mIndicatorDrawable.getIntrinsicWidth()
                    / (float) mIndicatorDrawable.getIntrinsicHeight();
        }
        return Math.max(0, tabRight - tabLeft);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || mTabCount <= 0) {
            return;
        }
        int height = getHeight();
        int paddingLeft = getPaddingLeft();

        if (mUnderlineHeight > 0) {
            mRectPaint.setColor(mUnderlineColor);
            canvas.drawRect(paddingLeft, height - mUnderlineHeight,
                    mTabsContainer.getWidth() + paddingLeft, height, mRectPaint);
        }

        calcIndicatorRect();
        if (mIndicatorHeight <= 0 || mIndicatorDrawable == null) {
            return;
        }
        mIndicatorDrawable.setBounds(
                paddingLeft + mIndicatorRect.left,
                mIndicatorRect.top,
                paddingLeft + mIndicatorRect.right,
                mIndicatorRect.bottom);
        mIndicatorDrawable.draw(canvas);
    }

    public void setCurrentTab(int currentTab) {
        this.mCurrentTab = currentTab;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(currentTab);
        } else {
            updateTabSelection(currentTab);
            scrollToCurrentTab();
            invalidate();
        }
    }

    public void setCurrentTab(int currentTab, boolean smoothScroll) {
        this.mCurrentTab = currentTab;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(currentTab, smoothScroll);
        } else {
            updateTabSelection(currentTab);
            scrollToCurrentTab();
            invalidate();
        }
    }

    public void setTabPadding(float tabPadding) {
        this.mTabPadding = dp2px(tabPadding);
        updateTabStyles();
    }

    public void setTabSpaceEqual(boolean tabSpaceEqual) {
        this.mTabSpaceEqual = tabSpaceEqual;
        notifyDataSetChanged();
    }

    public void setIndicatorHeight(float indicatorHeightDp) {
        this.mIndicatorHeight = dp2px(indicatorHeightDp);
        updateTabStyles();
        requestLayout();
        invalidate();
    }

    public void setIndicatorMarginTop(float marginTopDp) {
        this.mIndicatorMarginTop = dp2px(marginTopDp);
        updateTabStyles();
        requestLayout();
        invalidate();
    }

    public void setIndicatorMarginBottom(float marginBottomDp) {
        this.mIndicatorMarginBottom = dp2px(marginBottomDp);
        updateTabStyles();
        requestLayout();
        invalidate();
    }

    public void setIndicatorWidth(float indicatorWidthDp) {
        this.mIndicatorWidth = dp2px(indicatorWidthDp);
        invalidate();
    }

    public void setIndicatorWidthEqualTitle(boolean equalTitle) {
        this.mIndicatorWidthEqualTitle = equalTitle;
        invalidate();
    }

    public void setIndicatorBackground(int resId) {
        this.mIndicatorBackgroundResId = resId;
        ensureIndicatorDrawable();
        invalidate();
    }

    public void setTextSize(float textSizeSp) {
        this.mTextSize = sp2px(textSizeSp);
        updateTabStyles();
    }

    public void setTextSelectColor(int textSelectColor) {
        this.mTextSelectColor = textSelectColor;
        updateTabStyles();
    }

    public void setTextUnSelectColor(int textUnSelectColor) {
        this.mTextUnSelectColor = textUnSelectColor;
        updateTabStyles();
    }

    public void setTextBold(int textBold) {
        this.mTextBold = textBold;
        updateTabStyles();
    }

    public void setTextScale(float textScale) {
        this.mTextScale = textScale;
        updateTabStyles();
    }

    public void setSnapOnTabClick(boolean snapOnTabClick) {
        mSnapOnTabClick = snapOnTabClick;
    }

    public int getTabCount() {
        return mTabCount;
    }

    public int getCurrentTab() {
        return mCurrentTab;
    }

    public TextView getTitleView(int tab) {
        View tabView = mTabsContainer.getChildAt(tab);
        return tabView == null ? null : (TextView) tabView.findViewById(R.id.tv_tab_title);
    }

    public void showMsg(int position, int num) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        if (tabView == null) {
            return;
        }
        MsgView tipView = tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            UnreadMsgUtils.show(tipView, num);
            if (mInitSetMap.get(position) != null && mInitSetMap.get(position)) {
                return;
            }
            setMsgMargin(position, 4, 2);
            mInitSetMap.put(position, true);
        }
    }

    public void showDot(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        showMsg(position, 0);
    }

    public void hideMsg(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        if (tabView == null) {
            return;
        }
        MsgView tipView = tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            tipView.setVisibility(View.GONE);
        }
    }

    public void setMsgMargin(int position, float leftPadding, float bottomPadding) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        if (tabView == null) {
            return;
        }
        MsgView tipView = tabView.findViewById(R.id.rtv_msg_tip);
        TextView tvTabTitle = tabView.findViewById(R.id.tv_tab_title);
        if (tipView != null && tvTabTitle != null) {
            mTextPaint.setTextSize(mTextSize);
            float textWidth = mTextPaint.measureText(tvTabTitle.getText().toString());
            float textHeight = mTextPaint.descent() - mTextPaint.ascent();
            MarginLayoutParams lp = (MarginLayoutParams) tipView.getLayoutParams();
            lp.leftMargin = mTabWidth >= 0
                    ? (int) (mTabWidth / 2 + textWidth / 2 + dp2px(leftPadding))
                    : (int) (mTabPadding + textWidth + dp2px(leftPadding));
            lp.topMargin = mHeight > 0 ? (int) (mHeight - textHeight) / 2 - dp2px(bottomPadding) : 0;
            tipView.setLayoutParams(lp);
        }
    }

    public MsgView getMsgView(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        return tabView == null ? null : (MsgView) tabView.findViewById(R.id.rtv_msg_tip);
    }

    public void setOnTabSelectListener(OnTabSelectListener listener) {
        this.mListener = listener;
    }

    private void onScroll() {
        if (scrollListener == null) {
            return;
        }
        int scrollX = getScrollX();
        int width = getWidth();
        int scrollViewMeasuredWidth = getChildAt(0).getMeasuredWidth();

        if (scrollX == 0) {
            scrollListener.onScrollStart(this, scrollX);
        } else if ((scrollX + width) >= scrollViewMeasuredWidth) {
            scrollListener.onScrollEnd(this, scrollX);
        } else {
            scrollListener.onScrolling(this, scrollX);
        }
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.scrollListener = listener;
    }

    public interface OnScrollListener {
        void onScrolling(View view, int scroll);

        void onScrollStart(View view, int scroll);

        void onScrollEnd(View view, int scroll);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("mCurrentTab", mCurrentTab);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("instanceState");
            mCurrentTab = bundle.getInt("mCurrentTab");
            if (mCurrentTab != 0 && mTabsContainer.getChildCount() > 0) {
                updateTabSelection(mCurrentTab);
                scrollToCurrentTab();
            }
        }
        super.onRestoreInstanceState(state);
    }

    protected int dp2px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected int sp2px(float sp) {
        final float scale = this.mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    private int getTitleCount() {
        if (mTitleList != null) {
            return mTitleList.size();
        } else if (mViewPager != null && mViewPager.getAdapter() != null) {
            return mViewPager.getAdapter().getCount();
        } else {
            return 0;
        }
    }

    private CharSequence getTitle(int position) {
        if (mTitleList != null && position < mTitleList.size()) {
            return mTitleList.get(position);
        } else if (mViewPager != null && mViewPager.getAdapter() != null
                && position < mViewPager.getAdapter().getCount()) {
            return mViewPager.getAdapter().getPageTitle(position);
        } else {
            return "";
        }
    }
}
