# lib_startup（模块启动初始化）

最底层的库，只负责一件事：让各个库在 App 启动时自动完成初始化，壳工程不用逐个调用。不放通用工具类。

## 依赖

无（只依赖 Android SDK）。

## 主要内容

```
com.lib.startup
├── ModuleInitializer     初始化接口：priority() 决定顺序，onCreate(Application) 里做初始化
└── ModuleInitializers    用 ServiceLoader 收集所有实现，按 priority 从小到大执行
```

`App.onCreate()` 里调用一次 `ModuleInitializers.init(this)`。

目前登记的初始化类：

| 类 | 所在库 | 优先级 | 做什么 |
| --- | --- | --- | --- |
| `DbInitializer` | lib_greendao | `PRIORITY_DATA`（-200） | 打开 GreenDao 数据库，后台导入省市区数据 |
| `RouterInitializer` | lib_router | `PRIORITY_ROUTER`（-100） | 初始化 ARouter |

## 新增一个初始化

1. 实现接口：

```java
public class XxxInitializer implements ModuleInitializer {
    @Override
    public void onCreate(Application application) {
        // 主线程执行，耗时操作自己切线程
    }
}
```

2. 在本模块建文件 `src/main/resources/META-INF/services/com.lib.startup.ModuleInitializer`，每行写一个实现类全名：

```
com.xxx.XxxInitializer
```

## 注意

- 登记文件的文件名就是接口全名，接口改包名时这个文件必须跟着改名，否则编译能过、运行时一个都找不到
- 用到路由的初始化，优先级不能小于 `PRIORITY_ROUTER`
- 某个初始化抛异常只会打日志，不会让 App 启动失败
- `consumer-rules.pro` 保留了实现类的无参构造，混淆后 ServiceLoader 仍能反射创建
