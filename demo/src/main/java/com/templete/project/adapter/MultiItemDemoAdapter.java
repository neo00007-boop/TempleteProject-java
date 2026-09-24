package com.templete.project.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.R;

import androidx.annotation.NonNull;

/**
 * {@link BaseMultiItemQuickAdapter} + 刷新列表基类 Demo 用 Adapter
 */
public class MultiItemDemoAdapter extends BaseMultiItemQuickAdapter<MultiItemDemoAdapter.Item, BaseViewHolder> {

    public static final int TYPE_TITLE = 0;
    public static final int TYPE_CONTENT = 1;

    public MultiItemDemoAdapter() {
        super();
        addItemType(TYPE_TITLE, R.layout.picker_item);
        addItemType(TYPE_CONTENT, R.layout.demo_layout);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, Item item) {
        if (item == null) {
            return;
        }
        switch (holder.getItemViewType()) {
            case TYPE_TITLE:
                holder.setText(R.id.tv_picker_name, item.text);
                break;
            case TYPE_CONTENT:
            default:
                holder.setText(R.id.tv, item.text);
                break;
        }
    }

    public static class Item implements MultiItemEntity {
        private final int itemType;
        public final String text;

        public Item(int itemType, String text) {
            this.itemType = itemType;
            this.text = text;
        }

        public static Item title(String text) {
            return new Item(TYPE_TITLE, text);
        }

        public static Item content(String text) {
            return new Item(TYPE_CONTENT, text);
        }

        @Override
        public int getItemType() {
            return itemType;
        }
    }
}
