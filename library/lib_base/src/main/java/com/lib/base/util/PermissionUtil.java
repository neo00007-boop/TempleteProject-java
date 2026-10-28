package com.lib.base.util;

import android.app.Activity;
import android.content.Context;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * PackageName  com.lib.base.util
 * ProjectName  TempleteProject-java
 * @author      xwchen
 * Date         2021/12/26.
 */

public class PermissionUtil {
    public static final int REQUEST_CODE = XXPermissions.REQUEST_CODE;

    /**
     * 请求权限
     *
     * @param context
     * @param callback
     * @param permissions
     */
    public static void requestPermission(Context context, OnPermissionCallback callback, String... permissions) {
        XXPermissions
                .with(context)
                .permission(permissions)
                .unchecked()
                .request(callback);
    }

    /**
     * 检查一个或多个权限状态是否已允许
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean isGranted(Context context, String... permissions) {
        return XXPermissions.isGranted(context, permissions);
    }

    /**
     * 获取指定一个或多个权限中未允许的权限
     *
     * @param context
     * @param permissions
     * @return
     */
    public static List<String> getDenied(Context context, String... permissions) {
        return XXPermissions.getDenied(context, permissions);
    }

    /**
     * 判断一个或多个权限是否被永久禁止
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean isPermanentDenied(Activity activity, String... permissions) {
        return XXPermissions.isPermanentDenied(activity, permissions);
    }

    /**
     * 跳转到应用详情页(权限页)
     *
     * @param o
     * @param permissions
     * @return
     */
    public static void startPermissionActivity(Object o, List<String> permissions) {
        if (o instanceof Activity) {
            XXPermissions.startPermissionActivity((Activity) o, permissions);
        } else if (o instanceof Fragment) {
            // XXPermissions 18.x 无 androidx Fragment 重载，走 Context
            XXPermissions.startPermissionActivity(((Fragment) o).requireContext(), permissions);
        } else if (o instanceof Context) {
            XXPermissions.startPermissionActivity((Context) o, permissions);
        } else {
            throw new RuntimeException("权限参数不合法");
        }
    }


}
