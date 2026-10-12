# 工程结构设计

## 1. 目标

- 业务模块固定为 library，不再切换「单独 App / 模块」，由壳工程统一打包
- 业务模块之间只依赖对方的 `xxx_api`，不依赖实现
- 基础能力按职责拆库，自下而上单向依赖
- 模块初始化靠注册（ServiceLoader），壳工程不写死各模块的初始化代码
- 每个模块的 `build.gradle` 用 Android Studio 默认写法、写全配置，版本号统一从根目录 `config.gradle` 读取

## 2. 目录树

```
TempleteProject
├── config.gradle                版本号（SDK、第三方库），各模块 build.gradle 读取
├── app/                         壳工程（正式包），含业务模块用法示例
├── demo/                        基础库示例，不依赖业务模块代码
├── module_square_api/           广场对外服务接口
├── module_square/               广场实现
├── module_player/               播放实现（暂无对外服务，不建 api）
├── module_login/                登录实现（暂无对外服务，不建 api）
└── library/
    ├── lib_common/              最底层：模块初始化接口
    ├── lib_router/              路由封装（ARouter）、全局路由表 RoutePath
    ├── lib_network/             网络（Retrofit / OkHttp）
    ├── lib_greendao/            数据库（GreenDao）
    ├── lib_room/                数据库（Room），含版本迁移和表结构导出
    ├── lib_res/                 公共资源
    ├── lib_view/                通用控件，含 Tab 控件（源自 FlycoTabLayout）
    └── lib_base/                UI 基础：Base 类、工具、App
```

## 3. 分层

从上到下分六层，上层依赖下层：

```
壳工程      app    demo
              ▼
业务实现    module_square    module_player    module_login
              ▼
业务接口    module_square_api
              ▼
基础 UI     lib_base
              ▼
基础能力    lib_router    lib_network    lib_greendao    lib_room    lib_view
              ▼
最底层      lib_common    lib_res
```

每个模块具体依赖谁：

| 模块 | 直接依赖 |
|------|----------|
| `app` | `lib_base`、`module_square`、`module_player`、`module_login` |
| `demo` | `lib_base`；`module_login` 只在运行时打包（`runtimeOnly`） |
| `module_square` | `lib_base`、`module_square_api` |
| `module_player` | `lib_base` |
| `module_login` | `lib_base` |
| `module_square_api` | `lib_router` |
| `lib_base` | `lib_router`、`lib_network`、`lib_greendao`、`lib_room`、`lib_view` |
| `lib_router` | `lib_common` |
| `lib_greendao` | `lib_common` |
| `lib_view` | `lib_res` |
| `lib_network`、`lib_room`、`lib_common`、`lib_res` | 无 |

业务接口层是可选的：只有要给别的模块提供能力的业务模块才建 `_api`，目前只有广场有，所以播放和登录直接依赖 `lib_base`。

规则：

- 只能向下依赖，不能向上，不能同层互相依赖实现
- 业务模块之间跳页面：用 `RoutePath` 里的路由，不需要依赖对方任何模块
- 业务模块之间调能力：依赖对方 `xxx_api`，通过服务接口调用
- `lib_*` 不知道任何业务模块的存在
- `demo` 只演示基础库，代码里不引用业务模块；业务模块的用法示例放 `app`

## 4. 依赖关系

所有依赖都写在各模块自己的 `build.gradle` 的 `dependencies {}` 里，打开文件就能看到。

### 4.1 每个模块的依赖一览

| 模块 | build.gradle 里写的项目依赖 | 最终能直接用到的项目模块 |
| --- | --- | --- |
| app | `implementation` lib_base、module_square、module_player、module_login | 下面所有模块 |
| demo | `implementation` lib_base；`runtimeOnly` module_login | lib_base 及其带出的全部基础库；登录页只在运行时存在，代码里不能引用 |
| module_square | `implementation` lib_base；`api` module_square_api | module_square_api、lib_base 及其带出的全部基础库 |
| module_player | `implementation` lib_base | lib_base 及其带出的全部基础库 |
| module_login | `implementation` lib_base | lib_base 及其带出的全部基础库 |
| module_square_api | `api` lib_router | lib_router、lib_common |
| lib_base | `api` lib_router、lib_network、lib_greendao、lib_room、lib_view | lib_router、lib_common、lib_network、lib_greendao、lib_room、lib_view、lib_res |
| lib_view | `api` lib_res | lib_res |
| lib_greendao | `api` lib_common | lib_common |
| lib_router | `api` lib_common | lib_common |
| lib_network、lib_room、lib_res、lib_common | 无 | 无（只依赖第三方库） |

### 4.2 展开后的完整依赖树

