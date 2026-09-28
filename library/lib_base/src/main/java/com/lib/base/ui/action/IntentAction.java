package com.lib.base.ui.action;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;

import com.lib.base.bean.ShareData;
import com.lib.base.util.intent.IntentData;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 1.activity跳转+intent添加数据,让开发者不再处理intent和数据添加的过程;
 * 2.结合OverridePendingTransitionAction实现常见的转场动画.
 * PackageName  com.lib.base.ui.action
 * ProjectName  TempleteProject-java
 * Date         2022/1/28.
 *
 * @author xwchen
 */

public interface IntentAction extends OverridePendingTransitionAction {
    /*****************************************跳转,存数据******************************************************/
    /**
     * @param activity
     * @param aClass
     */
    default void error(Activity activity, Class<?> aClass) {
        if (activity == null || aClass == null) {
            throw new RuntimeException("activity or class error");
        }
    }

    /**
     * 关闭Activity,带默认返回动画
     *
     * @param activity
     */
    default void finishActivityWithAnim(@NonNull Activity activity) {
        activity.finish();
        finishAnim(activity);
    }

    default void startAnim(@NonNull Activity activity) {
        if (startAnimType() != ANIM_NO) {
            overridePendingTransitionStartNormal();
        }
    }

    default void finishAnim(@NonNull Activity activity) {
        overridePendingTransitionFinishNormal();
    }

    default void startAty(@NonNull Activity activity, @NonNull Class aClass) {
        error(activity, aClass);
        startIntent(activity, getIntent(activity, aClass));
    }

    /**
     * 元素共享
     *
     * @param activity
     * @param aClass
     * @param data
     */
    @SuppressLint("ObsoleteSdkInt")
    default void startAty(@NonNull Activity activity, @NonNull Class aClass, @NonNull ShareData... data) {
        if (data.length > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //判断Android版本, 大于等于5.0才支持元素共享
            Pair[] arr = new Pair[data.length];
            for (int i = 0; i < data.length; i++) {
                arr[i] = new Pair<>(data[i].getView(), data[i].getTransitionName());
            }
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity, arr).toBundle();
            activity.startActivity(getIntent(activity, aClass), bundle);
            //转场动画不作任何处理,不然元素共享动画无效
            //overridePendingTransitionStartNone();
        } else {
            //5.0以下,不支持元素共享
            startAty(activity, aClass);
        }
    }

    default void startAty(@NonNull Activity activity, @NonNull Class aClass, @NonNull IntentData... data) {
        error(activity, aClass);
        Intent intent = getIntent(activity, aClass);
        putdata(intent, data);
        startIntent(activity, intent);
    }

    default void startAtyForResult(@NonNull Activity activity, @NonNull Class aClass) {
        startAtyForResult(activity, aClass, 1000);
    }

    default void startAtyForResult(@NonNull Activity activity, @NonNull Class aClass, int requestCode) {
        error(activity, aClass);
        activity.startActivityForResult(getIntent(activity, aClass), requestCode);
        startAnim(activity);
    }

    default void startAtyForResult(@NonNull Activity activity, @NonNull Class aClass, @NonNull IntentData... data) {
        startAtyForResult(activity, aClass, 1000, data);
    }

    default void startAtyForResult(@NonNull Activity activity, @NonNull Class aClass, int requestCode, @NonNull IntentData... data) {
        error(activity, aClass);
        Intent intent = getIntent(activity, aClass);
        putdata(intent, data);
        activity.startActivityForResult(intent, requestCode);
        startAnim(activity);
    }

    default void putdata(Intent intent, @NonNull IntentData[] data) {
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
    default Intent getIntent(@NonNull Activity activity, @NonNull Class aClass) {
        return new Intent(activity, aClass);
    }

    default void startIntent(@NonNull Activity activity, Intent intent) {
        activity.startActivity(intent);
        startAnim(activity);
    }

    /*****************************************取出数据******************************************************/
    /**
     * getIntent
     *
     * @return
     */
    @Nullable
    Intent getIntents();

    /**
     * getIntExtra
     *
     * @param key
     * @return
     */
    default int getIntExtra(String key) {
        return getIntExtra(key, 0);
    }

    /**
     * getIntExtra
     *
     * @param key
     * @param defaults
     * @return
     */
    default int getIntExtra(String key, int defaults) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getIntExtra(key, defaults);
        }
        return defaults;
    }

    /**
     * getLongExtra
     *
     * @param key
     * @return
     */
    default long getLongExtra(String key) {
        return getLongExtra(key, 0);
    }

    /**
     * getLongExtra
     *
     * @param key
     * @param defaults
     * @return
     */
    default long getLongExtra(String key, long defaults) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getLongExtra(key, defaults);
        }
        return defaults;
    }

    /**
     * getFloatExtra
     *
     * @param key
     * @return
     */
    default float getFloatExtra(String key) {
        return getFloatExtra(key, 0);
    }

    /**
     * getFloatExtra
     *
     * @param key
     * @param defaults
     * @return
     */
    default float getFloatExtra(String key, float defaults) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getFloatExtra(key, defaults);
        }
        return defaults;
    }

    /**
     * getDoubleExtra
     *
     * @param key
     * @return
     */
    default double getDoubleExtra(String key) {
        return getDoubleExtra(key, 0);
    }

    /**
     * getFloatExtra
     *
     * @param key
     * @param defaults
     * @return
     */
    default double getDoubleExtra(String key, double defaults) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getDoubleExtra(key, defaults);
        }
        return defaults;
    }

    /**
     * getStringExtra
     *
     * @param key
     * @return
     */
    default String getStringExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getStringExtra(key);
        }
        return null;
    }

    /**
     * getIntArrayExtra
     *
     * @param key
     * @return
     */
    default int[] getIntArrayExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getIntArrayExtra(key);
        }
        return null;
    }

    /**
     * getDoubleArrayExtra
     *
     * @param key
     * @return
     */
    default double[] getDoubleArrayExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getDoubleArrayExtra(key);
        }
        return null;
    }

    /**
     * getLongArrayExtra
     *
     * @param key
     * @return
     */
    default long[] getLongArrayExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getLongArrayExtra(key);
        }
        return null;
    }

    /**
     * getFloatArrayExtra
     *
     * @param key
     * @return
     */
    default float[] getFloatArrayExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getFloatArrayExtra(key);
        }
        return null;
    }

    /**
     * getStringArrayExtra
     *
     * @param key
     * @return
     */
    default String[] getStringArrayExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getStringArrayExtra(key);
        }
        return null;
    }

    /**
     * getParcelableExtra
     *
     * @param key
     * @return
     */
    default <T extends Parcelable> T getParcelableExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getParcelableExtra(key);
        }
        return null;
    }

    /**
     * getParcelableArrayExtra
     *
     * @param key
     * @return
     */
    default Parcelable[] getParcelableArrayExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getParcelableArrayExtra(key);
        }
        return null;
    }

    /**
     * getParcelableArrayListExtra
     *
     * @param key
     * @return
     */
    default <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getParcelableArrayListExtra(key);
        }
        return null;
    }

    /**
     * getIntegerArrayListExtra
     *
     * @param key
     * @return
     */
    default ArrayList<Integer> getIntegerArrayListExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getIntegerArrayListExtra(key);
        }
        return null;
    }

    /**
     * getStringArrayListExtra
     *
     * @param key
     * @return
     */
    default ArrayList<String> getStringArrayListExtra(String key) {
        Intent intent = getIntents();
        if (intent != null) {
            return intent.getStringArrayListExtra(key);
        }
        return null;
    }
}
