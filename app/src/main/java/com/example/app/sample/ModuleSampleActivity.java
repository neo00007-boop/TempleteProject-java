package com.example.app.sample;

import android.content.Intent;

import com.example.app.databinding.ModuleSampleActivityBinding;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.LoginUtil;
import com.lib.router.Router;
import com.lib.router.RoutePath;
import com.module.square.api.SquareService;

/**
 * 业务模块用法示例：路由跳转、带参跳转、跨模块服务，以及 Room 数据库示例入口。demo 只放基础库示例，不依赖业务模块。
 */
public class ModuleSampleActivity extends BaseActivity<ModuleSampleActivityBinding> {
    public static final String TAG = "ModuleSampleActivity";

    @Override
    protected ModuleSampleActivityBinding viewBinding() {
        return ModuleSampleActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("业务模块示例");
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {
        mViewBinding.btnSquare.setOnClickListener(v -> Router.open(this, RoutePath.SQUARE_ACTIVITY));
        mViewBinding.btnPlayer.setOnClickListener(v -> Router.open(this, RoutePath.PLAYER_ACTIVITY));
        mViewBinding.btnPlayerWithId.setOnClickListener(v -> Router.build(RoutePath.PLAYER_ACTIVITY)
                .withString(RoutePath.PLAYER_KEY_VIDEO_ID, "456")
                .navigation(this));
        mViewBinding.btnLogin.setOnClickListener(v -> LoginUtil.toLogin());
        mViewBinding.btnSquareService.setOnClickListener(v -> callSquareService());
        mViewBinding.btnRoom.setOnClickListener(v -> startActivity(new Intent(this, RoomSampleActivity.class)));
    }

    @Override
    public void initData() {

    }

    private void callSquareService() {
        SquareService squareService = Router.service(SquareService.class);
        if (squareService != null) squareService.getInfo(info -> logD("squareService", "squareService=" + info));
    }
}
