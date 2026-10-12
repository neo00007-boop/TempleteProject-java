package com.greendao.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * PackageName  com.lib.db
 * ProjectName  TempleteProject
 * @author      xwchen
 * Date         10/10/21.
 */
@Entity
public class DemoBean {
    @Id(autoincrement = true)
    public Long _id;
    public String name;
    public int age;
    public int age1;
    public int age2;
    public int age3;
    @Generated(hash = 205963509)
    public DemoBean(Long _id, String name, int age, int age1, int age2, int age3) {
        this._id = _id;
        this.name = name;
        this.age = age;
        this.age1 = age1;
        this.age2 = age2;
        this.age3 = age3;
    }
    @Generated(hash = 2085635340)
    public DemoBean() {
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
    public int getAge() {
        return this.age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public int getAge1() {
        return this.age1;
    }
    public void setAge1(int age1) {
        this.age1 = age1;
    }
    public int getAge2() {
        return this.age2;
    }
    public void setAge2(int age2) {
        this.age2 = age2;
    }
    public int getAge3() {
        return this.age3;
    }
    public void setAge3(int age3) {
        this.age3 = age3;
    }

}
