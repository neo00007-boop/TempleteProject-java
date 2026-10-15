package com.lib.base.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lib.base.config.App;

import java.lang.reflect.Field;


/**
 * 键盘输入工具类
 * Created by Dave on 2017/1/13.
 */

public class KeyBoardUtil {
    /**
     * 输入类型管理器
     *
     * @return
     */
    public static InputMethodManager getInputManager() {
        return (InputMethodManager) App.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * 为给定的编辑器开启软键盘
     *
     * @param editText 给定的编辑器
     */
    public static void showSoftKeyboard(EditText editText) {
        try {
            editText.requestFocus();
            InputMethodManager inputManager = getInputManager();
            inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            release(inputManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeKeybord(EditText mEditText) {
        try {
            InputMethodManager inputManager = getInputManager();
            inputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            release(inputManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭系统的软键盘(无需传EditText）
     */
    public static void dismissSoftKeyboard(Activity activity) {
        try {
            View view = activity.getWindow().peekDecorView();
            if (view != null) {
                InputMethodManager imm = getInputManager();
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    release(imm);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param imm
     */
    private static void release(InputMethodManager imm) {
        //Android 9 起系统禁止反射这类非公开字段，反射只会抛异常
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return;
        }
        try {
            String[] arr = new String[]{"mLastSrvView"};
            for (String param : arr) {
                try {
                    Field field = imm.getClass().getDeclaredField(param);
                    field.setAccessible(true);
                    field.set(imm, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换软键盘的状态
     */
    public static void toggleSoftKeyboardState() {
        try {
            InputMethodManager inputManager = getInputManager();
            inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            release(inputManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
