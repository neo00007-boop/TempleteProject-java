package com.greendao.db.bean;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.greendao.db.util.GsonUtil;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

/**
 * PackageName  com.greendao.db.bean
 * ProjectName  TempleteProject-java
 * Date         2022/2/24.
 *
 * @author xwchen
 */
@Entity
public class CityBean {
    @Id(autoincrement = true)
    public Long _id;
    /**
     * name : 北京市
     * city : [{"name":"北京市","area":["东城区","西城区","海淀区","朝阳区","丰台区","石景山区","门头沟区","通州区","顺义区","房山区","大兴区","昌平区","怀柔区","平谷区","密云区","延庆区"]}]
     */

    public String name;
    @Convert(converter = Converter.class, columnType = String.class)
    public List<CityDataBean> city;

    @Generated(hash = 416108385)
    public CityBean(Long _id, String name, List<CityDataBean> city) {
        this._id = _id;
        this.name = name;
        this.city = city;
    }

    @Generated(hash = 273649691)
    public CityBean() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CityDataBean> getCity() {
        return this.city;
    }

    public void setCity(List<CityDataBean> city) {
        this.city = city;
    }

    public static class Converter implements PropertyConverter<List<CityDataBean>, String> {
        @Override
        public List<CityDataBean> convertToEntityProperty(String databaseValue) {
            return !TextUtils.isEmpty(databaseValue) ?
                    GsonUtil.getInstance().fromJson(databaseValue, new TypeToken<List<CityDataBean>>() {}.getType()) :
                    null;
        }

        @Override
        public String convertToDatabaseValue(List<CityDataBean> entityProperty) {
            return entityProperty != null ? GsonUtil.getInstance().toJson(entityProperty) : "";
        }
    }
}
