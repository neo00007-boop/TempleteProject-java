package com.module.square.ui;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.lib.router.Router;
import com.lib.router.RoutePath;
import com.lib.base.bean.User;
import com.lib.base.ui.activity.BaseActivity;
import com.module.square.databinding.SqActivityBinding;

import io.reactivex.rxjava3.disposables.Disposable;

@Route(path = RoutePath.SQUARE_ACTIVITY)
public class SquareActivity extends BaseActivity<SqActivityBinding> {
    public static final String TAG = "sssss";
    //    @Autowired
    public int age;
    //    @Autowired
    public String name;
    //    @Autowired
    public User user;
    private Disposable disposable;

    @Override
    public void inits() {
        setTitleStr("广场");
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {
        mViewBinding.btnPlay.setOnClickListener(v -> Router.build(RoutePath.PLAYER_ACTIVITY)
                .withString(RoutePath.PLAYER_KEY_VIDEO_ID, "123")
                .navigation(this));
    }

    @Override
    public void initData() {
    }

    @Override
    public SqActivityBinding viewBinding() {
        return SqActivityBinding.inflate(getLayoutInflater());
    }
}