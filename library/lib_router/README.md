# lib_router（路由）

封装 ARouter，提供全工程唯一的路由表。业务代码只用这里的 `Router`，不直接调 ARouter，以后换路由框架只改这一层。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_startup` | api | 启动时初始化 ARouter |
| `arouter-api` | api | 路由框架 |

## 主要内容

```
com.lib.router
├── Router              统一入口：build / open / service / inject
├── RoutePath           全部路由路径和参数名，按模块分段
├── RouteExtras         写在 @Route(extras = ...) 上的标记位，目前有 NEED_LOGIN
└── RouterInitializer   启动时初始化 ARouter，debug 包打开日志
```

## 用法

```java
// 打开页面
Router.open(context, RoutePath.SQUARE_ACTIVITY);

// 带参数、带动画
Router.build(RoutePath.PLAYER_ACTIVITY)
        .withString(RoutePath.PLAYER_KEY_VIDEO_ID, "123")
        .navigation(context);

// 取跨模块服务（对方模块没打进包时返回 null）
SquareService service = Router.service(SquareService.class);

// 页面里接收 @Autowired 参数
Router.inject(this);
```

## 新增路由

在 `RoutePath` 里本模块的分段下加常量：

```java
// ==================== 模块名 module_x ====================
private static final String X = "/module_x";
public static final String X_ACTIVITY = X + "/XActivity";
public static final String X_KEY_ID = "id";
```

## 注意

- 路径格式 `/模块名/页面`，第一段是 ARouter 的分组，同一个分组只能出现在一个模块里
- 有 `@Route` / `@Interceptor` / `@Autowired` 的模块要配置 arouter-compiler 和 `AROUTER_MODULE_NAME`，写法见 `module_player/build.gradle`
- `consumer-rules.pro` 已带 ARouter 的混淆规则
