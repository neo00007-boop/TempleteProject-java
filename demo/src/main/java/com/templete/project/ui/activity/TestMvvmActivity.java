package com.templete.project.ui.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lib.base.ui.activity.BaseMvvmActivity;
import com.templete.project.R;
import com.templete.project.databinding.DemoMvvmActivityBinding;
import com.templete.project.mvvm.DemoViewModel;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 * @author      xwchen
 * Date         2021/12/28.
 */

public class TestMvvmActivity extends BaseMvvmActivity<DemoMvvmActivityBinding, DemoViewModel> {
    public static final String TAG = "DemoMvvmActivity";
    private static final int ITEM_ANIM_MS = 180;

    @Override
    protected DemoMvvmActivityBinding viewBinding() {
        return DemoMvvmActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<DemoViewModel> getViewModelClass() {
        return DemoViewModel.class;
    }

    @Override
    public void inits() {
        setTitleStr("mvvm activity");
    }

    @Override
    public void initView() {
        mViewBinding.ll.setLayoutTransition(null);
        mViewBinding.tvAdd.setOnClickListener(v -> addItem());
        mViewBinding.tvDelete.setOnClickListener(v -> removeLast());
    }

    private void addItem() {
        View viewChild = LayoutInflater.from(this).inflate(R.layout.add_item, mViewBinding.ll, false);
        TextView tv = viewChild.findViewById(R.id.tv);
        tv.setText(String.valueOf(nextIndex()));
        viewChild.setAlpha(0f);
        viewChild.setTranslationY(tv.getTextSize());
        mViewBinding.ll.addView(viewChild, insertIndex());
        viewChild.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ITEM_ANIM_MS)
                .start();
    }

    private void removeLast() {
        View child = lastStableChild();
        if (child == null) {
            return;
        }
        child.setTag(Boolean.TRUE);
        child.animate().cancel();
        Runnable remove = () -> {
            if (child.getParent() == mViewBinding.ll) {
                mViewBinding.ll.removeView(child);
            }
        };
        child.animate()
                .alpha(0f)
                .translationY(child.getHeight() > 0 ? child.getHeight() / 3f : 40f)
                .setDuration(ITEM_ANIM_MS)
                .withEndAction(remove)
                .start();
        mViewBinding.ll.postDelayed(remove, ITEM_ANIM_MS + 40);
    }

    private int nextIndex() {
        int stable = 0;
        for (int i = 0; i < mViewBinding.ll.getChildCount(); i++) {
            if (!Boolean.TRUE.equals(mViewBinding.ll.getChildAt(i).getTag())) {
                stable++;
            }
        }
        return stable + 1;
    }

    private int insertIndex() {
        int count = mViewBinding.ll.getChildCount();
        for (int i = 0; i < count; i++) {
            if (Boolean.TRUE.equals(mViewBinding.ll.getChildAt(i).getTag())) {
                return i;
            }
        }
        return count;
    }

    private View lastStableChild() {
        for (int i = mViewBinding.ll.getChildCount() - 1; i >= 0; i--) {
            View child = mViewBinding.ll.getChildAt(i);
            if (!Boolean.TRUE.equals(child.getTag())) {
                return child;
            }
        }
        return null;
    }

    @Override
    public void initEvent() {
        /*getGlobalUserStateViewModel().data1.observe(this, integer -> DebugUtil.logD(TAG, "data1=" + integer));
        getGlobalUserStateViewModel().data2.observe(this, integer -> DebugUtil.logD(TAG, "data2=" + integer));*/

    }

    @Override
    public void initData() {
//        mViewModel.test(BaseData.TAG_1, "test");
        /*if (!getGlobalUserStateViewModel().init) {
            getGlobalUserStateViewModel().init = true;
            getGlobalUserStateViewModel().data1.setValue(1);
            getGlobalUserStateViewModel().data2.setValue(2);
        }*/
         /*mViewModel.getBaseData().observe(this, baseData -> {
            switch (baseData.type) {
                case BaseData.TAG_1:
                    DebugUtil.logD(TAG, "baseData=" + GsonUtil.getInstance().toJson(baseData));
                    break;
                default:
                    break;
            }
        });*/
    }

    /*动态显示隐藏标题栏/底部栏*/
    /*private void toggleMenu(boolean hideStatusBar) {
        initMenuAnim();
        if (mAblTopMenu.getVisibility() == View.VISIBLE) {
            //关闭
            mAblTopMenu.startAnimation(mTopOutAnim);
            mLlBottomMenu.startAnimation(mBottomOutAnim);
            mAblTopMenu.setVisibility(GONE);
            mLlBottomMenu.setVisibility(GONE);
            mTvPageTip.setVisibility(GONE);

            if (hideStatusBar) {
                hideSystemBar();
            }
        } else {
            mAblTopMenu.setVisibility(View.VISIBLE);
            mLlBottomMenu.setVisibility(View.VISIBLE);
            mAblTopMenu.startAnimation(mTopInAnim);
            mLlBottomMenu.startAnimation(mBottomInAnim);

            showSystemBar();
        }
    }

    private void initMenuAnim() {
        if (mTopInAnim != null) return;

        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
        //退出的速度要快
        mTopOutAnim.setDuration(200);
        mBottomOutAnim.setDuration(200);
    }*/
}
