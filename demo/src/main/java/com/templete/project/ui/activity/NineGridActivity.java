package com.templete.project.ui.activity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.widget.ninegrid.NineGridImageView;
import com.lib.base.ui.widget.ninegrid.NinePicChooseView;
import com.lib.base.ui.widget.ninegrid.UrlNineGridAdapter;
import com.lib.base.util.upload.MeOnResultCallbackListener;
import com.lib.base.util.upload.MediaPickerUtil;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.templete.project.R;
import com.templete.project.databinding.ActivityNineGridBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 九宫格 Demo：发帖选图 {@link NinePicChooseView} + 展示 {@link NineGridImageView}。
 */
public class NineGridActivity extends BaseActivity<ActivityNineGridBinding> {

    private static final String[] DEMO_URLS = {
            "https://picsum.photos/id/10/400/400",
            "https://picsum.photos/id/11/400/400",
            "https://picsum.photos/id/12/400/400",
            "https://picsum.photos/id/13/400/400",
            "https://picsum.photos/id/14/400/400",
            "https://picsum.photos/id/15/400/400",
            "https://picsum.photos/id/16/400/400",
            "https://picsum.photos/id/17/400/400",
            "https://picsum.photos/id/18/400/400",
            "https://picsum.photos/id/19/400/400",
            "https://picsum.photos/id/20/400/400",
            "https://picsum.photos/id/21/400/400",
            "https://picsum.photos/id/22/400/400",
            "https://picsum.photos/id/23/400/400",
            "https://picsum.photos/id/24/400/400",
    };

    @Override
    protected ActivityNineGridBinding viewBinding() {
        return ActivityNineGridBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("九宫格（展示 / 选图）");
    }

    @Override
    public void initView() {
        setupChooseView();
        LinearLayout container = mViewBinding.llNineGridSamples;
        LayoutInflater inflater = getLayoutInflater();

        addSample(container, inflater, "1 张", "微信：边长≈2格+间距（约 2/3 宽）", 1, false);
        addSample(container, inflater, "2 张", "微信：两格并排，总宽约 2/3", 2, false);
        addSample(container, inflater, "3 张", "微信：三格满宽", 3, false);
        addSample(container, inflater, "4 张", "微信：2×2，格子同三列 cell，总宽约 2/3", 4, false);
        addSample(container, inflater, "5 张", "2 行布局", 5, false);
        addSample(container, inflater, "6 张", "2 行满格", 6, false);
        addSample(container, inflater, "7 张", "3 行", 7, false);
        addSample(container, inflater, "8 张", "3 行", 8, false);
        addSample(container, inflater, "9 张", "标准九宫格", 9, false);
        addSample(container, inflater, "12 张（>9）", "只展 9 格，末格显示剩余「3+」", 12, false);
        addSample(container, inflater, "15 张 + 样式定制",
                "末格「6+」；大圆角 + 全局蒙层 + 红底白字", 15, true);
    }

    private void setupChooseView() {
        NinePicChooseView choose = mViewBinding.ninePicChoose;
        refreshChooseState();
        choose.setOnPicActionListener(new NinePicChooseView.OnPicActionListener() {
            @Override
            public void onAdd(int remain) {
                if (remain <= 0) {
                    toast("已达上限");
                    return;
                }
                // max=总上限 + 已选回显；customCamera=true 相册内可拍摄
                MediaPickerUtil.getMedia(NineGridActivity.this, SelectMimeType.ofImage(),
                        true, false, choose.getMaxCount(), choose.getSelected(),
                        new MeOnResultCallbackListener() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {
                                // 带回含已选：用全量覆盖并去重截断更稳
                                choose.setMediaList(result);
                            }
                        });
            }

            @Override
            public void onPreview(int position, @NonNull ArrayList<LocalMedia> data) {
                MediaPickerUtil.preview(NineGridActivity.this, position, data, true, true,
                        choose.getRecyclerView(),
                        deletePos -> choose.setMediaList(data));
            }

            @Override
            public void onChanged(@NonNull List<LocalMedia> data) {
                refreshChooseState();
            }
        });
    }

    private void refreshChooseState() {
        NinePicChooseView choose = mViewBinding.ninePicChoose;
        mViewBinding.tvChooseState.setText("已选 " + choose.getAll().size() + "/" + choose.getMaxCount());
    }

    private void addSample(LinearLayout container, LayoutInflater inflater,
                           String title, String desc, int count, boolean customStyle) {
        View item = inflater.inflate(R.layout.item_nine_grid_sample, container, false);
        TextView tvTitle = item.findViewById(R.id.tvTitle);
        TextView tvDesc = item.findViewById(R.id.tvDesc);
        NineGridImageView<String> nineGrid = item.findViewById(R.id.nineGrid);
        tvTitle.setText(title);
        tvDesc.setText(desc);

        if (customStyle) {
            nineGrid.setCornerRadius(getResources().getDimension(R.dimen.x32));
            nineGrid.setMaskColor(Color.parseColor("#33ff0000"));
            nineGrid.setMoreMaskColor(Color.parseColor("#80000000"));
            nineGrid.setMoreTextSize(getResources().getDimension(R.dimen.x36));
            nineGrid.setMoreTextColor(Color.WHITE);
            nineGrid.setMoreTextBgColor(Color.parseColor("#E6E53935"));
            nineGrid.setMoreTextCornerRadius(getResources().getDimension(R.dimen.x12));
        }
        List<String> urls = takeUrls(count);
        nineGrid.setAdapter(new UrlNineGridAdapter());
        nineGrid.setItemImageClickListener((context, imageView, index, list) ->
                MediaPickerUtil.preview(context, index, list, true, true,
                        nineGrid,
                        deletePos -> nineGrid.setImagesData(list)));
        nineGrid.setImagesData(urls);
        container.addView(item);
    }

    private static List<String> takeUrls(int count) {
        List<String> all = Arrays.asList(DEMO_URLS);
        if (count <= all.size()) {
            return new ArrayList<>(all.subList(0, count));
        }
        List<String> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(all.get(i % all.size()));
        }
        return out;
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }
}
