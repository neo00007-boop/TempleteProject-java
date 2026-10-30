package com.lib.base.ui.widget.ninegrid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lib.base.R;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * 发帖选图九宫格（与 {@link NineGridImageView} 同包）：展示已选 + 末格添加（虚线边框可配）。
 * 选图/预览由外部通过 {@link OnPicActionListener} 接入 {@code MediaPickerUtil}。
 */
public class NinePicChooseView extends FrameLayout {

    public interface OnPicActionListener {
        /** 点击添加格；剩余可选数量 {@code remain} */
        void onAdd(int remain);

        /** 点击已选图预览 */
        void onPreview(int position, @NonNull ArrayList<LocalMedia> data);

        /** 删除/变更后回调 */
        void onChanged(@NonNull List<LocalMedia> data);
    }

    private static final int TYPE_PIC = 0;
    private static final int TYPE_ADD = 1;
    private static final int UNSET_COLOR = Color.TRANSPARENT;
    private static final int UNSET_DIMEN = Integer.MIN_VALUE;

    private final RecyclerView recyclerView;
    private final PicAdapter adapter;

    private int maxCount = 9;
    private int spanCount = 3;
    private int itemGapPx;
    private float cornerRadiusPx;

    @DrawableRes
    private int addIconRes = R.drawable.ic_nine_pic_add;
    private int addIconSizePx = UNSET_DIMEN;
    private int addIconTint = UNSET_COLOR;
    private int addIconPaddingPx = UNSET_DIMEN;
    private int addBgColor = Color.parseColor("#F5F5F5");
    private float addCornerRadiusPx = -1f;
    private int addBorderColor = Color.parseColor("#CCCCCC");
    private float addBorderWidthPx;
    private float addDashWidthPx;
    private float addDashGapPx;
    private boolean addBorderDashed = true;

    @DrawableRes
    private int deleteIconRes = R.drawable.ic_nine_pic_delete;
    private int deleteIconSizePx = UNSET_DIMEN;
    private boolean showDelete = true;

    @Nullable
    private OnPicActionListener actionListener;

    public NinePicChooseView(@NonNull Context context) {
        this(context, null);
    }

