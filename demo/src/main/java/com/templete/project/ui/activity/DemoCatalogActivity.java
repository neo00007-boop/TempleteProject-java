package com.templete.project.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.flyco.tablayoutsamples.ui.SimpleHomeActivity;
import com.greendao.db.util.GsonUtil;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.shape.view.ShapeTextView;
import com.lib.base.bean.VideoBuilder;
import com.lib.base.config.App;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.activity.MvpActivity;
import com.lib.base.util.JsonDataUtil;
import com.lib.base.util.PermissionUtil;
import com.lib.base.util.intent.IntentData;
import com.templete.project.R;
import com.templete.project.bean.JBean;
import com.templete.project.bean.PBean;
import com.templete.project.databinding.ActivityDemoCatalogBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 现有 Demo 合集，按原来五个分页归类。
 */
public class DemoCatalogActivity extends BaseActivity<ActivityDemoCatalogBinding> {

    @Override
    protected ActivityDemoCatalogBinding viewBinding() {
        return ActivityDemoCatalogBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("Demo 合集");
    }

    @Override
    public void initView() {
        LinearLayout container = mViewBinding.llCatalog;
        String group = "";
        for (Entry entry : entries()) {
            if (!group.equals(entry.group)) {
                group = entry.group;
                container.addView(section(group));
            }
            container.addView(button(entry.title, v -> entry.open(this)));
        }
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @NonNull
    private TextView section(@NonNull String title) {
        TextView text = new TextView(this);
        text.setText(title);
        text.setTextColor(getResources().getColor(R.color.common_text_color));
        text.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.x36));
        text.setPadding(0, getResources().getDimensionPixelSize(R.dimen.x24), 0, getResources().getDimensionPixelSize(R.dimen.x16));
        return text;
    }

    @NonNull
    private View button(@NonNull String title, @NonNull View.OnClickListener listener) {
        ShapeTextView item = new ShapeTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.x120));
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.x24);
        item.setLayoutParams(params);
        item.setText(title);
        item.setGravity(Gravity.CENTER);
        item.setTextColor(getResources().getColor(R.color.white));
        item.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.x36));
        item.getShapeDrawableBuilder()
                .setSolidColor(getResources().getColor(R.color.common_accent_color))
                .setRadius(getResources().getDimension(R.dimen.x60))
                .intoBackground();
        item.setOnClickListener(listener);
        return item;
    }

    private void open(@NonNull Class<?> page) {
        startAty(this, page);
    }

    @NonNull
    private static List<Entry> entries() {
        List<Entry> list = new ArrayList<>();
        list.add(new Entry("常用控件", "Popup 弹窗", PopActivity.class));
        list.add(new Entry("常用控件", "Shape 圆角背景", ShapeActivity.class));
        list.add(new Entry("常用控件", "Dialog 对话框", DialogActivity.class));
        list.add(new Entry("常用控件", "BottomSheet 底部弹层", SheetActivity.class));
        list.add(new Entry("常用控件", "TabLayout 多样式", SimpleHomeActivity.class));
        list.add(new Entry("常用控件", "评分条 RatingBar", ViewActivity.class));
        list.add(new Entry("常用控件", "百分比布局", RatioActivity.class));
        list.add(new Entry("常用控件", "Glide 图片变换", GlideActivity.class));
        list.add(new Entry("常用控件", "沉浸式状态栏", ImmersionActivity.class));
        list.add(new Entry("常用控件", "Txt 富文本标签", TxtActivity.class));
        list.add(new Entry("常用控件", "Shimmer 骨架屏", ShimmerActivity.class));
        list.add(new Entry("常用控件", "官方 FlowLayout", FlexBoxActivity.class));
        list.add(new Entry("常用控件", "自定义 FlowLayout", FlexActivity.class));
        list.add(new Entry("常用控件", "Canvas 图形绘制", CanvasActivity.class));
        list.add(new Entry("常用控件", "九宫格（展示 / 选图）", NineGridActivity.class));
        list.add(new Entry("常用控件", "折叠 TextView", FoldTextActivity.class));
        list.add(new Entry("常用控件", "验证码输入", VerificationCodeActivity.class));

        list.add(new Entry("基础工具", "Gson 容错解析", DemoCatalogActivity::showGson));
        list.add(new Entry("基础工具", "统一返回体 ApiResult", ApiResultActivity.class));
        list.add(new Entry("基础工具", "权限申请（Android 10+）", DemoCatalogActivity::askPermission));
        list.add(new Entry("基础工具", "双缓存（内存 + SP）", DoubleCacheActivity.class));
        list.add(new Entry("基础工具", "Intent 传参", DemoCatalogActivity::openIntent));
        list.add(new Entry("基础工具", "正则校验 PatternUtil", PatternActivity.class));
        list.add(new Entry("基础工具", "DataBinding", DatabindingActivity.class));
        list.add(new Entry("基础工具", "LiveDataBus 事件总线", DataBusActivity.class));
        list.add(new Entry("基础工具", "MVP + Holder", MvpActivity.class));
        list.add(new Entry("基础工具", "MVVM + 动画", TestMvvmActivity.class));
        list.add(new Entry("基础工具", "页面 Loading / Dialog 状态", LoadingActivity.class));
        list.add(new Entry("基础工具", "图片 / 视频选择器", SelectorActivity.class));
        list.add(new Entry("基础工具", "保存到相册", GallerySaveActivity.class));

        list.add(new Entry("列表刷新", "下拉刷新 · Grass", GrassFreshActivity.class));
        list.add(new Entry("列表刷新", "下拉刷新 · Sun", SunFreshActivity.class));
        list.add(new Entry("列表刷新", "横向刷新", HorizontalFreshActivity.class));
        list.add(new Entry("列表刷新", "悬浮刷新（Coordinator）", ScrollActivity.class));
        list.add(new Entry("列表刷新", "列表整体位移", TranslateActivity.class));
        list.add(new Entry("列表刷新", "刷新列表基类 · Activity", FreshListDemoActivity.class));
        list.add(new Entry("列表刷新", "多类型刷新列表 · Activity", FreshMultiListDemoActivity.class));
        list.add(new Entry("列表刷新", "刷新列表基类 · Fragment", activity -> activity.openFreshFragment(false)));
        list.add(new Entry("列表刷新", "多类型刷新列表 · Fragment", activity -> activity.openFreshFragment(true)));
        list.add(new Entry("列表刷新", "字母索引列表", IndexActivity.class));
        list.add(new Entry("列表刷新", "吸顶 Pinned", PinnedActivity.class));
        list.add(new Entry("列表刷新", "Design 折叠布局场景", CollapsingDemoListActivity.class));

        list.add(new Entry("页面交互", "ViewPager 多级导航", NavsActivity.class));
        list.add(new Entry("页面交互", "单元素共享转场", OneElementActivity.class));
        list.add(new Entry("页面交互", "多元素共享转场", MultipleElementActivity.class));
        list.add(new Entry("页面交互", "输入法适配", SoftActivity.class));

        list.add(new Entry("音视频", "简易视频播放器", DemoCatalogActivity::playVideo));
        list.add(new Entry("音视频", "帖子音频组件", PostAudioActivity.class));
        list.add(new Entry("音视频", "聊天 @群员", MentionEditActivity.class));
        return list;
    }

    private void openFreshFragment(boolean multi) {
        Intent intent = new Intent(this, FreshFragmentHostActivity.class);
        intent.putExtra(FreshFragmentHostActivity.EXTRA_MULTI, multi);
        startActivity(intent);
    }

    private void showGson() {
        String json = JsonDataUtil.getJson(App.getContext(), "app.json");
        JBean bean = GsonUtil.getNoCrashInstance().fromJson(json, JBean.class);
        String text = GsonUtil.getNoCrashInstance().toJson(bean);
        logD(GsonUtil.TAG, text);
        toast(bean == null ? "解析失败" : "解析成功，详见日志");
    }

    private void askPermission() {
        String[] permissions = {Permission.CAMERA};
        if (PermissionUtil.isGranted(this, permissions)) {
            toast("相机权限已允许");
            return;
        }
        PermissionUtil.requestPermission(this, new OnPermissionCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted, boolean allGranted) {
                toast(allGranted ? "相机权限已允许" : "相机权限未完全允许");
            }

            @Override
            public void onDenied(@NonNull List<String> denied, boolean doNotAskAgain) {
                toast("相机权限被拒绝");
            }
        }, permissions);
    }

    private void openIntent() {
        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        ArrayList<PBean> people = new ArrayList<>();
        people.add(new PBean("张三1"));
        people.add(new PBean("张三2"));
        startAty(this, TargetActivity.class,
                IntentData.put("key1", 1),
                IntentData.put("key2", "1"),
                IntentData.put("key3", new int[]{1, 2, 3}),
                IntentData.put("key4", new PBean("张三")),
                IntentData.putListInt("key5", numbers),
                IntentData.putListPar("key6", people));
    }

    private void playVideo() {
        VideoBuilder.newBuilder()
                .setVideoTitle("速度与激情特别行动")
                .setVideoSource("https://media.w3.org/2010/05/sintel/trailer.mp4")
                .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .start(this);
    }

    private static final class Entry {
        final String group;
        final String title;
        final Opener opener;

        Entry(@NonNull String group, @NonNull String title, @NonNull Class<?> page) {
            this.group = group;
            this.title = title;
            this.opener = activity -> activity.open(page);
        }

        Entry(@NonNull String group, @NonNull String title, @NonNull Opener opener) {
            this.group = group;
            this.title = title;
            this.opener = opener;
        }

        void open(@NonNull DemoCatalogActivity activity) {
            opener.open(activity);
        }
    }

    private interface Opener {
        void open(@NonNull DemoCatalogActivity activity);
    }
}
