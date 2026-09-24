package com.templete.project.ui.fragment;

import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.greendao.db.util.GsonUtil;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.lib.base.bean.VideoBuilder;
import com.lib.base.config.App;
import com.lib.base.ui.activity.MvpActivity;
import com.lib.base.ui.fragment.BaseFragment;
import com.lib.base.util.JsonDataUtil;
import com.lib.base.util.PermissionUtil;
import com.templete.project.bean.JBean;
import com.templete.project.databinding.SecondFragmentBinding;
import com.templete.project.ui.activity.LoadingActivity;
import com.templete.project.ui.activity.ScrollActivity;
import com.templete.project.ui.activity.TestMvvmActivity;
import com.templete.project.ui.activity.TranslateActivity;

import java.util.List;

/**
 * PackageName  com.templete.project.ui.fragment
 * ProjectName  TempleteProject
 * Date         10/10/21.
 *
 * @author xwchen
 */

public class SecondFragment extends BaseFragment<SecondFragmentBinding> {
    public static final String TAG = "SecondFragment";

    @Override
    public SecondFragmentBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return SecondFragmentBinding.inflate(getLayoutInflater(), container, false);
    }

    @Override
    public void inits() {
    }

    @Override
    public void initView() {
    }

    @Override
    public void initEvent() {
        setOnClickListener(v -> {
                    Class clazz = null;
                    if (v.equals(mViewBinding.tv1)) {
                        gson();
                    } else if (v.equals(mViewBinding.tv2)) {
                        permission();
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = MvpActivity.class;
                    } else if (v.equals(mViewBinding.tv4)) {
                        clazz = TestMvvmActivity.class;
                    } else if (v.equals(mViewBinding.tv5)) {
                        clazz = LoadingActivity.class;
                    } else if (v.equals(mViewBinding.tv6)) {
                        clazz = TranslateActivity.class;
                    } else if (v.equals(mViewBinding.tv7)) {
                        clazz = ScrollActivity.class;
                    } /*else if (v.equals(mViewBinding.tv8)) {
                        clazz = FloatingActivity.class;
                    } else if (v.equals(mViewBinding.tv9)) {
                        clazz = FloatingNewActivity.class;
                    } */ else if (v.equals(mViewBinding.tv10)) {
                        VideoBuilder.newBuilder()
                                .setVideoTitle("速度与激情特别行动")
                                .setVideoSource("https://media.w3.org/2010/05/sintel/trailer.mp4")
                                //.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                                .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .start(requireActivity());
                    }
                    if (clazz != null) {
                        getBaseActivity().startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2, mViewBinding.tv3,
                mViewBinding.tv4, mViewBinding.tv5, mViewBinding.tv6,
                mViewBinding.tv7, /*mViewBinding.tv8, mViewBinding.tv9,*/
                mViewBinding.tv10);
    }

    private void gson() {
        String json = JsonDataUtil.getJson(App.getContext(), "app.json");
        JBean jBean = GsonUtil.getNoCrashInstance().fromJson(json, JBean.class);
        logD(GsonUtil.TAG, GsonUtil.getNoCrashInstance().toJson(jBean));
    }

    private void permission() {
        String[] arr = {Permission.CAMERA, Permission.READ_SMS};
        boolean hasPermission = PermissionUtil.isGranted(getContext(), arr);
        if (hasPermission) {
            logD(TAG, "permission ok1");
        } else {
            PermissionUtil.requestPermission(getContext(), new OnPermissionCallback() {
                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    if (all) {
                        logD(TAG, "permission ok2");
                    } else {
                        toast("获取部分权限成功，但部分权限未正常授予");
                    }
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                    if (never) {
                        toast("被永久拒绝授权，请手动授予权限");
                        PermissionUtil.startPermissionActivity(getContext(), permissions);
                    } else {
                        toast("获取权限失败");
                    }
                }
            }, arr);
        }
    }

    @Override
    public void initData() {
    }

    /* //判断从系统权限界面返回后权限状态
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtil.REQUEST_CODE) {
            if (PermissionUtil.isGranted(this, Permission.CAMERA) &&
                    PermissionUtil.isGranted(this, Permission.READ_SMS)) {
                //toast("用户已经在权限设置页授予了录音和日历权限");
                mViewModel.initOnlyId();
            } else {
                toast("用户没有权限设置页授予权限");
            }
        }
    }*/
}
