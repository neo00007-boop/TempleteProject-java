package com.templete.project.ui.activity;

import android.view.View;

import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.pop.PopMenuView;
import com.lib.base.ui.pop.PopView;
import com.templete.project.R;
import com.templete.project.databinding.MenuLayoutBinding;
import com.templete.project.databinding.PopActivityBinding;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 * @author      xwchen
 * Date         2021/12/27.
 */

public class PopActivity extends BaseActivity<PopActivityBinding> {

    private PopMenuView popMenuView;
    private PopView listPopView;

    @Override
    public void inits() {
        setTitleStr("智能popView");
        setRightClickViews((position, view) -> togoMenu(), false, new BtnBean("菜单"));
    }

    private void togoMenu() {
        if (popMenuView == null) {
            MenuLayoutBinding binding = MenuLayoutBinding.inflate(getLayoutInflater());
            binding.holder.setOnClickListener(v -> popMenuView.dismiss());
            popMenuView = new PopMenuView(binding.getRoot(), getTitleBar());
            popMenuView.show();
        } else {
            if (popMenuView.isShowing()) {
                popMenuView.dismiss();
            } else {
                popMenuView.show();
            }
        }
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {
        setOnClickListener(this, R.id.tv_zuoshang, R.id.tv_youshang, R.id.tv_zuoxia, R.id.tv_youxia, R.id.tv_shape, R.id.tv_list);
    }

    @Override
    public void initData() {

    }

    @Override
    protected PopActivityBinding viewBinding() {
        return PopActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onClick(View v) {
        View view = null;
        if (v.getId() == R.id.tv_zuoshang) {
            view = mViewBinding.tvZuoshang;
        } else if (v.getId() == R.id.tv_youshang) {
            view = mViewBinding.tvYoushang;
        } else if (v.getId() == R.id.tv_zuoxia) {
            view = mViewBinding.tvZuoxia;
        } else if (v.getId() == R.id.tv_youxia) {
            view = mViewBinding.tvYouxia;
        } else if (v.getId() == R.id.tv_shape) {
            view = mViewBinding.tvShape;
        } else if (v.getId() == R.id.tv_list) {
            showListMenu(mViewBinding.tvList);
            return;
        }
        if (view != null) {
            new PopView(this, null).show(view);
        }
    }

    private void showListMenu(View anchor) {
        if (listPopView == null) {
            listPopView = new PopView(this, null);
            listPopView.setPopBeans(new BtnBean[]{
                    new BtnBean("短"),
                    new BtnBean("中等长度"),
                    new BtnBean("这一条最长,用来把菜单宽度撑开"),
                    new BtnBean("列表四"),
                    new BtnBean("列表五"),
                    new BtnBean("列表六"),
                    new BtnBean("列表七"),
                    new BtnBean("列表八"),
                    new BtnBean("列表九"),
                    new BtnBean("列表十"),
                    new BtnBean("列表十一"),
                    new BtnBean("列表十二")
            }, false, false);
        }
        if (listPopView.isShowing()) {
            listPopView.dismiss();
        } else {
            listPopView.show(anchor);
        }
    }

}
