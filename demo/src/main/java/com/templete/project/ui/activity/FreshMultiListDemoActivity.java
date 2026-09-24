package com.templete.project.ui.activity;

import android.view.View;

import com.lib.base.ui.activity.BaseFreshListActivity;
import com.lib.base.util.DebugUtil;
import com.templete.project.adapter.MultiItemDemoAdapter;
import com.templete.project.adapter.MultiItemDemoAdapter.Item;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * {@link BaseFreshListActivity} + {@link com.chad.library.adapter.base.BaseMultiItemQuickAdapter} 用法示例
 */
public class FreshMultiListDemoActivity extends BaseFreshListActivity<Item, MultiItemDemoAdapter> {

    @Override
    public void inits() {
        setTitleStr("多类型刷新列表 Demo");
    }

    @NonNull
    @Override
    protected MultiItemDemoAdapter createAdapter() {
        return new MultiItemDemoAdapter();
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
        list.add(Item.title("第 " + page + " 页分组"));
        for (int i = 0; i < pageSize() - 1; i++) {
            list.add(Item.content("多类型条目 " + (start + i + 1)));
        }
        return list;
    }
}
