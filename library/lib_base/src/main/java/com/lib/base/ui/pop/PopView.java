package com.lib.base.ui.pop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.lib.base.R;
import com.lib.base.adapter.PopAdapter;
import com.lib.base.bean.BtnBean;
import com.lib.base.databinding.PopLayoutBinding;
import com.lib.base.ui.widget.TitleBar;
import com.lib.base.util.Arrays;
import com.lib.base.util.ContextUtil;
import com.lib.base.util.DebugUtil;
import com.lib.base.util.ScreenUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 自动判断展示位置PopupWindow:以列表形式展示.可以添加左侧icon,右侧选择状态等
 * PackageName  com.spot.ispot.view.widget
 * ProjectName  Spot1-26-yoga
 * Date         6/27/21.
 *
 * @author xwchen
 */
public class PopView extends PopupWindow implements DefaultLifecycleObserver {
    public static final String TAG = "PopView";
    private final Context context;
    private final PopLayoutBinding layoutBinding;
    private final TitleBar.OnRightViewsClickListener clickListener;
    private PopAdapter popAdapter;

    public PopView(Context context, TitleBar.OnRightViewsClickListener clickListener) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        this.clickListener = clickListener;
        layoutBinding = PopLayoutBinding.inflate(LayoutInflater.from(context));
        setContentView(layoutBinding.getRoot());
        setFocusable(true);
        setOutsideTouchable(false);
        setClippingEnabled(false);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //感知activity生命周期,自动dismiss
        addObserver(context);
    }

    private void addObserver(Context context) {
        LifecycleOwner lifecycleOwner = ContextUtil.getLifecycleOwnerByContext(context);
        if (lifecycleOwner != null) {
            lifecycleOwner.getLifecycle().addObserver(this);
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DebugUtil.logD(TAG, "pop onDestroy");
        if (isShowing()) {
            dismiss();
        }
    }

    /**
     * pop加载数据
     *
     * @param popBeans
     * @param hasSelect
     * @param hasHtml
     */
    public void setPopBeans(BtnBean[] popBeans, boolean hasSelect, boolean hasHtml) {
        setPopBeans(Arrays.asList(popBeans), hasSelect, hasHtml);
    }

    /**
     * pop加载数据
     *
     * @param popBeans
     * @param hasSelect
     * @param hasHtml
     */
    public void setPopBeans(List<BtnBean> popBeans, boolean hasSelect, boolean hasHtml) {
        if (popBeans == null || popBeans.size() == 0) {
            return;
        }
        layoutBinding.recycleView.setLayoutManager(new LinearLayoutManager(context));
        popAdapter = new PopAdapter(context, hasSelect, hasHtml);
        popAdapter.setList(popBeans);
        popAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (clickListener != null) {
                clickListener.clickPosition(position, view);
            }
            popAdapter.notifyData(position);
            dismiss();
        });
        layoutBinding.recycleView.setAdapter(popAdapter);
    }

    /**
     * 自动判断展示位置PopupWindow,角标自动对准view中间位置
     *
     * @param locationView
     */
    public void show(View locationView) {
        int[] location = new int[2];
        locationView.getLocationOnScreen(location);
        int viewWidthHalf = locationView.getWidth() / 2;
        int viewMiddlePositionX = location[0] + viewWidthHalf;
//        int viewMiddlePositionY = location[1] + locationView.getHeight() / 2;
        int middleScreenX = ScreenUtil.getScreenWid() / 2;//宽度以一半为界限
//        int middleScreenY = (int) (ScreenUtil.getScreenHei() * 0.6);//宽度以60%为界限
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int emptyY = ScreenUtil.getScreenHei() - location[1] - locationView.getHeight() - 5;//下面有足够空间弹出pop
        boolean isTop = measuredHeight < emptyY;
        float dimensionX10 = context.getResources().getDimension(R.dimen.x5);//尖叫距离view距离
        float dimensionX25 = context.getResources().getDimension(R.dimen.x25);//尖叫距离最近边距离
        int sanjiaoWidthTop = layoutBinding.ivTop.getMeasuredWidth();
        int sanjiaoWidthBottom = layoutBinding.ivBottom.getMeasuredWidth();
        int sanjiaoWidthHalfTop = sanjiaoWidthTop / 2;
        int sanjiaoWidthHalfBottom = sanjiaoWidthBottom / 2;
        //左上区域
        if (viewMiddlePositionX <= middleScreenX && /*viewMiddlePositionY <= middleScreenY*/isTop) {
            setAnimationStyle(R.style.pop_anim1);
            layoutBinding.ivBottom.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) layoutBinding.ivTop.getLayoutParams()).leftMargin = (int) dimensionX25;
            layoutBinding.ivTop.requestLayout();
            //这种方式还有bug
            //showAsDropDown(locationView, viewWidthHalf - (int) dimensionX25 - sanjiaoWidthHalfTop, (int) dimensionX10);
            showAtLocation(locationView, Gravity.NO_GRAVITY,
                    (int) (location[0] + viewWidthHalf - sanjiaoWidthHalfTop - dimensionX25),
                    (int) (location[1] + locationView.getHeight() + dimensionX10));
        } else if (viewMiddlePositionX > middleScreenX && /*viewMiddlePositionY <= middleScreenY*/isTop) {
            //右上区域
            setAnimationStyle(R.style.pop_anim2);
            layoutBinding.ivBottom.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) layoutBinding.ivTop.getLayoutParams()).leftMargin = measuredWidth - (int) dimensionX25 - sanjiaoWidthTop;
            layoutBinding.ivTop.requestLayout();
            //这种方式还有bug
            //showAsDropDown(locationView, -measuredWidth + viewWidthHalf + (int) dimensionX25 + sanjiaoWidthHalfTop, (int) dimensionX10);
            showAtLocation(locationView, Gravity.NO_GRAVITY,
                    //(int) (location[0]+viewWidthHalf-sanjiaoWidthHalfTop-(measuredWidth-sanjiaoWidthTop-dimensionX25)),
                    //简化写法
                    (int) (location[0] + viewWidthHalf + sanjiaoWidthHalfTop - measuredWidth + dimensionX25),
                    (int) (location[1] + locationView.getHeight() + dimensionX10));
        } else if (viewMiddlePositionX <= middleScreenX) {
            //左下区域
            setAnimationStyle(R.style.pop_anim3);
            layoutBinding.ivTop.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) layoutBinding.ivBottom.getLayoutParams()).leftMargin = (int) dimensionX25;
            layoutBinding.ivBottom.requestLayout();
            showAtLocation(locationView, Gravity.NO_GRAVITY, location[0] + viewWidthHalf - (int) dimensionX25 - sanjiaoWidthHalfBottom, location[1] - measuredHeight - (int) dimensionX10);
        } else {
            //右下区域
            setAnimationStyle(R.style.pop_anim4);
            layoutBinding.ivTop.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) layoutBinding.ivBottom.getLayoutParams()).leftMargin = measuredWidth - (int) dimensionX25 - sanjiaoWidthBottom;
            layoutBinding.ivBottom.requestLayout();
            showAtLocation(locationView, Gravity.NO_GRAVITY, location[0] + viewWidthHalf + (int) dimensionX25 + sanjiaoWidthHalfBottom - measuredWidth, location[1] - measuredHeight - (int) dimensionX10);
        }
    }

    /**
     * 测量高度
     *
     * @return
     */
    public int getMeasuredHeight() {
        getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return getContentView().getMeasuredHeight();
    }

    /**
     * 测量宽度
     *
     * @return
     */
    public int getMeasuredWidth() {
        getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return getContentView().getMeasuredWidth();
    }

    public interface OnItemClickListener {
        void onitemClick();
    }

}