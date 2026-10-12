# module_square_api（广场对外接口）

广场模块对外暴露的服务接口和实体。其它模块要调用广场的能力，只依赖这里，不依赖 `module_square`。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_router` | api | 服务接口要继承 ARouter 的 `IProvider` |

只依赖路由层，不依赖 `lib_base`，保持轻量。

## 主要内容

```
com.module.square.api
├── SquareService    广场服务接口：getInfo()、getInfo(Result)
└── Result           异步结果回调
```

## 用法

```java
// build.gradle
implementation project(':module_square_api')

// 代码
SquareService service = Router.service(SquareService.class);
if (service != null) {   // 广场模块没打进包时返回 null
    String info = service.getInfo();
}
```

## 注意

- 只放接口和跨模块要用的实体，不放实现、页面和资源
- 路由路径不放这里，统一在 `lib_router` 的 `RoutePath`
- 已发布的接口方法不要随便改签名，别的模块在用