    public NinePicChooseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NinePicChooseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        itemGapPx = Math.round(8f * density);
        cornerRadiusPx = 6f * density;
        addBorderWidthPx = Math.max(1f, density);
        addDashWidthPx = 6f * density;
        addDashGapPx = 4f * density;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NinePicChooseView);
            maxCount = Math.max(1, ta.getInteger(R.styleable.NinePicChooseView_npc_maxCount, maxCount));
            spanCount = Math.max(1, ta.getInteger(R.styleable.NinePicChooseView_npc_spanCount, spanCount));
            itemGapPx = ta.getDimensionPixelSize(R.styleable.NinePicChooseView_npc_itemGap, itemGapPx);
            cornerRadiusPx = ta.getDimension(R.styleable.NinePicChooseView_npc_cornerRadius, cornerRadiusPx);

            deleteIconRes = ta.getResourceId(R.styleable.NinePicChooseView_npc_deleteIcon, deleteIconRes);
            deleteIconSizePx = ta.getDimensionPixelSize(
                    R.styleable.NinePicChooseView_npc_deleteIconSize, deleteIconSizePx);
            showDelete = ta.getBoolean(R.styleable.NinePicChooseView_npc_showDelete, showDelete);

            addIconRes = ta.getResourceId(R.styleable.NinePicChooseView_npc_addIcon, addIconRes);
            addIconSizePx = ta.getDimensionPixelSize(
                    R.styleable.NinePicChooseView_npc_addIconSize, addIconSizePx);
            addIconTint = ta.getColor(R.styleable.NinePicChooseView_npc_addIconTint, addIconTint);
            addIconPaddingPx = ta.getDimensionPixelSize(
                    R.styleable.NinePicChooseView_npc_addIconPadding, addIconPaddingPx);
            addBgColor = ta.getColor(R.styleable.NinePicChooseView_npc_addBgColor, addBgColor);
            addCornerRadiusPx = ta.getDimension(
                    R.styleable.NinePicChooseView_npc_addCornerRadius, addCornerRadiusPx);
            addBorderColor = ta.getColor(R.styleable.NinePicChooseView_npc_addBorderColor, addBorderColor);
            addBorderWidthPx = ta.getDimension(
                    R.styleable.NinePicChooseView_npc_addBorderWidth, addBorderWidthPx);
            addDashWidthPx = ta.getDimension(
                    R.styleable.NinePicChooseView_npc_addDashWidth, addDashWidthPx);
            addDashGapPx = ta.getDimension(
                    R.styleable.NinePicChooseView_npc_addDashGap, addDashGapPx);
            addBorderDashed = ta.getBoolean(
                    R.styleable.NinePicChooseView_npc_addBorderDashed, addBorderDashed);
            ta.recycle();
        }

        LayoutInflater.from(context).inflate(R.layout.view_nine_pic_choose, this, true);
        recyclerView = findViewById(R.id.rvNinePic);
        GridLayoutManager glm = new GridLayoutManager(context, spanCount);
        recyclerView.setLayoutManager(glm);
        recyclerView.setNestedScrollingEnabled(false);
        if (itemGapPx > 0) {
            recyclerView.addItemDecoration(new GridSpacingDecoration(spanCount, itemGapPx));
        }
        adapter = new PicAdapter();
        recyclerView.setAdapter(adapter);
    }

    private float resolveAddCornerRadius() {
        return addCornerRadiusPx >= 0f ? addCornerRadiusPx : cornerRadiusPx;
    }

    public void setOnPicActionListener(@Nullable OnPicActionListener listener) {
        this.actionListener = listener;
    }

    /** 供预览 zoom 动画绑定（与已选图 position 对齐；加号格在末尾不影响）。 */
    @NonNull
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = Math.max(1, maxCount);
        adapter.notifyDataSetChanged();
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getRemainCount() {
        return Math.max(0, maxCount - adapter.list.size());
    }

    @NonNull
    public ArrayList<LocalMedia> getSelected() {
        return new ArrayList<>(adapter.list);
    }

    @NonNull
    public List<LocalMedia> getAll() {
        return adapter.list;
    }

    /** 覆盖设置（去重并截断到 maxCount）。 */
    @SuppressLint("NotifyDataSetChanged")
    public void setMediaList(@Nullable List<LocalMedia> mediaList) {
        adapter.list.clear();
        if (mediaList != null) {
            for (LocalMedia media : mediaList) {
                if (media == null || adapter.list.size() >= maxCount) {
                    continue;
                }
                if (!contains(adapter.list, media)) {
                    adapter.list.add(media);
                }
            }
        }
        adapter.notifyDataSetChanged();
        notifyChanged();
    }

    /**
     * 追加选图并去重。
     *
     * @param compareLocalPath 保留参数，已统一走 {@link #isSameMedia}
     */
    @SuppressLint("NotifyDataSetChanged")
    public void addMedia(@Nullable List<LocalMedia> mediaList, boolean compareLocalPath) {
        if (mediaList == null || mediaList.isEmpty()) {
            return;
        }
        for (LocalMedia incoming : mediaList) {
            if (incoming == null || adapter.list.size() >= maxCount) {
                break;
            }
            if (!contains(adapter.list, incoming)) {
                adapter.list.add(incoming);
            }
        }
        while (adapter.list.size() > maxCount) {
            adapter.list.remove(adapter.list.size() - 1);
        }
        adapter.notifyDataSetChanged();
        notifyChanged();
    }

    public void addMedia(@Nullable List<LocalMedia> mediaList) {
        addMedia(mediaList, true);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void removeAt(int position) {
        if (position < 0 || position >= adapter.list.size()) {
            return;
        }
        adapter.list.remove(position);
        adapter.notifyDataSetChanged();
        notifyChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        adapter.list.clear();
        adapter.notifyDataSetChanged();
        notifyChanged();
    }

    // —— 样式 setter ——

    public void setCornerRadiusPx(float px) {
        cornerRadiusPx = Math.max(0f, px);
        adapter.notifyDataSetChanged();
    }

    public void setAddIcon(@DrawableRes int resId) {
        addIconRes = resId;
        adapter.notifyDataSetChanged();
    }

    public void setAddIconSizePx(int px) {
        addIconSizePx = Math.max(0, px);
        adapter.notifyDataSetChanged();
    }

    public void setAddIconTint(@ColorInt int color) {
        addIconTint = color;
        adapter.notifyDataSetChanged();
    }

    public void setAddCellStyle(@ColorInt int bgColor,
                                @ColorInt int borderColor,
                                float borderWidthPx,
                                float cornerRadiusPx,
                                float dashWidthPx,
                                float dashGapPx,
                                boolean dashed) {
        this.addBgColor = bgColor;
        this.addBorderColor = borderColor;
        this.addBorderWidthPx = Math.max(0f, borderWidthPx);
        this.addCornerRadiusPx = Math.max(0f, cornerRadiusPx);
        this.addDashWidthPx = Math.max(0f, dashWidthPx);
        this.addDashGapPx = Math.max(0f, dashGapPx);
        this.addBorderDashed = dashed;
        adapter.notifyDataSetChanged();
    }

    public void setDeleteIcon(@DrawableRes int resId) {
        deleteIconRes = resId;
        adapter.notifyDataSetChanged();
    }

    public void setShowDelete(boolean show) {
        showDelete = show;
        adapter.notifyDataSetChanged();
    }

    private void notifyChanged() {
        if (actionListener != null) {
            actionListener.onChanged(adapter.list);
        }
    }

    private static boolean contains(List<LocalMedia> list, LocalMedia target) {
        for (LocalMedia item : list) {
            if (isSameMedia(item, target)) {
                return true;
            }
        }
        return false;
    }

    static boolean isSameMedia(@Nullable LocalMedia a, @Nullable LocalMedia b) {
        if (a == null || b == null) {
            return false;
        }
        if (a == b) {
            return true;
        }
        long idA = a.getId();
        long idB = b.getId();
        if (idA > 0 && idA == idB) {
            return true;
        }
        if (nonEmptyEquals(a.getPath(), b.getPath())
                || nonEmptyEquals(a.getRealPath(), b.getRealPath())) {
            return true;
        }
        String[] keysA = pathKeys(a);
        String[] keysB = pathKeys(b);
        for (String ka : keysA) {
            if (TextUtils.isEmpty(ka)) {
                continue;
            }
            for (String kb : keysB) {
                if (TextUtils.equals(ka, kb)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean nonEmptyEquals(@Nullable String a, @Nullable String b) {
        return !TextUtils.isEmpty(a) && TextUtils.equals(a, b);
    }

    private static String[] pathKeys(@NonNull LocalMedia media) {
        return new String[]{
                media.getPath(),
                media.getRealPath(),
                media.getOriginalPath(),
                media.getCompressPath(),
                media.getSandboxPath(),
                media.getAvailablePath()
        };
    }

    private class PicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final ArrayList<LocalMedia> list = new ArrayList<>();

        @Override
        public int getItemViewType(int position) {
            if (list.size() < maxCount && position == getItemCount() - 1) {
                return TYPE_ADD;
            }
            return TYPE_PIC;
        }

        @Override
        public int getItemCount() {
            return list.size() >= maxCount ? maxCount : list.size() + 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_PIC) {
                return new PicHolder(inflater.inflate(R.layout.item_nine_pic_media, parent, false));
            }
            return new AddHolder(inflater.inflate(R.layout.item_nine_pic_add, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PicHolder) {
                bindPic((PicHolder) holder, position);
            } else if (holder instanceof AddHolder) {
                bindAdd((AddHolder) holder);
            }
        }

        private void bindPic(PicHolder holder, int position) {
            LocalMedia media = list.get(position);
            holder.iv.setCornerRadiusPx(cornerRadiusPx);
            String path = media.getAvailablePath();
            if (TextUtils.isEmpty(path)) {
                path = media.getPath();
            }
            Glide.with(holder.iv).load(path).centerCrop().into(holder.iv);
            holder.ivDelete.setImageResource(deleteIconRes);
            holder.ivDelete.setVisibility(showDelete ? VISIBLE : GONE);
            if (deleteIconSizePx != UNSET_DIMEN) {
                ViewGroup.LayoutParams lp = holder.ivDelete.getLayoutParams();
                lp.width = deleteIconSizePx;
                lp.height = deleteIconSizePx;
                holder.ivDelete.setLayoutParams(lp);
            }
            holder.ivDelete.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                list.remove(pos);
                notifyDataSetChanged();
                notifyChanged();
            });
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || actionListener == null) {
                    return;
                }
                actionListener.onPreview(pos, new ArrayList<>(list));
            });
        }

        private void bindAdd(AddHolder holder) {
            holder.root.setCellStyle(
                    addBgColor,
                    addBorderColor,
                    addBorderWidthPx,
                    resolveAddCornerRadius(),
                    addDashWidthPx,
                    addDashGapPx,
                    addBorderDashed);
            holder.ivAdd.setImageResource(addIconRes);
            if (addIconSizePx != UNSET_DIMEN) {
                ViewGroup.LayoutParams lp = holder.ivAdd.getLayoutParams();
                lp.width = addIconSizePx;
                lp.height = addIconSizePx;
                holder.ivAdd.setLayoutParams(lp);
            }
            if (addIconPaddingPx != UNSET_DIMEN) {
                holder.ivAdd.setPadding(addIconPaddingPx, addIconPaddingPx,
                        addIconPaddingPx, addIconPaddingPx);
            }
            if (addIconTint != UNSET_COLOR) {
                ImageViewCompat.setImageTintList(holder.ivAdd,
                        android.content.res.ColorStateList.valueOf(addIconTint));
            } else {
                ImageViewCompat.setImageTintList(holder.ivAdd, null);
            }
            holder.itemView.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAdd(getRemainCount());
                }
            });
        }
    }

    static final class PicHolder extends RecyclerView.ViewHolder {
        final NineGridRoundImageView iv;
        final ImageView ivDelete;

        PicHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivNinePic);
            ivDelete = itemView.findViewById(R.id.ivNinePicDelete);
        }
    }

    static final class AddHolder extends RecyclerView.ViewHolder {
        final DashBorderSquareLayout root;
        final ImageView ivAdd;

        AddHolder(@NonNull View itemView) {
            super(itemView);
            root = (DashBorderSquareLayout) itemView;
            ivAdd = itemView.findViewById(R.id.ivNinePicAdd);
        }
    }

    private static final class GridSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position < 0) {
                return;
            }
            int column = position % spanCount;
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}
