package com.templete.project.ui.activity;

import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.ActivityFoldTextBinding;

/**
 * 折叠 TextView Demo：仿哔站标题（左文右箭头）。
 */
public class FoldTextActivity extends BaseActivity<ActivityFoldTextBinding> {

    @Override
    protected ActivityFoldTextBinding viewBinding() {
        return ActivityFoldTextBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("折叠 TextView");
    }

    @Override
    public void initView() {
        mViewBinding.foldLong.setOnFoldChangeListener(expanded -> refreshState());
        mViewBinding.foldCustom.setOnFoldChangeListener(expanded -> refreshState());
        mViewBinding.foldShort.post(this::refreshState);
    }

    private void refreshState() {
        mViewBinding.tvFoldState.setText(
                "长标题：" + (mViewBinding.foldLong.isExpanded() ? "已展开" : "已收起")
                        + "　｜　自定义：" + (mViewBinding.foldCustom.isExpanded() ? "已展开" : "已收起")
                        + "　｜　短标题 needFold=" + mViewBinding.foldShort.isNeedFold());
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }
}
