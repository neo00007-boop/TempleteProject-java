package com.templete.project.ui.activity;

import android.view.View;

import androidx.annotation.NonNull;

import com.lib.base.R;
import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseFreshListActivity;
import com.lib.base.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link BaseFreshListActivity} 用法示例（含 childClick）
 */
public class FreshListDemoActivity extends BaseFreshListActivity<String, DemoAdapter> {

    @Override
    public void inits() {
        setTitleStr("刷新列表基类 Demo");
    }

    @NonNull
    @Override
    protected DemoAdapter createAdapter() {
        DemoAdapter adapter = new DemoAdapter(this);
        adapter.setShow(true);
        return adapter;
    }

    @Override
    protected void onListRequest(boolean isFresh) {
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
        DebugUtil.toast("item: " + getItem(position));
    }

    @Override
    protected void onListItemChildClick(@NonNull DemoAdapter adapter, @NonNull View view, int position) {
        if (view.getId() == R.id.tv1) {
            DebugUtil.toast("child tv1: " + getItem(position));
        }
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
