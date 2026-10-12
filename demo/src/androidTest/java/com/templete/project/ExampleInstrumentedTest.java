package com.templete.project;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.greendao.db.util.GsonUtil;
import com.lib.base.bean.BtnBean;
import com.lib.base.config.App;
import com.lib.base.util.DebugUtil;
import com.lib.base.util.JsonDataUtil;
import com.lib.base.util.txt.label.TxtUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@SuppressLint("CheckResult")
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static final String TAG = "TEST_LOG";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.templete.project", appContext.getPackageName());
    }

    @Test
    public void json() {
        try {
            String json = JsonDataUtil.getJson(App.getContext(), "app.json");
//            JBean jBean = GsonUtil.getNoCrashInstance().fromJson(json, JBean.class);
//            DebugUtil.logD(TAG, GsonUtil.getNoCrashInstance().toJson(jBean));
            List<BtnBean> btnBeans = GsonUtil.fromJsonList("", BtnBean.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void numberFormat() {
        String twoPoint = TxtUtil.getTwoPoint("1.23435");
        String point = TxtUtil.getPointNo0(1.001);
        String point1 = TxtUtil.getPointNo0("1.1001");
        DebugUtil.logD(TAG, "twoPoint=" + twoPoint + ",point=" + point + ",point1=" + point1);
    }
}