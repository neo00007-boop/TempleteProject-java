package com.templete.project.ui.activity;

import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.adapter.DoorEventAdapter;
import com.templete.project.bean.DoorBean;
import com.templete.project.databinding.PinnedActivityBinding;

import java.util.ArrayList;

/**
 * PackageName  com.templete.project.ui.fragment
 * ProjectName  TempleteProject-java
 * Date         2022/10/22.
 *
 * @author xwchen
 */

public class PinnedActivity extends BaseActivity<PinnedActivityBinding> {
    @Override
    public void inits() {

    }

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_BLUE;
    }

    @Override
    public void initView() {
        ArrayList<DoorBean> list = new ArrayList<>();
        DoorBean doorBean1 = new DoorBean();
        doorBean1.itemType = 0;
        list.add(doorBean1);
        for (int i = 0; i < 25; i++) {
            DoorBean doorBean = new DoorBean();
            doorBean.itemType = 1;
            list.add(doorBean);
        }
        DoorBean doorBean2 = new DoorBean();
        doorBean2.itemType = 0;
        list.add(doorBean2);
        for (int i = 0; i < 25; i++) {
            DoorBean doorBean = new DoorBean();
            doorBean.itemType = 1;
            list.add(doorBean);
        }

        DoorEventAdapter adapter = new DoorEventAdapter(list);
        mViewBinding.listView.setAdapter(adapter);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {

    }

    @Override
    protected PinnedActivityBinding viewBinding() {
        return PinnedActivityBinding.inflate(getLayoutInflater());
    }
}
