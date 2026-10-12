# module_square（广场）

广场业务的实现。对外提供的能力放在 `module_square_api`，其它模块不能直接依赖本模块。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_base` | implementation | 基类、路由、网络等全部基础能力 |
| `module_square_api` | api | 本模块对外的服务接口，依赖本模块的 app 也能拿到 |

## 主要内容

```
com.module.square
├── ui/SquareActivity            广场页，路由 RoutePath.SQUARE_ACTIVITY，演示带参跳转到播放页
└── service/SquareServiceImpl    SquareService 的实现，路由 RoutePath.SQUARE_SERVICE
```

## 别人怎么用

```java
// 打开广场页：不需要依赖本模块
Router.open(context, RoutePath.SQUARE_ACTIVITY);

// 调广场的服务：只需要依赖 module_square_api
SquareService service = Router.service(SquareService.class);
if (service != null) {
    service.getInfo(info -> { });
}
```

## 注意

- 资源名必须以 `sq_` 开头（`resourcePrefix`）
- 新增页面：路由路径写到 `lib_router` 的 `RoutePath` 广场分段里，不要在代码里写字符串
- 新增对外能力：接口加到 `module_square_api`，实现放 `service/`
