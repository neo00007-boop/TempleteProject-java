package com.flyco.tablayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 基于 {@link SlidingTabLayout} 的胶囊指示器 Tab：
 * <ul>
 *   <li>指示器仅支持纯色或背景图（{@code tl_indicator_color} / {@code tl_indicator_background}）</li>
 *   <li>支持设置宽高（{@code tl_indicator_width} / {@code tl_indicator_height}）</li>
 *   <li>指示器与 item 文字中心对齐（在 Tab 内垂直、水平居中）</li>
 * </ul>
 * 其余行为与 API 同 {@link SlidingTabLayout}：关联页面用 {@code setViewPager}，只展示标题用 {@code setTitles}。
 */
public class CenterBlockSlidingTabLayout extends SlidingTabLayout {

    private final Rect mCenterIndicatorRect = new Rect();
    private final RectF mTempBounds = new RectF();
    private final Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mUnderlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextMeasurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float mScrollPositionOffset;
    private int mUnderlineGravity = Gravity.BOTTOM;
    private int mSelectedTab;
    private float mTextScale = 1f;
    private Drawable mIndicatorBackgroundDrawable;

    public CenterBlockSlidingTabLayout(Context context) {
        this(context, null);
    }

    public CenterBlockSlidingTabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CenterBlockSlidingTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readUnderlineGravity(context, attrs);
        readIndicatorBackground(context, attrs);
        ensureTabsVerticalCenter();
    }

    private void readIndicatorBackground(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        android.content.res.TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);
        int backgroundResId = ta.getResourceId(R.styleable.SlidingTabLayout_tl_indicator_background, 0);
        mTextScale = ta.getFloat(R.styleable.SlidingTabLayout_tl_textScale, 1f);
        ta.recycle();
        if (backgroundResId != 0) {
            mIndicatorBackgroundDrawable = ContextCompat.getDrawable(context, backgroundResId);
        }
    }

    private void readUnderlineGravity(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        android.content.res.TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);
        mUnderlineGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_underline_gravity, Gravity.BOTTOM);
        ta.recycle();
    }

    private void ensureTabsVerticalCenter() {
        if (getChildCount() <= 0 || !(getChildAt(0) instanceof LinearLayout)) {
            return;
        }
        LinearLayout tabsContainer = (LinearLayout) getChildAt(0);
        ViewGroup.LayoutParams lp = tabsContainer.getLayoutParams();
        if (lp != null && lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            tabsContainer.setLayoutParams(lp);
        }
        tabsContainer.setGravity(Gravity.CENTER_VERTICAL);
        tabsContainer.setClipChildren(false);
        applyTabTitleStyle();
    }

    /**
     * 关闭字体顶部预留、保证文字在 Tab 内垂直居中，避免看起来偏下。
     */
    private void applyTabTitleStyle() {
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            TextView tv = getTitleView(i);
            if (tv == null) {
                continue;
            }
            tv.setIncludeFontPadding(false);
            tv.setGravity(Gravity.CENTER);
            float scale = i == mSelectedTab ? mTextScale : 1f;
            tv.setScaleX(scale);
            tv.setScaleY(scale);
        }
    }

    private float getTitleVisualCenterY(TextView titleView) {
        if (titleView.getHeight() <= 0) {
            return titleView.getTop() + titleView.getPaddingTop();
        }
        // 几何中心更稳定；再与字体视觉中心做折中，减少不同字体下的“偏上/偏下”感。
        float geometricCenter = titleView.getTop() + titleView.getHeight() / 2f;
        Paint.FontMetrics fm = titleView.getPaint().getFontMetrics();
        float baseline = titleView.getBaseline();
        if (baseline > 0) {
            float fontVisualCenter = titleView.getTop() + baseline + (fm.descent + fm.ascent) / 2f;
            return (geometricCenter + fontVisualCenter) / 2f;
        }
        return geometricCenter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mScrollPositionOffset = positionOffset;
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        mSelectedTab = position;
        super.onPageSelected(position);
        applyTabTitleStyle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 不调用 SlidingTabLayout.onDraw，避免原 indicator 绘制逻辑（与文字无法居中对齐）
        if (isInEditMode() || getTabCount() <= 0) {
            return;
        }

        ViewGroup tabsContainer = getTabsContainer();
        if (tabsContainer == null) {
            return;
        }

        int height = getHeight();
        int paddingLeft = getPaddingLeft();

        drawDivider(canvas, tabsContainer, height, paddingLeft);
        drawUnderline(canvas, tabsContainer, height, paddingLeft);
        calcCenterBlockIndicatorRect(tabsContainer);
        drawCenterBlockIndicator(canvas, paddingLeft);
    }

    @Nullable
    private ViewGroup getTabsContainer() {
        if (getChildCount() <= 0) {
            return null;
        }
        View child = getChildAt(0);
        return child instanceof ViewGroup ? (ViewGroup) child : null;
    }

    private void drawDivider(Canvas canvas, ViewGroup tabsContainer, int height, int paddingLeft) {
        float dividerWidth = getDividerWidth();
        if (dividerWidth <= 0) {
            return;
        }
        mDividerPaint.setStrokeWidth(dividerWidth);
        mDividerPaint.setColor(getDividerColor());
        float dividerPadding = getDividerPadding();
        int tabCount = getTabCount();
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(paddingLeft + tab.getRight(), dividerPadding,
                    paddingLeft + tab.getRight(), height - dividerPadding, mDividerPaint);
        }
    }

    private void drawUnderline(Canvas canvas, ViewGroup tabsContainer, int height, int paddingLeft) {
        float underlineHeight = getUnderlineHeight();
        if (underlineHeight <= 0) {
            return;
        }
        mUnderlinePaint.setColor(getUnderlineColor());
        if (mUnderlineGravity == Gravity.BOTTOM) {
            canvas.drawRect(paddingLeft, height - underlineHeight,
                    tabsContainer.getWidth() + paddingLeft, height, mUnderlinePaint);
        } else {
            canvas.drawRect(paddingLeft, 0,
                    tabsContainer.getWidth() + paddingLeft, underlineHeight, mUnderlinePaint);
        }
    }

    /**
     * 以 TextView 中心为基准计算指示器区域，并在滑动时插值。
     */
    private void calcCenterBlockIndicatorRect(ViewGroup tabsContainer) {
        int tab = getCurrentTab();
        int tabCount = getTabCount();
        if (tab < 0 || tab >= tabCount) {
            return;
        }

        View currentTab = tabsContainer.getChildAt(tab);
        TextView currentTv = currentTab.findViewById(R.id.tv_tab_title);
        if (currentTv == null) {
            return;
        }

        RectF current = getIndicatorBoundsInTab(currentTab, currentTv);
        float left = currentTab.getLeft() + current.left;
        float top = currentTab.getTop() + current.top;
        float right = currentTab.getLeft() + current.right;
        float bottom = currentTab.getTop() + current.bottom;

        if (tab < tabCount - 1 && mScrollPositionOffset > 0f) {
            View nextTab = tabsContainer.getChildAt(tab + 1);
            TextView nextTv = nextTab.findViewById(R.id.tv_tab_title);
            if (nextTv != null) {
                RectF next = getIndicatorBoundsInTab(nextTab, nextTv);
                float nLeft = nextTab.getLeft() + next.left;
                float nTop = nextTab.getTop() + next.top;
                float nRight = nextTab.getLeft() + next.right;
                float nBottom = nextTab.getTop() + next.bottom;
                float offset = mScrollPositionOffset;
                left = left + (nLeft - left) * offset;
                top = top + (nTop - top) * offset;
                right = right + (nRight - right) * offset;
                bottom = bottom + (nBottom - bottom) * offset;
            }
        }

        mCenterIndicatorRect.left = Math.round(left);
        mCenterIndicatorRect.top = Math.round(top);
        mCenterIndicatorRect.right = Math.round(right);
        mCenterIndicatorRect.bottom = Math.round(bottom);
    }

    private RectF getIndicatorBoundsInTab(View tab, TextView titleView) {
        float cx = titleView.getLeft() + titleView.getWidth() / 2f;
        float cy = getTitleVisualCenterY(titleView);

        float indicatorW = getIndicatorWidth();
        float indicatorH = getIndicatorHeight();

        if (indicatorW <= 0) {
            mTextMeasurePaint.setTextSize(titleView.getTextSize());
            float textW = mTextMeasurePaint.measureText(titleView.getText().toString());
            indicatorW = textW + getIndicatorMarginLeft() + getIndicatorMarginRight();
        }
        if (indicatorH <= 0) {
            float textH = titleView.getHeight();
            if (textH <= 0) {
                mTextMeasurePaint.setTextSize(titleView.getTextSize());
                textH = mTextMeasurePaint.descent() - mTextMeasurePaint.ascent();
            }
            indicatorH = textH + getIndicatorMarginTop() + getIndicatorMarginBottom();
        }

        // margin 在 block 模式下用于微调指示器中心位置（便于处理背景图视觉偏移）
        float offsetX = (getIndicatorMarginLeft() - getIndicatorMarginRight()) / 2f;
        float offsetY = (getIndicatorMarginTop() - getIndicatorMarginBottom()) / 2f;

        mTempBounds.left = cx - indicatorW / 2f + offsetX;
        mTempBounds.top = cy - indicatorH / 2f + offsetY;
        mTempBounds.right = cx + indicatorW / 2f + offsetX;
        mTempBounds.bottom = cy + indicatorH / 2f + offsetY;
        return mTempBounds;
    }

    private void drawCenterBlockIndicator(Canvas canvas, int paddingLeft) {
        if (mCenterIndicatorRect.width() <= 0 || mCenterIndicatorRect.height() <= 0) {
            return;
        }

        int left = paddingLeft + mCenterIndicatorRect.left;
        int top = mCenterIndicatorRect.top;
        int right = paddingLeft + mCenterIndicatorRect.right;
        int bottom = mCenterIndicatorRect.bottom;

        float corner = getIndicatorCornerRadius();
        if (corner < 0) {
            corner = (bottom - top) / 2f;
        }

        if (mIndicatorBackgroundDrawable != null) {
            mIndicatorBackgroundDrawable.setBounds(left, top, right, bottom);
            mIndicatorBackgroundDrawable.draw(canvas);
            return;
        }

        mIndicatorDrawable.setColor(getIndicatorColor());
        mIndicatorDrawable.setCornerRadius(corner);
        mIndicatorDrawable.setBounds(left, top, right, bottom);
        mIndicatorDrawable.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        applyTabTitleStyle();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (getTabCount() > 0 && mSelectedTab >= getTabCount()) {
            mSelectedTab = getTabCount() - 1;
        }
        ensureTabsVerticalCenter();
    }
}
