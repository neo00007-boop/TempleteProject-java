# app（壳工程）

正式包。只做组装：Application、启动页、渠道、签名，业务代码都在 `module_xxx` 里。也放业务模块和 Room 的用法示例。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_base` | implementation | 自己的代码要用基类、路由、`LoginUtil` 等 |
| `module_square`、`module_player`、`module_login` | implementation | 把业务模块打进包 |

业务模块对 `lib_base` 用的是 `implementation`，不会往上传，所以壳工程要自己声明 `lib_base`。

## 主要内容

```
com.example.app
├── MainApp                          Application，继承 lib_base 的 App，各模块初始化自动完成
└── sample/
    ├── ModuleSampleActivity         启动页「业务模块示例」：路由跳转、带参跳转、跨模块服务、登录
    ├── RoomSampleActivity           Room 增删改查、搜索，从上面的示例页进入
    └── NoteAdapter
```

`VotePKActivity`、`VoteListActivity` 等投票相关页面是单独的页面示例，目前没有入口。

## 打包

- 渠道在 `productFlavors` 里（guanfang、xiaomi、huawei、anzhi 等），渠道名写入 `UMENG_CHANNEL_VALUE`
- 输出文件名：`xsm_渠道_版本号_v版本名.apk`
- release 开启混淆和资源压缩，规则在 `proguard-rules.pro`；各库自己的规则通过它们的 `consumer-rules.pro` 自动带入

```
./gradlew :app:assembleGuanfangDebug
./gradlew :app:assembleGuanfangRelease
```

## 新增业务模块后

`build.gradle` 加 `implementation project(':module_x')` 就行，模块的初始化和路由会自动注册，壳工程代码不用改。

## 注意

- 业务模块的用法示例放这里，不放 `demo`
- 代码里不要直接引用业务模块内部的类，跳页面用路由，调能力用 `_api` 里的服务接口
