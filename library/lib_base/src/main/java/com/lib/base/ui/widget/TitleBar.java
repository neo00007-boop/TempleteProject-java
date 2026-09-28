package com.lib.base.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hjq.shape.view.ratioLayout.StatuesBar;
import com.lib.base.R;
import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.pop.PopView;
import com.lib.base.util.DebugUtil;
import com.lib.base.util.OUtil;

import androidx.annotation.NonNull;

import static com.lib.base.ui.activity.TitleBarTheme.THEME_NONE;


/**
 * 标题TitleBar,可以设置主题和对应主题下返回按钮,中间标题,右边按钮(支持pop列表)等样式属性:
 * 1.默认主题有蓝色(背景)和白色(背景)两个颜色,可以配置多个,对应{@link TitleBarTheme}中配置;
 * 2.{@link BaseActivity}中默认实现沉浸式效果,故默认不展示内部占位的StatuesBar{@link com.hjq.shape.view.ratioLayout.StatuesBar};
 * 3.需要展示StatuesBar的时候调用{@link TitleBar#showStatuesBar()}即可;
 * 4.给整个TitleBar设置背景图或者背景色调用{@link TitleBar#setTitleBGResource(int)}即可;
 * 5.当没有主动初始化init{@link TitleBar#init(int)}或者initView{@link TitleBar#initView(android.content.Context, int)}时,
 * 调用{@link TitleBar#setTitleName(java.lang.String)}等方法时会先默认初始化一个蓝色主题(也可以抛出一个异常提示未初始化).
 * <p>
 * PackageName  com.hgd.hgdnovel.wedjet
 * ProjectName  Project
 * @author      xwchen
 * Date         2018/3/20.
 */

