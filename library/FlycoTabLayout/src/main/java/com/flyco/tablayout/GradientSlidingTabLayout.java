package com.flyco.tablayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.RelativeLayout;
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
 * 滑动TabLayout, viewPager与title分离，不依赖于viewPager
 * 支持设置drawable类型游标背景
 */
public class GradientSlidingTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {

    private Context mContext;
    private ViewPager mViewPager;
    private ArrayList<String> mTitleList = new ArrayList<>();
    private LinearLayout mTabsContainer;
    private int mCurrentTab;
    private float mCurrentPositionOffset;
    private int mTabCount;
    //用于绘制显示器
    private Rect mIndicatorRect = new Rect();
    //用于实现滚动居中
    private Rect mTabRect = new Rect();
    protected GradientDrawable mIndicatorDrawable = new GradientDrawable();
    protected Drawable mIndicatorBackgroundDrawable;

    private Paint mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mTrianglePath = new Path();
    private Paint mIndicatorPaint = new Paint();

    private static final int STYLE_NORMAL = 0;
    private static final int STYLE_TRIANGLE = 1;
    private static final int STYLE_BLOCK = 2;

    private int mIndicatorStyle = STYLE_NORMAL;
    
    private int mIndicatorStartColor = 0;
    private int mIndicatorEndColor = 0;
    private int mIndicatorGradientDirection = 0; // 0 = HORIZONTAL, 1 = VERTICAL

    private float mTabPadding;
    private boolean mTabSpaceEqual;
    private float mTabWidth;

    private boolean showTabSelectColor = false;
    private int mTabSelectColor;
    private int mTabUnSelectColor;

    //indicator
    private int mIndicatorColor;
    private float mIndicatorHeight;
    private float mIndicatorWidth;
    private float mIndicatorCornerRadius;
    private float mIndicatorMarginLeft;
    private float mIndicatorMarginTop;
    private float mIndicatorMarginRight;
    private float mIndicatorMarginBottom;
    private int mIndicatorGravity;
    private boolean mIndicatorWidthEqualTitle;
    protected int mIndicatorBackgroundResId;

    //underline
    private int mUnderlineColor;
    private float mUnderlineHeight;
    private int mUnderlineGravity;

    //divider
    private int mDividerColor;
    private float mDividerWidth;
    private float mDividerPadding;

    //title
    private static final int TEXT_BOLD_NONE = 0;
    private static final int TEXT_BOLD_WHEN_SELECT = 1;
    private static final int TEXT_BOLD_BOTH = 2;
    private float mTextSize;
    private int mTextSelectColor;
    private int mTextUnSelectColor;
    private int mTextBold;
    private boolean mTextAllCaps;
    private float mTextWidth, mTextHeight;
    private int mTextBackgroundResId;
    private int mTextPadding, mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom;

    private int mLastScrollX;
    private int mHeight;
    private boolean mSnapOnTabClick;
    private float margin;
    private static final int TEXT_SCALE_DURATION = 50;
    private float mTextScale = 1.0f;


    public GradientSlidingTabLayout(Context context) {
        this(context, null, 0);
    }

    public GradientSlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ResourceType")
    public GradientSlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFillViewport(true);//设置滚动视图是否可以伸缩其内容以填充视口
        setWillNotDraw(false);//重写onDraw方法,需要调用这个方法来清除flag
        setClipChildren(false);
        setClipToPadding(false);

        mIndicatorPaint.setAntiAlias(true);

        this.mContext = context;
        mTabsContainer = new LinearLayout(context);
        addView(mTabsContainer);

        obtainAttributes(context, attrs);

        String height = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");
        if (!height.equals(ViewGroup.LayoutParams.MATCH_PARENT + "")
                && !height.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")) {
            int[] systemAttrs = {android.R.attr.layout_height};
            TypedArray a = context.obtainStyledAttributes(attrs, systemAttrs);
            mHeight = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            a.recycle();
        }
        if (mIndicatorBackgroundResId > 0) {
            mIndicatorBackgroundDrawable = ContextCompat.getDrawable(getContext(), mIndicatorBackgroundResId);
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
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);

