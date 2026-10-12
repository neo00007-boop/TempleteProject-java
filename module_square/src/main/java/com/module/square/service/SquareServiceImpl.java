package com.module.square.service;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.lib.router.RoutePath;
import com.module.square.api.SquareService;
import com.module.square.api.Result;
import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;

/**
 * 可以跨进程通讯
 * PackageName  com.module.square
 * ProjectName  TempleteProject
 * @author      xwchen
 * Date         10/11/21.
 */
@Route(path = RoutePath.SQUARE_SERVICE)
public class SquareServiceImpl implements SquareService {
    public static final String TAG = "SquareService";

    @Override
    public String getInfo() {
        return "square";
    }

    @Override
    public void getInfo(@NonNull Result result) {
        result.callBack(getInfo());
    }

    @Override
    public void init(Context context) {
        DebugUtil.logD(TAG, "SquareServiceImpl init");
    }
}