以 `module_login` 为例，它编译时实际拿到的项目模块（`./gradlew :module_login:dependencies --configuration debugCompileClasspath` 的结果）：

```
module_login
└── lib_base                      ← module_login 写 implementation
    ├── lib_router                ← lib_base 手写 api
    │   └── lib_common            ← lib_router 手写 api
    ├── lib_network               ← lib_base 手写 api
    ├── lib_greendao              ← lib_base 手写 api
    │   └── lib_common
    ├── lib_room                  ← lib_base 手写 api
    └── lib_view                  ← lib_base 手写 api
        └── lib_res               ← lib_view 手写 api
```

module_player 完全一样；module_square 在此基础上多一条：

```
module_square
├── module_square_api             ← module_square 写 api
│   └── lib_router                ← module_square_api 写 api
│       └── lib_common
└── lib_base（同上）
```

壳工程：

```
app
├── lib_base（同上）
├── module_square
├── module_player
└── module_login

demo
├── lib_base（同上）
└── module_login                  runtimeOnly：打进安装包，但 demo 代码里不能引用
```

### 4.3 api、implementation、runtimeOnly 的区别

| 写法 | 本模块代码能用 | 依赖本模块的上层能用 | 打进安装包 |
| --- | --- | --- | --- |
| `api` | 能 | 能（往上传递） | 是 |
| `implementation` | 能 | 不能 | 是 |
| `runtimeOnly` | 不能 | 不能 | 是 |

- `lib_base` 用 `api` 依赖各基础库，所以业务模块只依赖 `lib_base` 就能用路由、网络、数据库、控件、公共资源
- 业务模块用 `implementation` 依赖 `lib_base`，不会把基础库再往外暴露（壳工程自己也依赖了 `lib_base`）
- `lib_res` 由 `lib_view` 用 `api` 带出，`lib_base` 不再重复声明；如果以后 `lib_view` 改成 `implementation lib_res`，要在 `lib_base` 里补回 `api project(':library:lib_res')`

### 4.4 约定

- 业务模块只依赖 `lib_base` 一个基础库，不单独依赖 `lib_router`、`lib_network` 等
- 新增公共库：让 `lib_base` 用 `api` 依赖它，所有业务模块自动拿到
- 业务模块需要另一个模块的能力：手写 `api project(':module_xxx_api')`，不能依赖 `module_xxx`
- `_api` 模块只依赖 `lib_router`，保持轻量，不依赖 `lib_base`
- 不确定某个模块到底依赖了什么，用 4.2 里的命令查，以命令结果为准

## 5. 各模块职责

### 5.1 build.gradle 写法

每个模块都是 Android Studio 默认的写法，SDK 版本、第三方库版本用 `def config = rootProject.ext` 从 `config.gradle` 读取。不同类型模块的区别：

| 模块类型 | 插件 | 比基础写法多的配置 |
| --- | --- | --- |
| 壳工程 app | `com.android.application`、`kotlin-android` | applicationId、MultiDex、ARouter、ViewBinding、签名、渠道 |
| 业务模块 module_x | `com.android.library` | resourcePrefix、ARouter、ViewBinding、`implementation lib_base` |
| 业务接口 module_x_api | `com.android.library` | `api lib_router` |
| 基础库 lib_* | `com.android.library` | consumer-rules（本库的混淆规则随依赖带给 app） |

ARouter 配置分两部分，有 `@Route`、`@Interceptor`、`@Autowired` 的模块都要写：

```groovy
defaultConfig {
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = [AROUTER_MODULE_NAME: project.getName()]
        }
    }
}
dependencies {
    annotationProcessor "com.alibaba:arouter-compiler:${config.dependencies.route_version}"
}
```

### 5.2 业务模块：api / 实现拆分

```
module_login                           实现，其它模块看不到
└── com.module.login
    ├── ui/LoginActivity
    ├── mvvm/LoginViewModel
    ├── http/LoginApi、LoginHttp       登录接口
    ├── bean/LoginBean                 登录返回数据
    └── service/LoginInterceptor       需登录页面的路由拦截

其它模块用登录：跳登录页用 RoutePath.LOGIN_ACTIVITY 或 LoginUtil.toLogin()，
判断登录态用 LoginUtil.isLogin()，都不需要依赖 module_login
```

module_square（广场）、module_player（播放）：

```
module_square_api     SquareService、Result
module_square         ui/SquareActivity、service/SquareServiceImpl
module_player         ui/PlayerActivity
```

约定：

- 路由路径和参数名放 `lib_router` 的 `RoutePath`；服务接口、跨模块要用的实体放 `_api`；页面、ViewModel、接口请求放实现
- 接口按业务归属放：登录、用户相关接口放登录模块，广场接口放广场模块，不要因为谁先用到就放谁那里
- 资源加前缀：module_square 用 `square_`，module_player 用 `player_`，module_login 用 `login_`
- 实现模块不写 Application、不写 launcher 图标和主题，统一用壳工程的

