# module_login（登录）

登录页、登录接口，以及「需登录页面」的路由拦截器。其它模块不依赖本模块，跳登录页和判断登录态都走 `lib_base` 的 `LoginUtil`。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_base` | implementation | 基类、路由、网络、`LoginUtil` |

## 主要内容

```
com.module.login
├── ui/LoginActivity            登录页，路由 RoutePath.LOGIN_ACTIVITY，参数 RoutePath.LOGIN_KEY_DATA
├── mvvm/LoginViewModel
├── http/LoginApi、LoginHttp    登录接口（Retrofit），LoginHttp 是单例
├── bean/LoginBean              登录返回数据
└── service/LoginInterceptor    路由拦截器：目标页带 NEED_LOGIN 且未登录时，拦下并打开登录页
```

## 别人怎么用

```java
LoginUtil.toLogin();       // 打开登录页，多次调用只会开一个
LoginUtil.isLogin();       // 是否已登录

// 某个页面要求先登录：在注解上加标记，跳转代码不用改
@Route(path = RoutePath.XXX, extras = RouteExtras.NEED_LOGIN)
```

登录失效（接口返回 token 过期）时，网络层会回调 `LoginUtil.toLogin()`，在 `App.initNetwork()` 里配置。

## 注意

- 资源名必须以 `lg_` 开头（`resourcePrefix`）
- `consumer-rules.pro` 保留了 `bean` 包：Gson 按字段名解析，混淆后字段名变了会解析不出数据。新增接口实体放 `bean/` 即可
- 登录、用户相关的接口放这里，不要放到先用到它的模块里
- 拦截器只对走路由的跳转生效，直接 `startActivity` 打开的页面不会被拦
