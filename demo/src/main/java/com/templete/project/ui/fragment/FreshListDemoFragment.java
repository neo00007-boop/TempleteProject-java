package com.templete.project.ui.fragment;

import android.view.View;

import com.lib.base.R;
import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.fragment.BaseFreshListFragment;
import com.lib.base.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * {@link BaseFreshListFragment} 用法示例（与 FreshListDemoActivity 对应）
 */
public class FreshListDemoFragment extends BaseFreshListFragment<String, DemoAdapter> {

    @Override
    public void inits() {
    }

    @NonNull
    @Override
    protected DemoAdapter createAdapter() {
        DemoAdapter adapter = new DemoAdapter(requireContext());
        adapter.setShow(true);
        return adapter;
    }

    @Override
    protected void onListRequest(boolean isFresh) {
        mViewBinding.getRoot().postDelayed(() -> {
            if (!isFresh && getPage() > 3) {
                onRequestSuccess(new ArrayList<>(), true);
                return;
            }
            onRequestSuccess(mockData(getPage()));
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
            list.add("Fragment 条目 " + (start + i + 1));
        }
        return list;
    }
}