        mIndicatorStyle = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_style, STYLE_NORMAL);
        mIndicatorColor = ta.getColor(R.styleable.SlidingTabLayout_tl_indicator_color, Color.parseColor(mIndicatorStyle == STYLE_BLOCK ? "#4B6A87" : "#ffffff"));
        mIndicatorHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_height,
                dp2px(mIndicatorStyle == STYLE_TRIANGLE ? 4 : (mIndicatorStyle == STYLE_BLOCK ? -1 : 2)));
        mIndicatorWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_width, dp2px(mIndicatorStyle == STYLE_TRIANGLE ? 10 : -1));
        mIndicatorCornerRadius = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_corner_radius, dp2px(mIndicatorStyle == STYLE_BLOCK ? -1 : 0));
        mIndicatorMarginLeft = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_left, dp2px(0));
        mIndicatorMarginTop = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_top, dp2px(mIndicatorStyle == STYLE_BLOCK ? 7 : 0));
        mIndicatorMarginRight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_right, dp2px(0));
        mIndicatorMarginBottom = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_bottom, dp2px(mIndicatorStyle == STYLE_BLOCK ? 7 : 0));
        mIndicatorGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_gravity, Gravity.BOTTOM);
        mIndicatorWidthEqualTitle = ta.getBoolean(R.styleable.SlidingTabLayout_tl_indicator_width_equal_title, false);
        mIndicatorBackgroundResId = ta.getResourceId(R.styleable.SlidingTabLayout_tl_indicator_background, 0);

        mUnderlineColor = ta.getColor(R.styleable.SlidingTabLayout_tl_underline_color, Color.parseColor("#ffffff"));
        mUnderlineHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_underline_height, dp2px(0));
        mUnderlineGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_underline_gravity, Gravity.BOTTOM);

        mDividerColor = ta.getColor(R.styleable.SlidingTabLayout_tl_divider_color, Color.parseColor("#ffffff"));
        mDividerWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_width, dp2px(0));
        mDividerPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_padding, dp2px(12));

        mTextSize = ta.getDimension(R.styleable.SlidingTabLayout_tl_textsize, sp2px(14));
        mTextSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textSelectColor, Color.parseColor("#ffffff"));
        mTextUnSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textUnselectColor, Color.parseColor("#AAffffff"));
        mTextBold = resolveTextBold(ta.getInt(R.styleable.SlidingTabLayout_tl_textBold, TEXT_BOLD_NONE));
        mTextAllCaps = ta.getBoolean(R.styleable.SlidingTabLayout_tl_textAllCaps, false);
        mTextWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_text_width, dp2px(-1));
        mTextHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_text_height, dp2px(-1));
        mTextBackgroundResId = ta.getResourceId(R.styleable.SlidingTabLayout_tl_textBackground, 0);
        mTextPadding = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_text_padding, 0);
        mTextPaddingLeft = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_text_paddingLeft, 0);
        mTextPaddingTop = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_text_paddingTop, 0);
        mTextPaddingRight = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_text_paddingRight, 0);
        mTextPaddingBottom = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_text_paddingBottom, 0);

        mTabSpaceEqual = ta.getBoolean(R.styleable.SlidingTabLayout_tl_tab_space_equal, false);
        mTabWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_width, dp2px(-1));
        mTabPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_padding, mTabSpaceEqual || mTabWidth > 0 ? dp2px(0) : dp2px(20));

        showTabSelectColor = ta.getBoolean(R.styleable.SlidingTabLayout_tl_showTabSelectColor, false);
        mTabSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_tabSelectColor, Color.parseColor("#00000000"));
        mTabUnSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_tabUnSelectColor, Color.parseColor("#00000000"));

        mTextScale = ta.getFloat(R.styleable.SlidingTabLayout_tl_textScale, 1f);

        ta.recycle();

        TypedArray customTa = context.obtainStyledAttributes(attrs, R.styleable.GradientSlidingTabLayout);
        mIndicatorStartColor = customTa.getColor(R.styleable.GradientSlidingTabLayout_gst_indicator_start_color, 0);
        mIndicatorEndColor = customTa.getColor(R.styleable.GradientSlidingTabLayout_gst_indicator_end_color, 0);
        mIndicatorGradientDirection = customTa.getInt(R.styleable.GradientSlidingTabLayout_gst_indicator_gradient_direction, 0);
        customTa.recycle();
    }

    /** 不关联 ViewPager，只展示标题。选中变化通过 {@link OnTabSelectListener} 回调。 */
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
        if (mTitleList == null) {
            mTitleList = new ArrayList<>();
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

    /** 关联 ViewPager，标题用传入数组，长度须与 adapter 页数一致。 */
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
        if (mTitleList == null) {
            mTitleList = new ArrayList<>();
        }
        mTitleList.clear();
        mTitleList.addAll(titles);
        this.mViewPager.removeOnPageChangeListener(this);
        this.mViewPager.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /**
     * 更新数据
     */
    public void notifyDataSetChanged() {
        mTabsContainer.removeAllViews();
        this.mTabCount = getTitleCount();
        View tabView;
        for (int i = 0; i < mTabCount; i++) {
            tabView = View.inflate(mContext, R.layout.layout_tab, null);
            CharSequence pageTitle = getTitle(i);
            addTab(i, pageTitle.toString(), tabView);
        }
        updateTabStyles();
    }

    //创建并添加tab
    private void addTab(final int position, String title, View tabView) {
        TextView tabTv = tabView.findViewById(R.id.tv_tab_title);
        if (tabTv != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tabTv.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            if (title != null) {
                if (title.contains("\n") || title.contains("<font") || title.contains("<br/>")) {
                    tabTv.setSingleLine(false);
                    tabTv.setText(Html.fromHtml(title));
                } else
                    tabTv.setText(title);
            }
            if (mTextWidth > 0) {
                layoutParams.width = (int) mTextWidth;
            }
            if (mTextHeight > 0){
                layoutParams.height = (int) mTextHeight;
            }
            if (mTextBackgroundResId > 0){
                tabTv.setBackgroundResource(mTextBackgroundResId);
            }
            if (mTextPadding > 0){
                tabTv.setPadding(mTextPadding, mTextPadding, mTextPadding, mTextPadding);
            } else {
                tabTv.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
            }
        }

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
                } else {
                    if (mListener != null) {
                        mListener.onTabReselect(position1);
                    }
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
        //每一个Tab的布局参数
        LinearLayout.LayoutParams tabLayoutParams = mTabSpaceEqual ?
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f) :
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        if (mTabWidth > 0) {
            tabLayoutParams = new LinearLayout.LayoutParams((int) mTabWidth, LayoutParams.MATCH_PARENT);
        }
        tabView.setPadding((int) mTabPadding, 0, (int) mTabPadding, 0);
        mTabsContainer.addView(tabView, position, tabLayoutParams);
    }

    private void updateTabStyles() {
        for (int i = 0; i < mTabCount; i++) {
            View titleView = mTabsContainer.getChildAt(i);
            final boolean isSelect = i == mCurrentTab;
            TextView tabTv = titleView.findViewById(R.id.tv_tab_title);
            if (tabTv == null) {
                return;
            }
            tabTv.setTextColor(i == mCurrentTab ? mTextSelectColor : mTextUnSelectColor);
            tabTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
//            tabTv.setPadding((int) mTabPadding, 0, (int) mTabPadding, 0);
            if (mTextAllCaps) {
                tabTv.setText(tabTv.getText().toString().toUpperCase());
            }
            if (mTextBold == TEXT_BOLD_BOTH) {
                tabTv.getPaint().setFakeBoldText(true);
            } else if (mTextBold == TEXT_BOLD_NONE) {
                tabTv.getPaint().setFakeBoldText(false);
            } else {
                tabTv.getPaint().setFakeBoldText(i == mCurrentTab);
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
        //position:当前View的位置
        //mCurrentPositionOffset:当前View的偏移量比例.[0,1)
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

    //HorizontalScrollView滚到当前tab,并且居中显示
    private void scrollToCurrentTab() {
        if (mTabCount <= 0) {
            return;
        }
        int offset = (int) (mCurrentPositionOffset * mTabsContainer.getChildAt(mCurrentTab).getWidth());
        //当前Tab的left+当前Tab的Width乘以positionOffset
        int newScrollX = mTabsContainer.getChildAt(mCurrentTab).getLeft() + offset;

        if (mCurrentTab > 0 || offset > 0) {
            //HorizontalScrollView移动到当前tab,并居中
            newScrollX -= getWidth() / 2 - getPaddingLeft();
            calcIndicatorRect();
            newScrollX += ((mTabRect.right - mTabRect.left) / 2);
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            //scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
            //x:表示离起始位置的x水平方向的偏移量
            //y:表示离起始位置的y垂直方向的偏移量
            scrollTo(newScrollX, 0);
        }
        onScroll();
    }

    private void updateTabSelection(int position) {
        for (int i = 0; i < mTabCount; ++i) {
            View tabView = mTabsContainer.getChildAt(i);
            final boolean isSelect = i == position;
            TextView tabTv = tabView.findViewById(R.id.tv_tab_title);
            if (tabTv == null){
                return;
            }
            tabTv.setTextColor(isSelect ? mTextSelectColor : mTextUnSelectColor);
            if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                tabTv.getPaint().setFakeBoldText(isSelect);
                tabTv.invalidate();
            }
            if (showTabSelectColor) {
                try {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                    layoutParams.bottomMargin = (int) getIndicatorHeight();
                    tabView.setLayoutParams(layoutParams);
                    if (isSelect) {
                        tabView.setBackgroundColor(mTabSelectColor);
                    } else {
                        tabView.setBackgroundColor(mTabUnSelectColor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (isSelect) {
                scaleIn(tabTv);
            } else {
                scaleOut(tabTv);
            }
            tabTv.setSelected(isSelect);
        }
    }

    private void scaleIn(View view) {
        if (mTextScale > 0) {
            ObjectAnimator scaleInXAnimator = ObjectAnimator.ofFloat(view, "scaleX", mTextScale);
            ObjectAnimator scaleInYAnimator = ObjectAnimator.ofFloat(view, "scaleY", mTextScale);
            AnimatorSet scaleInAnimSet = new AnimatorSet();
            scaleInAnimSet.setDuration(TEXT_SCALE_DURATION);
            scaleInAnimSet.setInterpolator(new LinearInterpolator());
            scaleInAnimSet.play(scaleInXAnimator)
                    .with(scaleInYAnimator);
            scaleInAnimSet.start();
        }
    }

    private void scaleOut(View view) {
        ObjectAnimator scaleOutXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.0f);
        ObjectAnimator scaleOutYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.0f);
        AnimatorSet scaleOutAnimSet = new AnimatorSet();
        scaleOutAnimSet.setDuration(TEXT_SCALE_DURATION);
        scaleOutAnimSet.setInterpolator(new LinearInterpolator());
        scaleOutAnimSet.play(scaleOutXAnimator)
                .with(scaleOutYAnimator);
        scaleOutAnimSet.start();
    }

    private void calcIndicatorRect() {
        View currentTabView = mTabsContainer.getChildAt(this.mCurrentTab);
        float left = currentTabView.getLeft();
        float right = currentTabView.getRight();

        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            TextView tab_title = currentTabView.findViewById(R.id.tv_tab_title);
            mTextPaint.setTextSize(mTextSize);
            float textWidth = mTextPaint.measureText(tab_title.getText().toString());
            margin = (right - left - textWidth) / 2;
        }

        if (this.mCurrentTab < mTabCount - 1) {
            View nextTabView = mTabsContainer.getChildAt(this.mCurrentTab + 1);
            float nextTabLeft = nextTabView.getLeft();
            float nextTabRight = nextTabView.getRight();

            left = left + mCurrentPositionOffset * (nextTabLeft - left);
            right = right + mCurrentPositionOffset * (nextTabRight - right);

            //for mIndicatorWidthEqualTitle
            if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
                TextView next_tab_title = (TextView) nextTabView.findViewById(R.id.tv_tab_title);
                mTextPaint.setTextSize(mTextSize);
                float nextTextWidth = mTextPaint.measureText(next_tab_title.getText().toString());
                float nextMargin = (nextTabRight - nextTabLeft - nextTextWidth) / 2;
                margin = margin + mCurrentPositionOffset * (nextMargin - margin);
            }
        }

        mIndicatorRect.left = (int) left;
        mIndicatorRect.right = (int) right;
        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            mIndicatorRect.left = (int) (left + margin - 1);
            mIndicatorRect.right = (int) (right - margin - 1);
        }

        mTabRect.left = (int) left;
        mTabRect.right = (int) right;

        if (mIndicatorWidth < 0) {   //indicatorWidth小于0时,原jpardogo's PagerSlidingTabStrip
            return;
        }
        //indicatorWidth大于0时,圆角矩形以及三角形
        float indicatorLeft = currentTabView.getLeft() + (currentTabView.getWidth() - mIndicatorWidth) / 2;

        if (this.mCurrentTab < mTabCount - 1) {
            View nextTab = mTabsContainer.getChildAt(this.mCurrentTab + 1);
            indicatorLeft = indicatorLeft + mCurrentPositionOffset * (currentTabView.getWidth() / 2f + nextTab.getWidth() / 2f);
        }
        mIndicatorRect.left = (int) indicatorLeft;
        mIndicatorRect.right = (int) (mIndicatorRect.left + mIndicatorWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || mTabCount <= 0) {
            return;
        }
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.setStrokeWidth(mDividerWidth);
            mDividerPaint.setColor(mDividerColor);
            for (int i = 0; i < mTabCount - 1; i++) {
                View tab = mTabsContainer.getChildAt(i);
                canvas.drawLine(paddingLeft + tab.getRight(), mDividerPadding, paddingLeft + tab.getRight(), height - mDividerPadding, mDividerPaint);
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.setColor(mUnderlineColor);
            if (mUnderlineGravity == Gravity.BOTTOM) {
                canvas.drawRect(paddingLeft, height - mUnderlineHeight, mTabsContainer.getWidth() + paddingLeft, height, mRectPaint);
            } else {
                canvas.drawRect(paddingLeft, 0, mTabsContainer.getWidth() + paddingLeft, mUnderlineHeight, mRectPaint);
            }
        }

        //draw indicator line
        calcIndicatorRect();
        if (mIndicatorStyle == STYLE_TRIANGLE) {
            if (mIndicatorHeight > 0) {
                mTrianglePaint.setColor(mIndicatorColor);
                mTrianglePath.reset();
                mTrianglePath.moveTo(paddingLeft + mIndicatorRect.left, height);
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.left / 2f + mIndicatorRect.right / 2f, height - mIndicatorHeight);
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.right, height);
                mTrianglePath.close();
                canvas.drawPath(mTrianglePath, mTrianglePaint);
            }
        } else if (mIndicatorStyle == STYLE_BLOCK) {
            if (mIndicatorHeight < 0) {
                mIndicatorHeight = height - mIndicatorMarginTop - mIndicatorMarginBottom;
            }

            if (mIndicatorHeight > 0) {
                if (mIndicatorCornerRadius < 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
                    mIndicatorCornerRadius = mIndicatorHeight / 2;
                }
                if (mIndicatorBackgroundResId == 0) {
                    if (mIndicatorStartColor != 0 && mIndicatorEndColor != 0) {
                        mIndicatorDrawable.setColors(new int[]{mIndicatorStartColor, mIndicatorEndColor});
                        if (mIndicatorGradientDirection == 0) {
                            mIndicatorDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                        } else {
                            mIndicatorDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                        }
                    } else {
                        mIndicatorDrawable.setColor(mIndicatorColor);
                    }
                    mIndicatorDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                            (int) mIndicatorMarginTop, (int) (paddingLeft + mIndicatorRect.right - mIndicatorMarginRight),
                            (int) (mIndicatorMarginTop + mIndicatorHeight));
                    mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
                    mIndicatorDrawable.draw(canvas);

                    mIndicatorPaint.setColor(mIndicatorColor);

                } else {
                    mIndicatorRect.top = (int) mIndicatorMarginTop;
                    mIndicatorRect.bottom = (int) (mIndicatorMarginTop + mIndicatorHeight);
                    if (mIndicatorBackgroundDrawable != null){
                        mIndicatorBackgroundDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                                mIndicatorRect.top,
                                (int) (paddingLeft + mIndicatorRect.right - mIndicatorMarginRight),
                                mIndicatorRect.bottom);
                        mIndicatorBackgroundDrawable.draw(canvas);
                    }
                }
            }
        } else if (mIndicatorHeight > 0) {
            if (mIndicatorBackgroundResId == 0) {
                if (mIndicatorStartColor != 0 && mIndicatorEndColor != 0) {
                    mIndicatorDrawable.setColors(new int[]{mIndicatorStartColor, mIndicatorEndColor});
                    if (mIndicatorGradientDirection == 0) {
                        mIndicatorDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    } else {
                        mIndicatorDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                    }
                } else {
                    mIndicatorDrawable.setColor(mIndicatorColor);
                }
                if (mIndicatorGravity == Gravity.BOTTOM) {
                    mIndicatorDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                            height - (int) mIndicatorHeight - (int) mIndicatorMarginBottom,
                            paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
                            height - (int) mIndicatorMarginBottom);
                } else {
                    mIndicatorDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                            (int) mIndicatorMarginTop,
                            paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
                            (int) mIndicatorHeight + (int) mIndicatorMarginTop);
                }
                mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
                mIndicatorDrawable.draw(canvas);
            } else {
                if (mIndicatorGravity == Gravity.BOTTOM) {
                    mIndicatorRect.top = (int) mIndicatorMarginTop;
                    mIndicatorRect.bottom = (int) (mIndicatorMarginTop + mIndicatorHeight);
                    if (mIndicatorBackgroundDrawable != null){
                        mIndicatorBackgroundDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                                height - (int) mIndicatorHeight - (int) mIndicatorMarginBottom,
                                paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
                                height - (int) mIndicatorMarginBottom);
                        mIndicatorBackgroundDrawable.draw(canvas);
                    }

                } else {
                    mIndicatorRect.top = (int) mIndicatorMarginTop;
                    mIndicatorRect.bottom = (int) (mIndicatorMarginTop + mIndicatorHeight);
                    if (mIndicatorBackgroundDrawable != null){
                        mIndicatorBackgroundDrawable.setBounds(paddingLeft + (int) mIndicatorMarginLeft + mIndicatorRect.left,
                                (int) mIndicatorMarginTop,
                                paddingLeft + mIndicatorRect.right - (int) mIndicatorMarginRight,
                                (int) mIndicatorHeight + (int) mIndicatorMarginTop);
                        mIndicatorBackgroundDrawable.draw(canvas);
                    }
                }
            }
        }
    }

    public void setCurrentTab(int currentTab) {
        this.mCurrentTab = currentTab;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(currentTab);
        } else {
            updateTabSelection(currentTab);
        }
    }

    public void setCurrentTab(int currentTab, boolean smoothScroll) {
        this.mCurrentTab = currentTab;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(currentTab, smoothScroll);
        } else {
            updateTabSelection(currentTab);
        }
    }

    public void setIndicatorStyle(int indicatorStyle) {
        this.mIndicatorStyle = indicatorStyle;
        invalidate();
    }

    public void setTabPadding(float tabPadding) {
        this.mTabPadding = dp2px(tabPadding);
        updateTabStyles();
    }

    public void setTabSpaceEqual(boolean tabSpaceEqual) {
        this.mTabSpaceEqual = tabSpaceEqual;
        updateTabStyles();
    }

    public void setTabWidthDp(float tabWidth) {
        this.mTabWidth = dp2px(tabWidth);
        updateTabStyles();
    }

    public void setTabWidth(float tabWidth) {
        this.mTabWidth = tabWidth;
        updateTabStyles();
    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        invalidate();
    }

    public void setGradientIndicatorDrawable(int resId){
        this.mIndicatorBackgroundResId = resId;
        mIndicatorBackgroundDrawable = null;
        try {
            mIndicatorBackgroundDrawable = ContextCompat.getDrawable(getContext(), mIndicatorBackgroundResId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidate();
    }

    public void setIndicatorHeight(float indicatorHeight) {
        this.mIndicatorHeight = dp2px(indicatorHeight);
        invalidate();
    }

    public void setIndicatorWidth(float indicatorWidth) {
        this.mIndicatorWidth = dp2px(indicatorWidth);
        invalidate();
    }

    public void setIndicatorCornerRadius(float indicatorCornerRadius) {
        this.mIndicatorCornerRadius = dp2px(indicatorCornerRadius);
        invalidate();
    }

    public void setIndicatorGravity(int indicatorGravity) {
        this.mIndicatorGravity = indicatorGravity;
        invalidate();
    }

    public void setIndicatorMargin(float indicatorMarginLeft, float indicatorMarginTop,
                                   float indicatorMarginRight, float indicatorMarginBottom) {
        this.mIndicatorMarginLeft = dp2px(indicatorMarginLeft);
        this.mIndicatorMarginTop = dp2px(indicatorMarginTop);
        this.mIndicatorMarginRight = dp2px(indicatorMarginRight);
        this.mIndicatorMarginBottom = dp2px(indicatorMarginBottom);
        invalidate();
    }

    public void setIndicatorWidthEqualTitle(boolean indicatorWidthEqualTitle) {
        this.mIndicatorWidthEqualTitle = indicatorWidthEqualTitle;
        invalidate();
    }

    public void setUnderlineColor(int underlineColor) {
        this.mUnderlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineHeight(float underlineHeight) {
        this.mUnderlineHeight = dp2px(underlineHeight);
        invalidate();
    }

    public void setUnderlineGravity(int underlineGravity) {
        this.mUnderlineGravity = underlineGravity;
        invalidate();
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        invalidate();
    }

    public void setDividerWidth(float dividerWidth) {
        this.mDividerWidth = dp2px(dividerWidth);
        invalidate();
    }

    public void setDividerPadding(float dividerPadding) {
        this.mDividerPadding = dp2px(dividerPadding);
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.mTextSize = sp2px(textSize);
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

    public void setTextAllCaps(boolean textAllCaps) {
        this.mTextAllCaps = textAllCaps;
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

    public int getIndicatorStyle() {
        return mIndicatorStyle;
    }

    public float getTabPadding() {
        return mTabPadding;
    }

    public boolean isTabSpaceEqual() {
        return mTabSpaceEqual;
    }

    public float getTabWidth() {
        return mTabWidth;
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public float getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public float getIndicatorWidth() {
        return mIndicatorWidth;
    }

    public float getIndicatorCornerRadius() {
        return mIndicatorCornerRadius;
    }

    public float getIndicatorMarginLeft() {
        return mIndicatorMarginLeft;
    }

    public float getIndicatorMarginTop() {
        return mIndicatorMarginTop;
    }

    public float getIndicatorMarginRight() {
        return mIndicatorMarginRight;
    }

    public float getIndicatorMarginBottom() {
        return mIndicatorMarginBottom;
    }

    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    public float getUnderlineHeight() {
        return mUnderlineHeight;
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public float getDividerWidth() {
        return mDividerWidth;
    }

    public float getDividerPadding() {
        return mDividerPadding;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public int getTextSelectColor() {
        return mTextSelectColor;
    }

    public int getTextUnSelectColor() {
        return mTextUnSelectColor;
    }

    public int getTextBold() {
        return mTextBold;
    }

    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    public TextView getTitleView(int tab) {
        View tabView = mTabsContainer.getChildAt(tab);
        return tabView.findViewById(R.id.tv_tab_title);
    }

    //setter and getter

    // show MsgTipView
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SparseArray<Boolean> mInitSetMap = new SparseArray<>();

    /**
     * 显示未读消息
     *
     * @param position 显示tab位置
     * @param num      num小于等于0显示红点,num大于0显示数字
     */
    public void showMsg(int position, int num) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
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

    /**
     * 显示未读红点
     *
     * @param position 显示tab位置
     */
    public void showDot(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        showMsg(position, 0);
    }

    /**
     * 隐藏未读消息
     */
    public void hideMsg(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        MsgView tipView = tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            tipView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置未读消息偏移,原点为文字的右上角.当控件高度固定,消息提示位置易控制,显示效果佳
     */
    public void setMsgMargin(int position, float leftPadding, float bottomPadding) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        MsgView tipView = tabView.findViewById(R.id.rtv_msg_tip);
        if (tipView != null) {
            TextView tv_tab_title = tabView.findViewById(R.id.tv_tab_title);
            mTextPaint.setTextSize(mTextSize);
            float textWidth = mTextPaint.measureText(tv_tab_title.getText().toString());
            float textHeight = mTextPaint.descent() - mTextPaint.ascent();
            MarginLayoutParams lp = (MarginLayoutParams) tipView.getLayoutParams();
            lp.leftMargin = mTabWidth >= 0 ? (int) (mTabWidth / 2 + textWidth / 2 + dp2px(leftPadding)) : (int) (mTabPadding + textWidth + dp2px(leftPadding));
            lp.topMargin = mHeight > 0 ? (int) (mHeight - textHeight) / 2 - dp2px(bottomPadding) : 0;
            tipView.setLayoutParams(lp);
        }
    }

    /**
     * 隐藏未读消息
     */
    /*public void setVisibleRightIcon(int position,boolean visibleRightIcon,int resId) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        ImageView tipView = tabView.findViewById(R.id.ivRightIcon);

        if (tipView != null) {
            tipView.setImageResource(resId);
            tipView.setVisibility(visibleRightIcon?View.VISIBLE : View.GONE);
        }
    }*/

    /**
     * 设置右边图标
     */
    /*public void setRightIconMargin(int position, float leftMargin, float bottomMargin) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        ImageView ivRightIcon = tabView.findViewById(R.id.ivRightIcon);
        if (ivRightIcon != null) {
            mTextPaint.setTextSize(mTextSize);
            MarginLayoutParams lp = (MarginLayoutParams) ivRightIcon.getLayoutParams();
            lp.leftMargin = dp2px(leftMargin);
            lp.bottomMargin = dp2px(bottomMargin);
            ivRightIcon.setLayoutParams(lp);
        }
    }*/

    /**
     * 当前类只提供了少许设置未读消息属性的方法,可以通过该方法获取MsgView对象从而各种设置
     */
    public MsgView getMsgView(int position) {
        if (position >= mTabCount) {
            position = mTabCount - 1;
        }
        View tabView = mTabsContainer.getChildAt(position);
        return tabView.findViewById(R.id.rtv_msg_tip);
    }

    private OnTabSelectListener mListener;

    public void setOnTabSelectListener(OnTabSelectListener listener) {
        this.mListener = listener;
    }

    private OnScrollListener scrollListener;

    private void onScroll(){
        if(scrollListener==null){
            return;
        }
        int scrollX=getScrollX();
        int width=getWidth();
        int scrollViewMeasuredWidth=getChildAt(0).getMeasuredWidth();

        if(scrollX==0){
            // 滑倒头部
            scrollListener.onScrollStart(this,scrollX);
        }else if((scrollX+width)>=scrollViewMeasuredWidth){
            // 滑倒尾部
            scrollListener.onScrollEnd(this,scrollX);
        }else{
            scrollListener.onScrolling(this,scrollX);
        }

    }

    public void setOnScrollListener(OnScrollListener listener){
        this.scrollListener=listener;
    }

    public interface OnScrollListener{

        void onScrolling(View view,int scroll);
        void onScrollStart(View view,int scroll);
        void onScrollEnd(View view,int scroll);
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

    /**
     * 属性是 boolean 时 true 的值是 -1，对应选中才加粗。
     */
    private static int resolveTextBold(int value) {
        if (value == -1 || value == TEXT_BOLD_WHEN_SELECT) {
            return TEXT_BOLD_WHEN_SELECT;
        }
        if (value == TEXT_BOLD_BOTH) {
            return TEXT_BOLD_BOTH;
        }
        return TEXT_BOLD_NONE;
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
        } else if (mViewPager != null && mViewPager.getAdapter() != null && position < mViewPager.getAdapter().getCount()) {
            return mViewPager.getAdapter().getPageTitle(position);
        } else {
            return "";
        }
    }

    public void setTabLayoutGravity(int gravity) {
        if (mTabsContainer != null) {
            mTabsContainer.setGravity(gravity);
        }
    }

}
