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
├── RetryInterceptor        GET/HEAD 遇到网络异常、408、429、5xx 时重试，最多 2 次
├── OkCookieJar             Cookie 管理（只存内存，进程重启后清空）
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

3. 返回体统一用 `ApiResult<T>`。`data` 是对象就写对象，是数组就写 `List`：

```java
@POST("/user/detail")
Single<ApiResult<UserBean>> detail(@Query("id") String id);

@POST("/user/list")
Single<ApiResult<List<UserBean>>> list(@Query("page") int page);
```

`code` 可以是数字或数字字符串，文案认 `msg`、`message`、`errorMsg`、`errMsg`。成功码默认 `0` 和 `200`，后台不是这两个就在启动时 `ApiResult.setSuccessCodes(...)`。

取出 data：

```java
api.detail(id)
        .compose(RxUtils.toSimpleSingle(this))
        .compose(ApiResults.unwrap())
        .subscribe(user -> { }, throwable -> {
            if (throwable instanceof ApiException) {
                int code = ((ApiException) throwable).getCode();
            }
        });
```

没有 data 的接口不要 unwrap，自己判断 `result.isCodeSuccess()`（`isSuccess()` 也只看 code）。`hasData()` 是 code 成功且 data 非空，空字符串、空 List 都算空。`hasListData()` 还要求 data 是非空 List。字符串响应可以用 `ApiResults.parse(gson, json, new TypeToken<ApiResult<UserBean>>(){}.getType())`。

Demo 样例：接口 `ApiResultSampleApi`，页面 `ApiResultActivity`。几段 JSON 由 `ApiResultSampleInterceptor` 本地返回，用来看对象、列表、无 data、空 data 和业务失败。

## 注意

- 使用前必须先 `RetrofitHelper.init`，否则 `getInstance()` 直接抛 `IllegalStateException`
- token 只加在自己的接口上（`baseUrl` 和 `host()` 登记的域名），第三方地址不会带
- 接口按业务放到各自模块，不要都堆在这里
- `ApiResult` 的字段规则只在本库 `consumer-rules.pro`。业务实体放进该模块已经 keep 的 `bean` 包，不要再为单个类加规则
