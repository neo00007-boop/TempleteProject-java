# lib_network（网络）

Retrofit + OkHttp + RxJava3 的封装，只管请求本身。域名、公共请求头、token、登录失效处理都由上层通过 `HttpConfig` 传进来，本库不依赖任何业务和界面代码。

## 依赖

retrofit、adapter-rxjava3、converter-gson、okhttp 用 `api` 暴露，上层可以直接用；logging-interceptor、okio 只在内部用。

## 主要内容

```
com.lib.network
├── HttpConfig              网络配置，Builder 构建
├── RetrofitHelper          init(HttpConfig) 初始化，getInstance().getRetrofit() 取 Retrofit
├── Hosts                   多域名切换的 key 和请求头
├── NetworkInterceptor      加公共请求头、token，识别登录失效
├── UrlManagerInterceptor   按 Hosts 请求头切换域名
├── RetryInterceptor        失败重试
├── OkCookieJar             Cookie 持久化
└── HttpLog、HttpEventListener   请求日志
```

## 用法

1. 初始化，已在 `lib_base` 的 `App.initNetwork()` 里做好：

```java
RetrofitHelper.init(new HttpConfig.Builder(baseUrl)
        .host(Hosts.KEY2, otherBaseUrl)                        // 第二个域名
        .header("client_type", "android")                      // 公共请求头
        .tokenProvider(() -> SPUtil.getString(AppConfig.USER_TOKEN))
        .onTokenExpired(LoginUtil::toLogin)                    // 登录失效回调
        .gson(GsonUtil.getInstance())
        .debug(DebugUtil.isDebug)
        .build());
```

2. 写接口，参考 `module_login` 的 `LoginApi` / `LoginHttp`：

```java
public interface XxxApi {
    @Headers({Hosts.HOST2})            // 走第二个域名，不写就是 baseUrl
    @POST("/path")
    Single<XxxBean> request(@Query("id") String id);
}

XxxApi api = RetrofitHelper.getInstance().getRetrofit().create(XxxApi.class);
```

## 注意

- 使用前必须先 `RetrofitHelper.init`，否则 `getInstance()` 直接抛 `IllegalStateException`
- token 只加在自己的接口上（`baseUrl` 和 `host()` 登记的域名），第三方地址不会带
- 接口按业务放到各自模块，不要都堆在这里
- 接口返回的实体要在所在模块的 `consumer-rules.pro` 里保留字段，否则发布包里 Gson 解析不出数据
