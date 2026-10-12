# module_player（播放）

播放业务的实现。暂时没有对外服务，所以没有 `module_player_api`；其它模块只通过路由打开播放页。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_base` | implementation | 基类、路由、网络等全部基础能力 |

## 主要内容

```
com.module.player
└── ui/PlayerActivity    播放页，路由 RoutePath.PLAYER_ACTIVITY，参数 RoutePath.PLAYER_KEY_VIDEO_ID
```

## 别人怎么用

```java
// 不需要依赖本模块
Router.build(RoutePath.PLAYER_ACTIVITY)
        .withString(RoutePath.PLAYER_KEY_VIDEO_ID, "123")
        .navigation(context);
```

页面里用 `@Autowired(name = RoutePath.PLAYER_KEY_VIDEO_ID)` 接收参数，`inits()` 里调用 `Router.inject(this)` 完成注入。

## 注意

- 资源名必须以 `py_` 开头（`resourcePrefix`）
- 以后要给别的模块提供能力（比如查播放记录），再建 `module_player_api`，参考 `module_square_api`
