package com.module.square.api;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * module_square 对外提供的能力。其它模块用 {@code Router.service(SquareService.class)} 获取。
 *
 * @author xwchen
 * Date         10/11/21.
 */
public interface SquareService extends IProvider {
    String getInfo();

    void getInfo(Result result);
}
