package com.module.player.ui;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.lib.router.Router;
import com.lib.router.RoutePath;
import com.lib.base.ui.activity.BaseActivity;
import com.module.player.databinding.PyActivityBinding;

@Route(path = RoutePath.PLAYER_ACTIVITY)
public class PlayerActivity extends BaseActivity<PyActivityBinding> {
    @Autowired(name = RoutePath.PLAYER_KEY_VIDEO_ID)
    String videoId;

    @Override
    public PyActivityBinding viewBinding() {
        return PyActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        Router.inject(this);
        setTitleStr("播放");
    }

    @Override
    public void initView() {
        mViewBinding.tv.setText(videoId == null ? "未传视频 id" : "视频 id：" + videoId);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {

    }
}