public class TitleBar extends FrameLayout {
    private static final int MAX_RIGHT_BTN_COUNT = 2;
    private static final int TITLE_MARQUEE_COUNT = 10;
    private int theme = THEME_NONE;
    private LayoutInflater layoutInflater;
    private StatuesBar statuesBar;
    private TextView tvBackNoArrow;
    private TextView tvBackArrow;
    private TextView tvTitle;
    private LinearLayout llRight;
    private ImageView ivRight;
    private OnRightViewsClickListener rightClickListener;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        //主题
        int barInitModel = a.getInt(R.styleable.TitleBar_barInitModel, -1);
        boolean result;
        if (barInitModel == -1 && isInEditMode()) {
            //开启预览模式
            result = initView(context, TitleBarTheme.THEME_BLUE);
        } else if (barInitModel == 0) {
            //blue title theme
            result = initView(context, TitleBarTheme.THEME_BLUE);
        } else if (barInitModel == 1) {
            //white title theme
            result = initView(context, TitleBarTheme.THEME_WHITE);
        } else {
            return;
        }
        DebugUtil.logD("result", "result=" + result);
        //statuesBar
        boolean barStatuesVisible = a.getBoolean(R.styleable.TitleBar_barStatuesVisible, false);
        //statuesBar.setVisibility(barStatuesVisible ? VISIBLE : GONE);
        if (barStatuesVisible) {
            statuesBar.setVisibility(VISIBLE);
        }
        //返回按钮
        String barBackStr = a.getString(R.styleable.TitleBar_barBackStr);
        boolean barBackVisible = a.getBoolean(R.styleable.TitleBar_barBackVisible, true);
        boolean barBackArrowVisible = a.getBoolean(R.styleable.TitleBar_barBackArrowVisible, false);
        if (barBackVisible) {
            setLeftStr(barBackStr);
        } else if (barBackArrowVisible) {
            setLeftWithNoArrow(barBackStr);
        } else {
            tvBackArrow.setVisibility(GONE);
            tvBackNoArrow.setVisibility(GONE);
        }
        //title
        String barTitleStr = a.getString(R.styleable.TitleBar_barTitleStr);
        setTitleName(barTitleStr);
        //right
        int barRightIconResource = a.getResourceId(R.styleable.TitleBar_barRightIconResource, -1);
        if (barRightIconResource != -1) {
            ivRight.setVisibility(VISIBLE);
            ivRight.setImageResource(barRightIconResource);
            ivRight.setOnClickListener(v -> {
                if (rightClickListener != null) {
                    rightClickListener.clickPosition(0, v);
                }
            });
        }
        a.recycle();
    }

    /**
     * 防止异常出现,只允许初始化一次
     *
     * @param context
     * @param theme
     */
    public boolean initView(Context context, int theme) {
        if (!isInEditMode() && this.theme != THEME_NONE) {
            return false;
        }
        this.theme = theme;
        layoutInflater = LayoutInflater.from(context);
        int layoutId = TitleBarTheme.THEME_BLUE == theme ? R.layout.blue_title_layout : R.layout.white_title_layout;
        View rootView = layoutInflater.inflate(layoutId, this, true);
        statuesBar = rootView.findViewById(R.id.statues_bar);
        tvBackNoArrow = rootView.findViewById(R.id.tv_back_no_arrow);
        tvBackArrow = rootView.findViewById(R.id.tv_back_arrow);
        tvTitle = rootView.findViewById(R.id.tv_title);
        llRight = rootView.findViewById(R.id.ll_right);
        ivRight = rootView.findViewById(R.id.iv_right);
        return true;
    }

    public void setBackClickListener(OnClickListener onClickListener) {
        hasInit();
        tvBackNoArrow.setOnClickListener(onClickListener);
        tvBackArrow.setOnClickListener(onClickListener);
    }

    public void hideBackView() {
        hasInit();
        tvBackNoArrow.setVisibility(INVISIBLE);
        tvBackArrow.setVisibility(INVISIBLE);
    }

    public void setLeftStr(String strBack) {
        hasInit();
        tvBackNoArrow.setVisibility(View.GONE);
        tvBackArrow.setText(strBack);
        tvBackArrow.setVisibility(View.VISIBLE);
    }

    public void setLeftWithNoArrow(String strBack) {
        hasInit();
        tvBackArrow.setVisibility(View.GONE);
        tvBackNoArrow.setText(strBack);
        tvBackNoArrow.setVisibility(View.VISIBLE);
    }

    public void setTitleName(String titleName) {
        hasInit();
        setTitleName(titleName, false);
    }

    public void setTitleName(String titleName, boolean bold) {
        hasInit();
        tvTitle.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tvTitle.setSelected(/*titleName.length() > TITLE_MARQUEE_COUNT*/true);
        tvTitle.setText(titleName);
    }

    public void clearRightBtns() {
        hasInit();
        llRight.removeAllViews();
        ivRight.setVisibility(INVISIBLE);
        if (popView != null) {
            popView = null;
        }
    }

    @SuppressLint("InflateParams")
    public void setRightClickViews(OnRightViewsClickListener listener, boolean hasSelect, @NonNull BtnBean... titleBtnBeans) {
        hasInit();
        if (llRight.getChildCount() > 0) {
            return;
        }
        if (titleBtnBeans.length > MAX_RIGHT_BTN_COUNT) {
            for (BtnBean rightClickView : titleBtnBeans) {
                if (OUtil.isNull(rightClickView.str)) {
                    DebugUtil.toast("超过2个按钮必须是string类型");
                    return;
                }
            }
            if (VISIBLE != ivRight.getVisibility()) {
                ivRight.setVisibility(VISIBLE);
            }
            ivRight.setOnClickListener(v -> {
                if (rightClickListener != null) {
                    rightClickListener.clickPosition(0, v);
                }
                showPop(titleBtnBeans, hasSelect, listener);
            });
        } else {
            addRightClick(listener, titleBtnBeans);
        }
    }

    private PopView popView;

    private void showPop(BtnBean[] titleBtnBeans, boolean hasSelect, OnRightViewsClickListener listener) {
        if (popView == null) {
            popView = new PopView(getContext(), listener);
            popView.setPopBeans(titleBtnBeans, hasSelect, false);
        }
        View root = getParent() instanceof View ? (View) getParent() : this;
        popView.show(ivRight, root);
    }

    /**
     * 不要使用title_right_click_iv_view的ViewBinding引用方式,新版本as编辑模式会报错
     *
     * @param listener
     * @param rightClickViews
     */
    @SuppressLint("InflateParams")
    private void addRightClick(OnRightViewsClickListener listener, @NonNull BtnBean[] rightClickViews) {
        for (int i = 0; i < rightClickViews.length; i++) {
            BtnBean btnBean = rightClickViews[i];
            View view = null;
            if (btnBean.resId > 0) {
                view = layoutInflater.inflate(R.layout.title_right_click_iv_view, null);
                ImageView iv = view.findViewById(R.id.iv_right);
                iv.setImageResource(btnBean.resId);
            } else if (OUtil.isNotNull(btnBean.str)) {
                view = layoutInflater.inflate(R.layout.title_right_click_tv_view, null);
                TextView tv_right = view.findViewById(R.id.tv_right);
                tv_right.setText(btnBean.str);
                if (TitleBarTheme.THEME_WHITE == theme) {
                    tv_right.setTextColor(getResources().getColor(R.color.cl_333333));
                }
            } else if (btnBean.view != null) {
                view = btnBean.view;
            }
            if (view != null) {
                final int finalI = i;
                final View finalView = view;
                view.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.clickPosition(finalI, finalView);
                    }
                });
                llRight.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
    }

    public void setBackStr(String str) {
        tvBackArrow.setText(str);
    }

    public void setTitleBGResource(int backGroundResource) {
        hasInit();
        showStatuesBar();
        ((ViewGroup) tvBackArrow.getParent()).setBackgroundResource(R.color.cl_no_color);
        setBackgroundResource(backGroundResource);
    }

    public void showStatuesBar() {
        statuesBar.setVisibility(VISIBLE);
    }

    private void hasInit() {
        if (theme == THEME_NONE) {
            throw new RuntimeException("has method initView() called?");
        }
    }

    public void init(int appTheme) {
        initView(getContext(), appTheme);
    }


    public StatuesBar getStatuesBar() {
        return statuesBar;
    }

    public TextView getTvBackNoArrow() {
        return tvBackNoArrow;
    }

    public TextView getTvBackArrow() {
        return tvBackArrow;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public LinearLayout getLlRight() {
        return llRight;
    }

    public ImageView getIvRight() {
        return ivRight;
    }

    /**
     * 右侧菜单的点击事件
     */
    public interface OnRightViewsClickListener {
        /**
         * 右侧按钮点击回调
         *
         * @param position pos
         * @param view     view
         */
        void clickPosition(int position, View view);
    }

    public void setRightClickListener(OnRightViewsClickListener rightClickListener) {
        this.rightClickListener = rightClickListener;
    }
}
