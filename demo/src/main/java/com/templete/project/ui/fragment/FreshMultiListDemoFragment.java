package com.templete.project.ui.fragment;

import android.view.View;

import com.lib.base.ui.fragment.BaseFreshListFragment;
import com.lib.base.util.DebugUtil;
import com.templete.project.adapter.MultiItemDemoAdapter;
import com.templete.project.adapter.MultiItemDemoAdapter.Item;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * {@link BaseFreshListFragment} + MultiItem 用法示例（与 FreshMultiListDemoActivity 对应）
 */
public class FreshMultiListDemoFragment extends BaseFreshListFragment<Item, MultiItemDemoAdapter> {

    @Override
    public void inits() {
    }

    @NonNull
    @Override
    protected MultiItemDemoAdapter createAdapter() {
        return new MultiItemDemoAdapter();
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
    protected void onListItemClick(@NonNull MultiItemDemoAdapter adapter, @NonNull View view, int position) {
        Item item = getItem(position);
        if (item == null) {
            return;
        }
        DebugUtil.toast("type=" + item.getItemType() + "  " + item.text);
    }

    private List<Item> mockData(int page) {
        List<Item> list = new ArrayList<>();
        int start = (page - 1) * pageSize();
        list.add(Item.title("Fragment 第 " + page + " 页分组"));
        for (int i = 0; i < pageSize() - 1; i++) {
            list.add(Item.content("Fragment 多类型条目 " + (start + i + 1)));
        }
        return list;
    }
}
