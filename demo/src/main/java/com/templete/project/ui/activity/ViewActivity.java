package com.templete.project.ui.activity;

import android.annotation.SuppressLint;

import com.lib.base.glide.GlideUtil;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.databinding.AndratingBarActivityBinding;

/**
 * PackageName  com.templete.project.ui
 * ProjectName  TempleteProject-java
 * Date         2022/1/4.
 *
 * @author xwchen
 */
@SuppressLint("CheckResult")
public class ViewActivity extends BaseActivity<AndratingBarActivityBinding> {
    public static final String TAG = "AndRatingBarActivity";

    @Override
    protected AndratingBarActivityBinding viewBinding() {
        return AndratingBarActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("AndratingBar");
    }

    @Override
    public void initView() {
        mViewBinding.tv.setOnClickListener(v -> {
            mViewBinding.btn.setChecked(!mViewBinding.btn.isChecked());
            mViewBinding.btn.setEnabled(!mViewBinding.btn.isEnabled());
        });
        mViewBinding.dragContainer.setDragListener(() -> logD(TAG, "has drag"));
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        String url1 = "http://images0.zaijiawan.com/nanjing/huazhuangdaquan/2/5-1.jpg@!16app_video_list";
        String url2 = "http://images0.zaijiawan.com/nanjing/huazhuangdaquan/2/5-2.jpg@!16app_video_list";
        String url3 = "http://images0.zaijiawan.com/nanjing/huazhuangdaquan/2/5-3.jpg@!16app_video_list";

        GlideUtil.loadPic(this, url1, mViewBinding.iv);
        GlideUtil.loadPic(this, url1, mViewBinding.iv1);
        GlideUtil.loadPicCircle(this, url2, mViewBinding.iv2);
        GlideUtil.loadPicRound(this, url3, mViewBinding.iv3, (int) getDimen(R.dimen.x40));
//        GlideUtil.loadPicRound(this, url2, mViewBinding
//                .ratioIv, (int) getResources().getDimension(R.dimen.x40));
    }
}
