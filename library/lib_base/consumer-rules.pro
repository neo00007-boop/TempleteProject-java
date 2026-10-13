# bean 包的数据类用 Gson 按字段名序列化和解析，字段不能被混淆
-keep class com.lib.base.bean.** { *; }
# 双缓存把 CacheBean 整个转成 JSON 存储
-keep class com.lib.base.util.DoubleCacheUtil$CacheBean { *; }
# ViewInjectUtil 反射读取 @ViewInject 注解给字段赋值
-keep @interface com.lib.base.util.inject.ViewInject
-keepclassmembers class * {
    @com.lib.base.util.inject.ViewInject <fields>;
}
