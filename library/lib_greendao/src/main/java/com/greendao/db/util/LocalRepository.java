package com.greendao.db.util;


import android.content.Context;

import com.greendao.db.helper.CityBeanDao;
import com.greendao.db.helper.DaoMaster;
import com.greendao.db.helper.DaoSession;
import com.greendao.db.helper.DemoBeanDao;


/**
 * 处理所有数据库操作
 * ProjectName  XSCat
 * PackageName  com.bigheadhorse.xscat.db
 * @author      xwchen
 * Date         2021/6/17.
 *
 * @author xwchen
 */

public class LocalRepository {
    private volatile static LocalRepository sInstance;
    private DaoSession daoSession;
    private DemoBeanDao demoBeanDao;
    private CityBeanDao cityBeanDao;

    private LocalRepository() {
    }

    public static LocalRepository getInstance() {
        if (sInstance == null) {
            synchronized (LocalRepository.class) {
                if (sInstance == null) {
                    sInstance = new LocalRepository();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        daoSession = new DaoMaster(new DbHelper(context).getWritableDb()).newSession();
        demoBeanDao = daoSession.getDemoBeanDao();
        cityBeanDao = daoSession.getCityBeanDao();
    }

    public CityBeanDao getCityBeanDao() {
        return cityBeanDao;
    }

    public DemoBeanDao getDemoBeanDao() {
        return demoBeanDao;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    /*********************************************搜索历史记录start*********************************************/

    /*public SearchHistoryBeanDao getSearchHistoryBeanDao() {
        return searchHistoryBeanDao;
    }

    public void addOrUpdateRecord(String content) {
        try {
            List<SearchHistoryBean> list = getSearchHistoryBeanDao()
                    .queryBuilder()
                    .where(SearchHistoryBeanDao.Properties.Content.eq(content))
                    .list();
            long searchTime = System.currentTimeMillis();
            if (list != null && list.size() > 0) {
                SearchHistoryBean searchHistoryBean = list.get(0);
                searchHistoryBean.searchTime = searchTime;
                searchHistoryBean.count = searchHistoryBean.count + 1;
                getSearchHistoryBeanDao().update(searchHistoryBean);
            } else {
                getSearchHistoryBeanDao().insert(new SearchHistoryBean(content, searchTime));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SearchHistoryBean> getHistoryWithLimit(int limit) {
        try {
            return getSearchHistoryBeanDao()
                    .queryBuilder()
                    .orderDesc(SearchHistoryBeanDao.Properties.SearchTime)
                    .limit(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/
    /*********************************************搜索历史记录end*********************************************/
}
