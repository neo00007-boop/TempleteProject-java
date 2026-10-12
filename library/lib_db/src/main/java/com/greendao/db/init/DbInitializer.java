package com.greendao.db.init;

import android.app.Application;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.greendao.db.R;
import com.greendao.db.bean.CityBean;
import com.greendao.db.helper.CityBeanDao;
import com.greendao.db.util.GsonUtil;
import com.greendao.db.util.LocalRepository;
import com.lib.common.init.ModuleInitializer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * 打开数据库，并在后台导入一次省市区数据。
 */
public class DbInitializer implements ModuleInitializer {
    private static final String TAG = "DbInitializer";

    @Override
    public int priority() {
        return PRIORITY_DATA;
    }

    @Override
    public void onCreate(Application application) {
        LocalRepository.getInstance().init(application);
        new Thread(() -> importCity(application), "db-city-import").start();
    }

    private void importCity(Application application) {
        try {
            CityBeanDao cityBeanDao = LocalRepository.getInstance().getCityBeanDao();
            if (cityBeanDao.count() > 0) {
                Log.d(TAG, "city num is " + cityBeanDao.count());
                return;
            }
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try (InputStream inputStream = application.getResources().openRawResource(R.raw.province)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, length);
                }
            }
            List<CityBean> list = GsonUtil.getInstance().fromJson(outStream.toString("UTF-8"), new TypeToken<List<CityBean>>() {
            }.getType());
            if (list == null || list.isEmpty()) {
                Log.e(TAG, "city data error");
                return;
            }
            cityBeanDao.insertInTx(list);
            Log.d(TAG, "city num is " + list.size());
        } catch (Exception e) {
            Log.e(TAG, "city data error", e);
        }
    }
}
