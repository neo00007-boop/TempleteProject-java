package com.templete.project.ui.activity;

import android.view.View;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseFreshListActivity;
import com.lib.base.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * {@link BaseFreshListActivity} 用法示例
 */
public class FreshListDemoActivity extends BaseFreshListActivity<String, DemoAdapter> {

    @Override
    public void inits() {
        setTitleStr("刷新列表基类 Demo");
    }

    @NonNull
    @Override
    protected DemoAdapter createAdapter() {
        return new DemoAdapter(this);
    }

    @Override
    protected boolean autoRefreshOnEnter() {
        return true;
    }

    @Override
    protected void onListRequest(boolean isFresh) {
        // TODO: 替换为真实接口，结束后 onRequestSuccess / onRequestFailure
        mViewBinding.getRoot().postDelayed(() -> {
            if (!isFresh && mPage > 3) {
                onRequestSuccess(new ArrayList<>(), true);
                return;
            }
            onRequestSuccess(mockData(mPage));
        }, 800);
    }

    @Override
    protected void onListItemClick(@NonNull DemoAdapter adapter, @NonNull View view, int position) {
        DebugUtil.toast("click: " + getItem(position));
    }

    private List<String> mockData(int page) {
        List<String> list = new ArrayList<>();
        int start = (page - 1) * pageSize();
        for (int i = 0; i < pageSize(); i++) {
            list.add("条目 " + (start + i + 1));
        }
        return list;
    }
}