### 5.3 基础库

```
lib_common       ModuleInitializer、ModuleInitializers
lib_router       Router（build / open / service / inject）
                 RoutePath（全部路由路径和参数名，按模块分段）
                 RouteExtras（NEED_LOGIN）
                 RouterInitializer
lib_network      HttpConfig（Builder 注入 host、header、token、登录失效回调）
                 Hosts、RetrofitHelper、各 Interceptor、OkCookieJar
lib_greendao     GreenDao 实体与 Dao、LocalRepository
                 DbInitializer（建库、导入城市数据）
lib_room         AppDatabase（全局唯一的库，VERSION 为当前版本）
                 entity/Note、dao/NoteDao（示例表，增删改查 + LiveData 查询）
                 migration/Migrations（版本迁移，改表结构的步骤写在类注释里）
                 repository/NoteRepository（写操作切子线程，结果回调主线程）
                 schemas/（每个版本的表结构 json，要提交），androidTest/MigrationTest
lib_res          colors、dimens（dp_/sp_）、strings、anim、公共图片
lib_view         通用控件（包名 com.hjq.shape）
                 Tab 控件（包名保留 com.flyco.tablayout，属性在 tablayout_attrs.xml）
lib_base         BaseActivity / Fragment / Dialog、Pop、MVP / MVVM
                 App、AppConfig、SPUtil、LoginUtil 等工具
```

## 6. 初始化流程

各模块在 `META-INF/services/com.lib.common.init.ModuleInitializer` 里登记实现类，启动时 `ModuleInitializers` 用 ServiceLoader 收集，按优先级从小到大执行。

```
App.onCreate
└── init()
    ├── RxJava 全局错误处理
    ├── Toast
    ├── initNetwork()               HttpConfig → RetrofitHelper.init
    ├── ModuleInitializers.init()
    │   ├── DbInitializer           PRIORITY_DATA    -200
    │   └── RouterInitializer       PRIORITY_ROUTER  -100
    └── Gson 异常回调、生命周期、刷新头、ViewModel、网络监听
```

新增模块要初始化：实现 `ModuleInitializer`，在本模块 `META-INF/services` 登记即可，壳工程不用改。

## 7. 路由与跨模块调用

```
页面跳转
任意模块 ──Router.open(context, RoutePath.LOGIN_ACTIVITY)──> ARouter ──> LoginActivity

服务调用
app ──Router.service(SquareService.class)──> SquareServiceImpl

示例见 app 的 sample/ModuleSampleActivity

需登录页面
@Route(path = ..., extras = RouteExtras.NEED_LOGIN)
    └── LoginInterceptor：未登录 → 跳登录页，已登录 → 放行

登录失效
NetworkInterceptor ──onTokenExpired──> LoginUtil.toLogin ──RoutePath.LOGIN_ACTIVITY──> LoginActivity
（登录页打开期间重复调用 toLogin 不会再开第二个）
```

路由路径以 `/模块名/页面` 命名，统一定义在 `lib_router` 的 `RoutePath` 里，常量名以模块名开头、按模块分段注释，不在代码里写字符串。

## 8. 资源

```
lib_res          公共颜色、尺寸、文字、动画、图片
lib_base         少量只给 base 控件用的 selector / drawable
module_x         本模块资源，带 resourcePrefix 前缀
app / demo       launcher 图标、主题
```

- 位图统一 WebP（`cwebp` 默认参数），点 9 图保留 PNG
- 未开启 nonTransitiveRClass，下层资源在上层仍可直接用 `R.xxx` 引用

## 9. 混淆

- `lib_common`：保留 `ModuleInitializer` 实现类的无参构造（ServiceLoader 反射创建）
- `lib_router`：ARouter 生成类和 IProvider 实现的 keep 规则
- `module_login`：保留 `bean` 包，Gson 按字段名解析接口数据
- 各库通过 `consumer-rules.pro` 随依赖带到壳工程，壳工程不用重复写

## 10. 新增业务模块步骤

1. 在 `RoutePath` 加一段 `// ==== 模块名 module_x ====`，写本模块的路由路径和参数名
2. 建 `module_x`：复制 `module_player/build.gradle`，改 `namespace` 和 `resourcePrefix`
3. 要给其它模块提供能力时，再建 `module_x_api`：复制 `module_square_api/build.gradle`，改 `namespace`；`module_x` 里加 `api project(':module_x_api')`
4. 需要初始化就实现 `ModuleInitializer` 并在 `META-INF/services` 登记
5. `settings.gradle` include 模块，`app` 加 `implementation project(':module_x')`









