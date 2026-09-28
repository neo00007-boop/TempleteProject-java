package com.lib.base.util;

import android.app.Activity;
import android.content.Intent;

import com.lib.base.R;
import com.lib.base.ui.action.IntentAction;
import com.lib.base.ui.action.OverridePendingTransitionAction;
import com.lib.base.util.intent.IntentData;

import androidx.annotation.NonNull;


/**
 * 跳转意图工具类,activity和fragment中使用{@link IntentAction}和{@link OverridePendingTransitionAction}即可.
 * Created by Dave on 2016/12/30.
 */

public class IntentUtil {

    private static void error(Activity activity, Class aClass) {
        if (activity == null || aClass == null) {
            throw new RuntimeException("activity or class error");
        }
    }

    public static void finishActivityWithAnim(@NonNull Activity activity) {
        activity.finish();
        finishAnim(activity);
    }

    public static void startAnim(@NonNull Activity activity) {
        activity.overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }

    public static void finishAnim(@NonNull Activity activity) {
        activity.overridePendingTransition(R.anim.trans_pre_in_back, R.anim.trans_pre_out_back);
    }

    public static void startActivity(Activity activity, Class aClass) {
        error(activity, aClass);
        startIntent(activity, getIntent(activity, aClass));
    }

    public static void startActivity(Activity activity, Class aClass, @NonNull IntentData... data) {
        error(activity, aClass);
        Intent intent = getIntent(activity, aClass);
        putdata(intent, data);
        startIntent(activity, intent);
    }

    public static void startActivityForResult(Activity activity, Class aClass) {
        error(activity, aClass);
        Intent intent = getIntent(activity, aClass);
        activity.startActivityForResult(intent, 1000);
        activity.overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }

    public static void startActivityForResult(Activity activity, Class aClass, @NonNull IntentData... data) {
        error(activity, aClass);
        Intent intent = getIntent(activity, aClass);
        putdata(intent, data);
        activity.startActivityForResult(intent, 1000);
        activity.overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }

    private static void putdata(Intent intent, @NonNull IntentData[] data) {
        if (intent == null) {
            return;
        }
        for (IntentData datum : data) {
            if (datum != null) {
                datum.write(intent);
            }
        }
    }

    @NonNull
    private static Intent getIntent(Activity activity, Class aClass) {
        return new Intent(activity, aClass);
    }

    public static void startIntent(@NonNull Activity activity, Intent intent) {
        activity.startActivity(intent);
        startAnim(activity);
    }

    /**
     * 返回桌面
     *
     * @param activity
     */
    public static void toHome(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(intent);
    }

}
