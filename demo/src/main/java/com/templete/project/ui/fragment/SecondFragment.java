package com.templete.project.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.greendao.db.util.GsonUtil;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.lib.base.config.App;
import com.lib.base.ui.activity.MvpActivity;
import com.lib.base.ui.fragment.BaseFragment;
import com.lib.base.util.IntentUtil;
import com.lib.base.util.JsonDataUtil;
import com.lib.base.util.PermissionUtil;
import com.lib.base.util.intent.IntentData;
import com.templete.project.bean.JBean;
import com.templete.project.bean.PBean;
import com.templete.project.databinding.SecondFragmentBinding;
import com.templete.project.ui.activity.DataBusActivity;
import com.templete.project.ui.activity.DatabindingActivity;
import com.templete.project.ui.activity.DoubleCacheActivity;
import com.templete.project.ui.activity.LoadingActivity;
import com.templete.project.ui.activity.PatternActivity;
import com.templete.project.ui.activity.SelectorActivity;
import com.templete.project.ui.activity.TargetActivity;
import com.templete.project.ui.activity.TestMvvmActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo 第二页：基础工具。
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
                    Class<?> clazz = null;
                    if (v.equals(mViewBinding.tv1)) {
                        gson();
                    } else if (v.equals(mViewBinding.tv2)) {
                        permission();
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = DoubleCacheActivity.class;
                    } else if (v.equals(mViewBinding.tv4)) {
                        openIntentDemo();
                    } else if (v.equals(mViewBinding.tv5)) {
                        clazz = PatternActivity.class;
                    } else if (v.equals(mViewBinding.tv6)) {
                        IntentUtil.startActivity(requireActivity(), DatabindingActivity.class);
                    } else if (v.equals(mViewBinding.tv7)) {
                        clazz = DataBusActivity.class;
                    } else if (v.equals(mViewBinding.tv8)) {
                        clazz = MvpActivity.class;
                    } else if (v.equals(mViewBinding.tv9)) {
                        clazz = TestMvvmActivity.class;
                    } else if (v.equals(mViewBinding.tv10)) {
                        clazz = LoadingActivity.class;
                    } else if (v.equals(mViewBinding.tv11)) {
                        clazz = SelectorActivity.class;
                    }
                    if (clazz != null) {
                        getBaseActivity().startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2, mViewBinding.tv3,
                mViewBinding.tv4, mViewBinding.tv5, mViewBinding.tv6,
                mViewBinding.tv7, mViewBinding.tv8, mViewBinding.tv9,
                mViewBinding.tv10, mViewBinding.tv11);
    }

    private void openIntentDemo() {
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(3);

        ArrayList<PBean> list2 = new ArrayList<>();
        list2.add(new PBean("张三1"));
        list2.add(new PBean("张三2"));
        list2.add(new PBean("张三3"));

        getBaseActivity().startAty(requireActivity(), TargetActivity.class,
                IntentData.put("key1", 1),
                IntentData.put("key2", "1"),
                IntentData.put("key3", new int[]{1, 2, 3}),
                IntentData.put("key4", new PBean("张三")),
                IntentData.putListInt("key5", list1),
                IntentData.putListPar("key6", list2)
        );
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
}
