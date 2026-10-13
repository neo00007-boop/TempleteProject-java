# bean 包的实体用 Gson 按字段名解析，也是 GreenDao 的表结构，字段不能被混淆
-keep class com.greendao.db.bean.** { *; }
# DaoConfig 用「Dao 类名 + $Properties」反射取列定义并读取静态 TABLENAME，Dao 类名不能被混淆
-keep class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties { *; }
