package com.templete.project.ui.activity;

import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.hjq.shape.view.ShapeTextView;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.databinding.ActivityCollapsingDemoListBinding;

/**
 * Material Design 嵌套折叠布局场景列表（Coordinator + AppBar）。
 * 与 {@link ScrollActivity} 无关，按主流 App 页面形态拆成多套差异化 Demo。
 */
public class CollapsingDemoListActivity extends BaseActivity<ActivityCollapsingDemoListBinding> {

    private static final String[] TITLES = {
            "① 社交个人主页 · 封面 Parallax",
            "② 音乐歌手页 · 封面 + 吸顶 Tab",
            "③ 电商店铺 · 店招 + 吸顶搜索",
            "④ 短视频作者 · 深色高封面",
            "⑤ 资讯详情 · 顶图收至标题",
            "⑥ 直播间资料 · 横幅收至最小高",
            "⑦ 外卖商家 · 店招 + 钉住优惠条",
            "⑧ 社区笔记 · 大图 + 作者条",
            "⑨ 酒店详情 · 图集 + enterAlways 价条",
            "⑩ 蝉游记详情 · Header 毛玻璃 + 下拉放大",
    };

    private static final int[] LAYOUTS = {
            R.layout.collapsing_scene_profile,
            R.layout.collapsing_scene_music,
            R.layout.collapsing_scene_shop,
            R.layout.collapsing_scene_creator,
            R.layout.collapsing_scene_news,
            R.layout.collapsing_scene_live,
            R.layout.collapsing_scene_food,
            R.layout.collapsing_scene_note,
            R.layout.collapsing_scene_hotel,
            0, // 蝉游记走独立 Activity，不用 CollapsingScene
    };

    @Override
    protected ActivityCollapsingDemoListBinding viewBinding() {
        return ActivityCollapsingDemoListBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("Design 折叠布局场景");
    }

    @Override
    public void initView() {
        LinearLayout container = mViewBinding.llScenes;
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < TITLES.length; i++) {
            ShapeTextView item = (ShapeTextView) inflater.inflate(
                    R.layout.item_collapsing_scene_entry, container, false);
            item.setText(TITLES[i]);
            final int layoutRes = LAYOUTS[i];
            final String title = TITLES[i];
            item.setOnClickListener(v -> {
                if (layoutRes == 0) {
                    ChanyoujiDetailActivity.start(this);
                } else {
                    CollapsingSceneActivity.start(this, title, layoutRes);
                }
            });
            container.addView(item);
        }
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }
}
