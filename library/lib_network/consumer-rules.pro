# 统一响应体的字段要留给 Gson 和业务读取
-keepclassmembers class com.lib.network.ApiResult {
    <fields>;
}
