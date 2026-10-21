package com.templete.project.ui.fragment;

import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lib.base.bean.VideoBuilder;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.fragment.BaseFragment;
import com.templete.project.databinding.FifthFragmentBinding;
import com.templete.project.ui.activity.MentionEditActivity;
import com.templete.project.ui.activity.PostAudioActivity;

/**
 * Demo 第五页：音视频。
 */
public class FifthFragment extends BaseFragment<FifthFragmentBinding> {

    @Override
    public FifthFragmentBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FifthFragmentBinding.inflate(getLayoutInflater(), container, false);
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
                    if (v.equals(mViewBinding.tv1)) {
                        VideoBuilder.newBuilder()
                                .setVideoTitle("速度与激情特别行动")
                                .setVideoSource("https://media.w3.org/2010/05/sintel/trailer.mp4")
                                .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .start(requireActivity());
                        return;
                    }
                    Class<? extends BaseActivity<?>> clazz = null;
                    if (v.equals(mViewBinding.tv2)) {
                        clazz = PostAudioActivity.class;
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = MentionEditActivity.class;
                    }
                    if (clazz != null) {
                        ((BaseActivity<?>) requireActivity()).startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2, mViewBinding.tv3);
    }

    @Override
    public void initData() {
    }
}
