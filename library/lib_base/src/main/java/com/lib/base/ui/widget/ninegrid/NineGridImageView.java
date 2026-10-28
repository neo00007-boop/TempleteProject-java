package com.lib.base.ui.widget.ninegrid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lib.base.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaeger on 16/2/24.
 * <p>
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public class NineGridImageView<T> extends ViewGroup {
    public final static int STYLE_GRID = 0;     // 宫格布局
    public final static int STYLE_FILL = 1;     // 全填充布局
    ///////////////////////////////////////////////////////////////////////////
    // 跨行跨列的类型
    ///////////////////////////////////////////////////////////////////////////
    public final static int NOSPAN = 0;         // 不跨行也不跨列
    public final static int TOPCOLSPAN = 2;     // 首行跨列
    public final static int BOTTOMCOLSPAN = 3;  // 末行跨列
    public final static int LEFTROWSPAN = 4;    // 首列跨行

    private int mRowCount;                      // 行数
    private int mColumnCount;                   // 列数

    private int mMaxSize;                       // 最大图片数
    private int mShowStyle;                     // 显示风格
    private int mGap;                           // 宫格间距
    private int mSingleImgSize;                 // 单张时可选固定边长；未设则按微信：2*cell+gap
    private int mGridSize;                      // 宫格大小（微信：以三列 cell 为基准）
    private int mSpanType;                      // 跨行跨列的类型
    private float mCornerRadius;                // 图片圆角 px
    private int mMaskColor;                     // 图片蒙层色
    private int mMoreMaskColor;                 // 超过 maxSize 时末格蒙层色
    private int mMoreTextColor = Color.WHITE;   // 「n+」文字色
    private int mMoreTextBgColor = Color.parseColor("#99000000"); // 「n+」背景色
    private float mMoreTextCornerRadius;        // 「n+」背景圆角 px
    private float mMoreTextSize;                // 「n+」文字大小 px
    private int mMoreMarginEnd;                 // 「n+」右边距 px
    private int mMoreMarginBottom;              // 「n+」下边距 px
    private int mMorePaddingHorizontal;         // 「n+」左右内边距 px
    private int mMorePaddingVertical;           // 「n+」上下内边距 px

    private List<NineGridItemView> mImageViewList = new ArrayList<>();
    private List<T> mImgDataList;

    private NineGridImageViewAdapter<T> mAdapter;
    private ItemImageClickListener<T> mItemImageClickListener;
    private ItemImageLongClickListener<T> mItemImageLongClickListener;

    public NineGridImageView(Context context) {
        this(context, null);
    }

    public NineGridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NineGridImageView);
        this.mGap = (int) typedArray.getDimension(R.styleable.NineGridImageView_imgGap, 0);
        this.mSingleImgSize = typedArray.getDimensionPixelSize(R.styleable.NineGridImageView_singleImgSize, -1);
        this.mShowStyle = typedArray.getInt(R.styleable.NineGridImageView_showStyle, STYLE_GRID);
        this.mMaxSize = typedArray.getInt(R.styleable.NineGridImageView_maxSize, 9);
        float density = context.getResources().getDisplayMetrics().density;
        this.mCornerRadius = typedArray.getDimension(R.styleable.NineGridImageView_ngiv_cornerRadius, 0f);
        this.mMaskColor = typedArray.getColor(R.styleable.NineGridImageView_ngiv_maskColor, Color.TRANSPARENT);
        this.mMoreMaskColor = typedArray.getColor(R.styleable.NineGridImageView_ngiv_moreMaskColor, Color.parseColor("#66000000"));
        this.mMoreTextColor = typedArray.getColor(R.styleable.NineGridImageView_ngiv_moreTextColor, Color.WHITE);
        this.mMoreTextBgColor = typedArray.getColor(R.styleable.NineGridImageView_ngiv_moreTextBgColor, Color.parseColor("#99000000"));
        this.mMoreTextCornerRadius = typedArray.getDimension(
                R.styleable.NineGridImageView_ngiv_moreTextCornerRadius, 4f * density);
        this.mMoreTextSize = typedArray.getDimension(
                R.styleable.NineGridImageView_ngiv_moreTextSize, 16f * density);
        int defaultMoreMargin = Math.round(6f * density);
        this.mMoreMarginEnd = typedArray.getDimensionPixelSize(
                R.styleable.NineGridImageView_ngiv_moreMarginEnd, defaultMoreMargin);
        this.mMoreMarginBottom = typedArray.getDimensionPixelSize(
                R.styleable.NineGridImageView_ngiv_moreMarginBottom, defaultMoreMargin);
        this.mMorePaddingHorizontal = typedArray.getDimensionPixelSize(
                R.styleable.NineGridImageView_ngiv_morePaddingHorizontal, Math.round(6f * density));
        this.mMorePaddingVertical = typedArray.getDimensionPixelSize(
                R.styleable.NineGridImageView_ngiv_morePaddingVertical, Math.round(2f * density));
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int totalWidth = width - getPaddingLeft() - getPaddingRight();
        if (mImgDataList != null && !mImgDataList.isEmpty()) {
            if (mColumnCount <= 0) {
                mColumnCount = 1;
            }
            if (!mImageViewList.isEmpty()) {
                mImageViewList.get(0).getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            // 微信朋友圈：始终以三列格子为基准 cell = (W - 2*gap) / 3
            // 1 张 ≈ 2*cell+gap；2/4 张占两列宽；3/5–9 张满三列
            int cellSize = Math.max(0, (totalWidth - mGap * 2) / 3);
            int showCount = getNeedShowCount(mImgDataList.size());
            if (showCount == 1) {
                if (mSingleImgSize > 0) {
                    mGridSize = Math.min(mSingleImgSize, totalWidth);
                } else {
                    mGridSize = cellSize * 2 + mGap;
                }
            } else {
                mGridSize = cellSize;
            }
            height = mGridSize * mRowCount + mGap * (mRowCount - 1) + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildrenView();
    }

    private void layoutChild(NineGridItemView child, int left, int top, int right, int bottom) {
        child.measure(
                MeasureSpec.makeMeasureSpec(Math.max(0, right - left), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Math.max(0, bottom - top), MeasureSpec.EXACTLY));
        child.layout(left, top, right, bottom);
    }

    /**
     * 根据照片数量和span类型来对子视图进行动态排版布局
     */
    private void layoutChildrenView() {
        if (mImgDataList == null) return;
        int showChildrenCount = getNeedShowCount(mImgDataList.size());
        //对不跨行不跨列的进行排版布局,单张或者2张默认进行普通排版
        if (mSpanType == NOSPAN || showChildrenCount <= 2) {
            layoutForNoSpanChildrenView(showChildrenCount);
            return;
        }
        switch (showChildrenCount) {
            case 3:
                layoutForThreeChildrenView(showChildrenCount);
                break;
            case 4:
                layoutForFourChildrenView(showChildrenCount);
                break;
            case 5:
                layoutForFiveChildrenView(showChildrenCount);
                break;
            case 6:
                layoutForSixChildrenView(showChildrenCount);
                break;
            default:
                layoutForNoSpanChildrenView(showChildrenCount);
                break;
        }
    }

    private void layoutForNoSpanChildrenView(int childrenCount) {
        if (childrenCount <= 0) return;
        int row, column, left, top, right, bottom;
        for (int i = 0; i < childrenCount; i++) {
            NineGridItemView childrenView = (NineGridItemView) getChildAt(i);
            row = i / mColumnCount;
            column = i % mColumnCount;
            left = (mGridSize + mGap) * column + getPaddingLeft();
            top = (mGridSize + mGap) * row + getPaddingTop();
            right = left + mGridSize;
            bottom = top + mGridSize;
            layoutChild(childrenView, left, top, right, bottom);
        }
    }

    private void layoutForThreeChildrenView(int childrenCount) {
        int left, top, right, bottom;
        for (int i = 0; i < childrenCount; i++) {
            NineGridItemView childrenView = (NineGridItemView) getChildAt(i);
            switch (mSpanType) {
                case TOPCOLSPAN:    //2行2列,首行跨列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + mGridSize;
                    } else if (i == 1) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case BOTTOMCOLSPAN: //2行2列,末行跨列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case LEFTROWSPAN:   //2行2列,首列跨行
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                default:
                    break;
            }
        }
    }

    private void layoutForFourChildrenView(int childrenCount) {
        int left, top, right, bottom;
        for (int i = 0; i < childrenCount; i++) {
            NineGridItemView childrenView = (NineGridItemView) getChildAt(i);
            switch (mSpanType) {
                case TOPCOLSPAN:    //3行3列,首行跨2行3列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize * 3 + mGap * 2;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 1) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case BOTTOMCOLSPAN: //3行3列,末行跨2行3列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize * 3 + mGap * 2;
                        bottom = top + mGridSize * 2 + mGap;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case LEFTROWSPAN:   //3行3列,首列跨3行2列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + mGridSize * 3 + mGap * 2;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                default:
                    break;
            }
        }
    }

    private void layoutForFiveChildrenView(int childrenCount) {
        int left, top, right, bottom;
        for (int i = 0; i < childrenCount; i++) {
            NineGridItemView childrenView = (NineGridItemView) getChildAt(i);
            switch (mSpanType) {
                case TOPCOLSPAN:    //3行3列,首行跨2行,2列跨3列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + (mGridSize * 3 + mGap) / 2;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 1) {
                        left = getPaddingLeft() + (mGridSize * 3 + mGap) / 2 + mGap;
                        top = getPaddingTop();
                        right = left + (mGridSize * 3 + mGap) / 2;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 2) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 3) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case BOTTOMCOLSPAN: //3行3列,末行跨2行,2列跨3列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 3) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + (mGridSize * 3 + mGap) / 2;
                        bottom = top + mGridSize * 2 + mGap;
                    } else {
                        left = getPaddingLeft() + (mGridSize * 3 + mGap) / 2 + mGap;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + (mGridSize * 3 + mGap) / 2;
                        bottom = top + mGridSize * 2 + mGap;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case LEFTROWSPAN:   //3行3列,2行跨3行，1列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + (mGridSize * 3 + mGap) / 2;
                    } else if (i == 1) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + (mGridSize * 3 + mGap) / 2 + mGap;
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + (mGridSize * 3 + mGap) / 2;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 3) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                default:
                    break;
            }
        }
    }

    private void layoutForSixChildrenView(int childrenCount) {
        int left, top, right, bottom;
        for (int i = 0; i < childrenCount; i++) {
            NineGridItemView childrenView = (NineGridItemView) getChildAt(i);
            switch (mSpanType) {
                case TOPCOLSPAN:    //3行3列,第一张跨2行2列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 3) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 4) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case BOTTOMCOLSPAN: //3行3列,第4张跨2行2列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 2) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 3) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 4) {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                case LEFTROWSPAN:   //3行3列,第2张跨2行2列
                    if (i == 0) {
                        left = getPaddingLeft();
                        top = getPaddingTop();
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 1) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop();
                        right = left + mGridSize * 2 + mGap;
                        bottom = top + mGridSize * 2 + mGap;
                    } else if (i == 2) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize + mGap;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 3) {
                        left = getPaddingLeft();
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else if (i == 4) {
                        left = getPaddingLeft() + mGridSize + mGap;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    } else {
                        left = getPaddingLeft() + mGridSize * 2 + mGap * 2;
                        top = getPaddingTop() + mGridSize * 2 + mGap * 2;
                        right = left + mGridSize;
                        bottom = top + mGridSize;
                    }
                    layoutChild(childrenView, left, top, right, bottom);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 根据跨行跨列的类型，以及图片数量，来确定单元格的行数和列数
     *
     * @param imagesSize 图片数量
     * @param gridParam  单元格的行数和列数
     */
    private void generatUnitRowAndColumnForSpanType(int imagesSize, int[] gridParam) {
        if (imagesSize <= 2) {
            gridParam[0] = 1;
            gridParam[1] = imagesSize;
        } else if (imagesSize == 3) {
            switch (mSpanType) {
                case TOPCOLSPAN:    //2行2列,首行跨列
                case BOTTOMCOLSPAN: //2行2列,末行跨列
                case LEFTROWSPAN:   //2行2列,首列跨行
                    gridParam[0] = 2;
                    gridParam[1] = 2;
                    break;
                case NOSPAN:    //1行3列
                default:
                    gridParam[0] = 1;
                    gridParam[1] = 3;
                    break;
            }
        } else if (imagesSize <= 6) {
            switch (mSpanType) {
                case TOPCOLSPAN:    //3行3列,首行跨列
                case BOTTOMCOLSPAN: //3行3列,末行跨列
                case LEFTROWSPAN:   //3行3列,首列跨行
                    gridParam[0] = 3;
                    gridParam[1] = 3;
                    break;
                case NOSPAN:    //2行
                default:
                    gridParam[0] = 2;
                    gridParam[1] = imagesSize / 2 + imagesSize % 2;
                    break;
            }
        } else {
            gridParam[0] = imagesSize / 3 + (imagesSize % 3 == 0 ? 0 : 1);
            gridParam[1] = 3;
        }
    }

    public void setImagesData(List<T> lists) {
        setImagesData(lists, NOSPAN);
    }

    /**
     * 设置图片数据
     *
     * @param lists    图片数据集合
     * @param spanType 跨行跨列排版类型
     */
    public void setImagesData(List<T> lists, int spanType) {
        if (lists == null || lists.isEmpty()) {
            mImgDataList = null;
            removeAllViews();
            // 保留 mImageViewList 以便复用
            setVisibility(GONE);
            mRowCount = 0;
            mColumnCount = 0;
            requestLayout();
            return;
        }
        setVisibility(VISIBLE);
        this.mSpanType = spanType;
        int newShowCount = getNeedShowCount(lists.size());

        int[] gridParam = calculateGridParam(newShowCount, mShowStyle);
        mRowCount = gridParam[0];
        mColumnCount = gridParam[1];
        // 子 View 数量与展示数对齐（池中 View 复用）
        int childCount = getChildCount();
        if (childCount > newShowCount) {
            removeViews(newShowCount, childCount - newShowCount);
        } else if (childCount < newShowCount) {
            for (int i = childCount; i < newShowCount; i++) {
                NineGridItemView iv = getImageView(i);
                if (iv == null) {
                    return;
                }
                addView(iv, generateDefaultLayoutParams());
            }
        }
        mImgDataList = lists;
        bindAllItems();
        requestLayout();
    }

    /** 数据/样式变化时绑定，避免在 onLayout 里重复 Glide 加载。 */
    private void bindAllItems() {
        if (mAdapter == null || mImgDataList == null) {
            return;
        }
        int showCount = getNeedShowCount(mImgDataList.size());
        for (int i = 0; i < showCount; i++) {
            View child = getChildAt(i);
            if (child instanceof NineGridItemView) {
                bindItem((NineGridItemView) child, i);
            }
        }
    }

    /** 仅刷新样式（圆角/蒙层/角标），不重新走 Adapter 加载时可调用；样式 setter 会走完整 bind。 */
    private void rebindStylesAndImages() {
        if (mImgDataList == null || mImgDataList.isEmpty()) {
            return;
        }
        bindAllItems();
        requestLayout();
    }

    private int getNeedShowCount(int size) {
        if (mMaxSize > 0 && size > mMaxSize) {
            return mMaxSize;
        } else {
            return size;
        }
    }

    /**
     * 获得 ImageView
     * 保证了 ImageView 的重用
     *
     * @param position 位置
     */
    private NineGridItemView getImageView(final int position) {
        if (position < mImageViewList.size()) {
            return mImageViewList.get(position);
        } else {
            if (mAdapter != null) {
                NineGridItemView itemView = new NineGridItemView(getContext());
                mImageViewList.add(itemView);
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView iv = ((NineGridItemView) v).getImageView();
                        mAdapter.onItemImageClick(getContext(), iv, position, mImgDataList);
                        if (mItemImageClickListener != null) {
                            mItemImageClickListener.onItemImageClick(getContext(), iv, position, mImgDataList);
                        }
                    }
                });
                itemView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ImageView iv = ((NineGridItemView) v).getImageView();
                        boolean consumedEvent = mAdapter.onItemImageLongClick(getContext(), iv, position, mImgDataList);
                        if (mItemImageLongClickListener != null) {
                            consumedEvent = mItemImageLongClickListener.onItemImageLongClick(getContext(), iv, position, mImgDataList) || consumedEvent;
                        }
                        return consumedEvent;
                    }
                });
                return itemView;
            } else {
                Log.e("NineGirdImageView", "Your must set a NineGridImageViewAdapter for NineGirdImageView");
                return null;
            }
        }
    }

    private void bindItem(NineGridItemView itemView, int index) {
        if (mAdapter == null || mImgDataList == null) {
            return;
        }
        itemView.applyImageStyle(mCornerRadius, mMaskColor);
        itemView.applyMoreBadgeStyle(mMoreTextColor, mMoreTextBgColor, mMoreTextCornerRadius, mMoreTextSize,
                mMoreMarginEnd, mMoreMarginBottom, mMorePaddingHorizontal, mMorePaddingVertical);
        int total = mImgDataList.size();
        int showCount = getNeedShowCount(total);
        boolean showMore = total > mMaxSize && mMaxSize > 0 && index == showCount - 1;
        int remain = Math.max(0, total - showCount);
        itemView.setMoreCount(remain, showMore, showMore ? mMoreMaskColor : Color.TRANSPARENT);
        mAdapter.onDisplayImage(getContext(), itemView.getImageView(), mImgDataList.get(index));
    }

    /** 图片圆角，单位 px。 */
    public void setCornerRadius(float cornerRadiusPx) {
        mCornerRadius = Math.max(0f, cornerRadiusPx);
        rebindStylesAndImages();
    }

    /** 图片蒙层色，ARGB；alpha=0 表示无蒙层。 */
    public void setMaskColor(@androidx.annotation.ColorInt int maskColor) {
        mMaskColor = maskColor;
        rebindStylesAndImages();
    }

    /** 超过 maxSize 时末格图片蒙层色。 */
    public void setMoreMaskColor(@androidx.annotation.ColorInt int moreMaskColor) {
        mMoreMaskColor = moreMaskColor;
        rebindStylesAndImages();
    }

    /** 「n+」文字颜色。 */
    public void setMoreTextColor(@androidx.annotation.ColorInt int moreTextColor) {
        mMoreTextColor = moreTextColor;
        rebindStylesAndImages();
    }

    /** 「n+」背景颜色。 */
    public void setMoreTextBgColor(@androidx.annotation.ColorInt int moreTextBgColor) {
        mMoreTextBgColor = moreTextBgColor;
        rebindStylesAndImages();
    }

    /** 「n+」背景圆角，单位 px。 */
    public void setMoreTextCornerRadius(float moreTextCornerRadiusPx) {
        mMoreTextCornerRadius = Math.max(0f, moreTextCornerRadiusPx);
        rebindStylesAndImages();
    }

    /** 「n+」字号，单位 px。 */
    public void setMoreTextSize(float moreTextSizePx) {
        mMoreTextSize = Math.max(0f, moreTextSizePx);
        rebindStylesAndImages();
    }

    /** 「n+」右边距，单位 px。 */
    public void setMoreMarginEnd(int moreMarginEndPx) {
        mMoreMarginEnd = Math.max(0, moreMarginEndPx);
        rebindStylesAndImages();
    }

    /** 「n+」下边距，单位 px。 */
    public void setMoreMarginBottom(int moreMarginBottomPx) {
        mMoreMarginBottom = Math.max(0, moreMarginBottomPx);
        rebindStylesAndImages();
    }

    /** 同时设置「n+」右、下边距，单位 px。 */
    public void setMoreMargin(int moreMarginPx) {
        int m = Math.max(0, moreMarginPx);
        mMoreMarginEnd = m;
        mMoreMarginBottom = m;
        rebindStylesAndImages();
    }

    /** 「n+」左右内边距，单位 px。 */
    public void setMorePaddingHorizontal(int paddingHorizontalPx) {
        mMorePaddingHorizontal = Math.max(0, paddingHorizontalPx);
        rebindStylesAndImages();
    }

    /** 「n+」上下内边距，单位 px。 */
    public void setMorePaddingVertical(int paddingVerticalPx) {
        mMorePaddingVertical = Math.max(0, paddingVerticalPx);
        rebindStylesAndImages();
    }

    /** 同时设置「n+」四边内边距，单位 px。 */
    public void setMorePadding(int paddingPx) {
        int p = Math.max(0, paddingPx);
        mMorePaddingHorizontal = p;
        mMorePaddingVertical = p;
        rebindStylesAndImages();
    }

    /**
     * 设置 宫格参数
     *
     * @param imagesSize 图片数量
     * @param showStyle  显示风格
     * @return 宫格参数 gridParam[0] 宫格行数 gridParam[1] 宫格列数
     */
    protected int[] calculateGridParam(int imagesSize, int showStyle) {
        int[] gridParam = new int[2];
        switch (showStyle) {
            case STYLE_FILL:
                generatUnitRowAndColumnForSpanType(imagesSize, gridParam);
                break;
            default:
            case STYLE_GRID:
                if (imagesSize == 1) {
                    gridParam[0] = 1;
                    gridParam[1] = 1;
                } else if (imagesSize == 2) {
                    gridParam[0] = 1;
                    gridParam[1] = 2;
                } else if (imagesSize == 4) {
                    // 社交常见 2x2，避免 3+1
                    gridParam[0] = 2;
                    gridParam[1] = 2;
                } else {
                    gridParam[0] = imagesSize / 3 + (imagesSize % 3 == 0 ? 0 : 1);
                    gridParam[1] = 3;
                }
                break;
        }
        return gridParam;
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(NineGridImageViewAdapter<T> adapter) {
        mAdapter = adapter;
        if (mImgDataList != null && !mImgDataList.isEmpty()) {
            bindAllItems();
        }
    }

    /**
     * 设置宫格间距
     *
     * @param gap 宫格间距 px
     */
    public void setGap(int gap) {
        mGap = gap;
    }

    /**
     * 设置显示风格
     *
     * @param showStyle 显示风格
     */
    public void setShowStyle(int showStyle) {
        mShowStyle = showStyle;
    }

    /**
     * 单张图固定边长（px）。&lt;=0 时按微信规则：边长 = 2*cell+gap（约 2/3 宽）。
     */
    public void setSingleImgSize(int singleImgSize) {
        mSingleImgSize = singleImgSize;
        requestLayout();
    }

    /**
     * 设置最大图片数
     *
     * @param maxSize 最大图片数
     */
    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
    }

    public void setItemImageClickListener(ItemImageClickListener<T> itemImageViewClickListener) {
        mItemImageClickListener = itemImageViewClickListener;
    }

    public void setItemImageLongClickListener(ItemImageLongClickListener<T> itemImageViewLongClickListener) {
        mItemImageLongClickListener = itemImageViewLongClickListener;
    }
}