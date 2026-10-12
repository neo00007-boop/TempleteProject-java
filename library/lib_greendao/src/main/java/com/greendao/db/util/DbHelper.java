package com.greendao.db.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.greendao.db.helper.DaoMaster;
import com.greendao.db.helper.DemoBeanDao;

import org.greenrobot.greendao.database.Database;

/**
 * PackageName  com.hycg.ee.dbHelper
 * ProjectName  HYCGCompanyProject
 * @author      xwchen
 * Date         2019/5/17.
 */
public class DbHelper extends DaoMaster.DevOpenHelper {
    public static final String DB_NAME = "translate-db";

    public DbHelper(Context context) {
        this(context, null);
    }

    public DbHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, null);
    }

    /**
     * 正式迭代后不要调用父类方法，不然直接清掉所有表数据
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        super.onUpgrade(db, oldVersion, newVersion);
        DBUpdateHelper.migrate(db, DemoBeanDao.class);
    }
}
