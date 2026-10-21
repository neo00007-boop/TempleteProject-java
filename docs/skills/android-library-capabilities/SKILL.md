---
name: android-library-capabilities
description: TempleteProject 工程 library 目录下各基础库（lib_base、lib_router、lib_network、lib_greendao、lib_room、lib_view、lib_res、lib_startup）及其依赖的三方库的能力清单。生成或修改 Android 代码时优先复用这些已有能力，不重复造轮子、不新增同类依赖。用于在 TempleteProject 里写页面、列表、弹窗、网络请求、数据库、路由跳转、权限、图片加载、选图上传、缓存、工具方法，或用户说用已有组件、别重复造轮子、优先用 library 时。
---

# library 组件能力清单

业务模块和壳工程只依赖 `lib_base`，下表所有库都通过 `api` 传递过来，**不需要也不要**在业务模块里重复声明这些依赖。

## 生成代码的规则

1. 先在下面的「常见需求对照」和「能力清单」里找，有现成能力就直接用。
2. 不新增和已有能力重复的三方库（例如已有 Glide 就不加 Coil/Picasso，已有 BRVAH 就不手写 RecyclerView.Adapter 模板，已有 RxJava3 就不引入协程网络框架）。
3. 拿不准某个方法的参数时，打开对应源文件确认，不要凭印象编造方法名。
4. 现有能力确实不满足时，优先在对应库里扩展（工具方法加到 `lib_base/util`，控件加到 `lib_view`），再在业务里使用，并告诉用户为什么没用现成的。
5. 写在 `library` 下的代码只能依赖更底层的库，不能反过来依赖 `module_*` 或 `app`。

## 常见需求对照

| 需求 | 用这个 | 不要 |
| --- | --- | --- |
| 新页面 | 继承 `BaseActivity<XxxBinding>` / `BaseFragment<XxxBinding>`，实现 `viewBinding()`、`inits()`、`initView()`、`initEvent()`、`initData()` | 直接继承 AppCompatActivity、手写 findViewById |
| MVVM 页面 | `BaseMvvmActivity<VB, VM>` / `BaseMvvmFragment`，ViewModel 继承 `BaseViewModel`，实现 `getViewModelClass()` | 自己 new ViewModelProvider |
| MVP 页面 | `BaseMvpActivity<VB, P>` / `BaseMvpFragment`，Presenter 继承 `RxPresenter`，实现 `bindPresenter()` | |
| 下拉刷新 + 分页列表 | `BaseFreshListActivity<T, Adapter>` / `BaseFreshListFragment`：实现 `createAdapter()`、`onListRequest(isFresh)`，结果回调 `onRequestSuccess(list)` / `onRequestSuccess(list, noMore)` / `onRequestFailure()`，页码用 `getPage()`；示例 `demo/.../FreshListDemoFragment` | 手写 SmartRefreshLayout 分页逻辑 |
| 列表适配器 | BRVAH 3.x：`BaseQuickAdapter<T, BaseViewHolder>`、多类型 `BaseMultiItemQuickAdapter` | 手写 RecyclerView.Adapter |
| 标题栏 | 基类自带：`setTitleStr()`、`setTitleStrWithBold()`、`setRightClickViews()`、`hideBackView()`、`setTitleBg()` | 每个页面自己画标题栏 |
| 加载中 / 空数据 / 出错占位 | 基类自带：`showLoadingView()`、`showNoDataView()`、`showErrorView()`、`hideView()`，重试回调 `onHolderRetryClick()` | 自己写占位布局切换 |
| Toast、日志 | 页面里 `toast()`、`logD()`、`logE()`；页面外 `DebugUtil.toast()`、`DebugUtil.logD()` | 直接 Toast.makeText、Log.d |
| 弹窗 | `ui/dialog/base` 下的现成对话框，统一用 Builder：`MessageDialog`、`InputDialog`、`MenuDialog`、`SelectDialog`、`DateDialog`、`TimeDialog`、`AddressDialog`、`ShareDialog`、`UpdateDialog`、`PayPasswordDialog`、`TipsDialog`、`WaitDialog`；队列显示用 `DialogManager` | 手写 AlertDialog |
| 气泡菜单 | `PopView`、`PopMenuView` | 手写 PopupWindow |
| 页面跳转 | `Router.open(context, RoutePath.XXX)`，带参数 `Router.build(path).withString(...).navigation(context)`，接收参数 `@Autowired` + `Router.inject(this)`；路径常量只加在 `RoutePath` | 跨模块 new Intent 引用别的模块的类、直接调 ARouter |
| 跨模块调用 | 在 `module_xxx_api` 定义继承 `IProvider` 的接口，用 `Router.service(Xxx.class)` 获取 | 业务模块互相依赖 |
| 需要登录的页面 | `@Route(path = ..., extras = RouteExtras.NEED_LOGIN)`，登录拦截自动处理；手动判断用 `LoginUtil.isLogin()`、`LoginUtil.toLogin()` | 每个页面自己判断登录 |
| 网络请求 | 在业务模块写 Retrofit 接口（返回 `Single`/`Observable`），`RetrofitHelper.getInstance().getRetrofit().create(XxxApi.class)`；多域名用 `@Headers({Hosts.HOST2})`；参考 `module_login` 的 `LoginApi`、`LoginHttp` | 新建 OkHttpClient / Retrofit 实例 |
| 线程切换 + 绑定生命周期 + 加载框 | `.compose(RxUtils.toSimpleSingle(this))`，带加载框 `RxUtils.toSimpleSingle(this, "加载中", true)`；Observable/Flowable 有同名方法 | 手写 subscribeOn/observeOn、忘记解绑 |
| 防重复点击 | `RxUtils.throwFirstClick()` / `throwEndClick()`（基于 RxBinding） | 自己记时间戳 |
| 普通子线程 / 回主线程 | `GlobalThreadPoolUtil.postOnSubThread()`、`postOnUiThread()`、`postOnUiThreadDelay()`；页面里 `post()`、`postDelayed()` | new Thread、new Handler |
| 页面间、组件间通信 | `LiveDataBus.get().with(key, Xxx.class)`（基于 UnPeek-LiveData，不会倒灌）；全局状态放 `GlobalViewModel` | EventBus、静态变量 |
| 简单键值存储 | `SPUtil.put()`、`getString()`、`getInt()`、`getBoolean()`、`getObj()`、`getList()`、`getMap()`；键名常量放 `AppConfig` | 直接 getSharedPreferences |
| 带过期时间的缓存 | `DoubleCacheUtil.put(key, obj, 秒)`、`get(key, Xxx.class)`、`get(key, Type)`（内存 + SP） | 自己写 LruCache |
| JSON | `GsonUtil.getInstance()`（容错解析，字段类型不对不崩溃）、`GsonUtil.toJson()`、`fromJsonObj()`、`fromJsonList()`、`fromJsonMap()` | new Gson() |
| 结构化数据存储 | 新功能用 `lib_room`（参考 `NoteDao`、`NoteRepository`、`Migrations`）；省市区等老数据在 `lib_greendao` 的 `LocalRepository` | 新增其他 ORM |
| 图片加载 | `GlideUtil.loadPic()`、`loadPicRound()`、`loadPicCircle()`、`loadVideoFrame()`；特效用 `glide/transformations`（模糊、灰度、裁剪、圆角、GPU 滤镜） | 引入其他图片库 |
| 选图、拍照、预览、裁剪、压缩 | `MediaPickerUtil.getMedia()`、`takeMedia()`、`preview()`（PictureSelector 3.x，已配好 Glide 引擎、压缩、裁剪、沙盒） | 直接调系统相册 |
| 运行时权限 | `PermissionUtil.requestPermission()`、`isGranted()`、`isPermanentDenied()`、`startPermissionActivity()`（XXPermissions） | 手写 requestPermissions |
| 沉浸式状态栏 | 基类 `setImmersionBar()`、`darkStatusBarFont()`（ImmersionBar） | 手写 window flags |
| 软键盘 | 页面里 `showKeyboard()`、`hideKeyboard()`；页面外 `KeyBoardUtil` | |
| 圆角、描边、渐变、按压态背景 | `lib_view` 的 ShapeView 系列（`ShapeTextView`、`ShapeButton`、`ShapeConstraintLayout`、`ShapeLinearLayout` 等），属性 `app:shape_radius`、`shape_solidColor`、`shape_strokeColor` 等，详见 `library/lib_view/document.txt` | 为每个背景单独写 shape drawable |
| Tab 指示器 | `SlidingTabLayout`、`CommonTabLayout`、`SegmentTabLayout` 等 | 手写 TabLayout 样式 |
| 尺寸、颜色 | 按 1080x1920 设计稿像素用 `@dimen/xN`（宽度方向）、`@dimen/yN`（高度方向）；颜色用 `lib_res` 的 `@color/cl_xxx` | 写死 dp、写死色值 |
| 富文本 | `TxtUtil`（多段变色、加粗、图片标签）、`Spanny`、`SuperSpanUtil` | 手写 SpannableString |
| 正则校验 | `PatternUtil.isValidPhone()`、`isValidEmail()`、`isValidPassword()`、`isValidCode()`、`isUrl()` | 业务里散落正则 |
| 日期 | `DateUtil`（格式化、时间戳互转、相对时间、星期、日期差） | 每处 new SimpleDateFormat |
| 加解密 | `MD5Util`、`SHAUtil`、`AESUtil`、`DESUtil`、`RSAUtil`、`Base64Util` | `DESUtil` 已弃用，对称加密用 `AESUtil`（输出十六进制，失败返回 null） |
| 骨架屏 | `ShimmerView`、androidveil 的 `VeilLayout`；列表页默认带骨架加载 | |
| 模块启动初始化 | 实现 `ModuleInitializer`，在本模块 `META-INF/services/com.lib.startup.ModuleInitializer` 登记 | 在 `App` 里逐个手动 init |

## 能力清单

### lib_base（基础层，业务唯一需要依赖的库）

包名 `com.lib.base`，详细说明见 `library/lib_base/README.md`、`document.txt`。

- **config**：`App`（Application 基类，已完成启动初始化、网络、Toast、全局 ViewModel）、`AppConfig`（地址、SP 键）、`ActivityLifecycleCallback`
- **ui/activity**：`BaseActivity`、`BaseMvvmActivity`、`BaseMvpActivity`、`BaseFreshActivity`、`BaseFreshListActivity`、`VideoPlayActivity`
- **ui/fragment**：`BaseFragment`、`BaseMvvmFragment`、`BaseMvpFragment`、`BaseFreshFragment`、`BaseFreshListFragment`
- **ui/action**（基类已实现的能力接口）：`ToastAction`、`LogAction`、`TitleBarAction`、`StatusAction`（占位视图）、`KeyboardAction`、`HandlerAction`、`ClickAction`、`ImmersionAction`、`IntentAction`、`BundleAction`、`AnimAction`
- **ui/fresh**：`FreshListKit`（刷新分页列表的通用实现）、`FreshListEmptySupport`
- **ui/dialog**：`BaseDialog`（Builder 基类）、`MessageDialog`、`ConfirmDialog`、`CommonDialog`、`InputDialog`、`MenuDialog`、`SelectDialog`、`DateDialog`、`TimeDialog`、`AddressDialog`、`ShareDialog`、`UpdateDialog`、`PayPasswordDialog`、`TipsDialog`、`WaitDialog`、`LoadingDialog`、`BottomSheetDialog`、`PhotoSelBottomDialog`、`SafeDialog`；`DialogManager`（弹窗队列）；`@SingleClick`
- **ui/pop**：`PopView`、`PopMenuView`
- **ui/widget**：`TitleBar`、`HolderView`（占位视图）、`ShimmerView`、`GlideImageView`、`GlideRoundImageView`、`PlayerView`、`PlayButton`、`BlackToastStyle`
- **mvvm / mvp**：`BaseViewModel`（`setBaseData()` 通知页面状态）、`GlobalViewModel`、`BaseContract`、`RxPresenter`
- **rxjava**：`RxUtils`（`toSimpleSingle/Observable/Flowable` 线程切换 + 生命周期 + 加载框，`bindToLifecycle`、`bindUntilEvent`，防重复点击）、`RxHelper`
- **glide**：`GlideUtil`、`GlideConfig`、`OkHttpLoader`；`transformations`：`BlurTransformation`、`GrayscaleTransformation`、`RoundedCornersTransformation`、`CropCircleTransformation`、`CropCircleWithBorderTransformation`、`CropSquareTransformation`、`CropTransformation`、`ColorFilterTransformation`、`MaskTransformation`，GPU 滤镜（亮度、对比度、反色、像素化、素描、漩涡、卡通、暗角、怀旧等）
- **util**：
  - 存储缓存：`SPUtil`、`DoubleCacheUtil`
  - 登录：`LoginUtil`（`isLogin`、`toLogin`、`clear`、`getUserNum`）
  - 线程：`GlobalThreadPoolUtil`
  - 通信：`LiveDataBus`
  - 权限：`PermissionUtil`
  - 设备与应用：`AppUtil`（版本号、前台判断、平板判断）、`NetUtil`（有网、WiFi、移动网络）、`ProcessUtil`（主进程判断）、`ScreenUtil`（屏幕宽高、状态栏、导航栏高度）、`UIUtil`（dp/px 换算）
  - 页面与上下文：`ContextUtil`（Context 转 Activity/LifecycleOwner、页面是否存活）、`IntentUtil`（带动画跳转、回桌面）、`KeyBoardUtil`、`ViewUtil`
  - 刷新：`FreshUtil`（结束刷新/加载、没有更多数据）
  - 日志调试：`DebugUtil`、`BaseLog`
  - 其他：`DateUtil`、`JsonDataUtil`（读 assets json）、`OUtil`（判空）
  - `txt`：`PatternUtil`、`StringCompareUtil`、`TxtUtil`、`Spanny`、`FakeBoldSpan`、`SuperSpanUtil`、`TextDrawable`、`VerticalImageSpan`
  - `encrypt`：`MD5Util`、`SHAUtil`、`AESUtil`、`DESUtil`、`RSAUtil`、`Base64Util`
  - `upload`：`MediaPickerUtil`（选图、拍照、预览、清缓存）、`GlideEngine`、`ImageCompressEngine`、`ImageCropEngine`、七牛上传回调 `MyResultCallback`
  - `inject`：`@ViewInject` + `ViewInjectUtil`（反射注入，新代码优先用 ViewBinding）

### lib_router（路由）

包名 `com.lib.router`：`Router`（`open`、`build`、`service`、`inject`）、`RoutePath`（全工程路由路径和参数名，按模块分段）、`RouteExtras`（`NEED_LOGIN`）、`RouterInitializer`。路径格式 `/模块名/页面`，一个分组只属于一个模块；有 `@Route` 的模块要配 arouter-compiler 和 `AROUTER_MODULE_NAME`（参考 `module_player/build.gradle`）。

### lib_network（网络）

包名 `com.lib.network`：`RetrofitHelper`（`init(HttpConfig)`、`getInstance().getRetrofit()`）、`HttpConfig`（baseUrl、多域名、公共请求头、token 提供者、登录失效回调、Gson、debug）、`Hosts`（多域名 key 和请求头）、`NetworkInterceptor`（公共头、token、登录失效识别）、`UrlManagerInterceptor`（切换域名）、`RetryInterceptor`、`OkCookieJar`、`HttpLog`。初始化已在 `App.initNetwork()` 完成。接口返回的实体要在所在模块 `consumer-rules.pro` 里保留字段。

### lib_greendao（GreenDao 数据库，老功能）

包名 `com.greendao.db`：`LocalRepository`（数据库入口，`getCityBeanDao()`、`getDaoSession()`）、`DbHelper`、`DBUpdateHelper`（升级迁移）、`GsonUtil`（全局 Gson）、`MapTypeAdapter`（Map/Object 解析时整数不变成 double）、内置省市区数据 `CityBean`、`CityDataBean`。`helper/` 是生成代码，不要手改。

### lib_room（Room 数据库，新功能首选）

包名 `com.lib.room`：`AppDatabase`（`get(context)`，库名 app_room.db）、`entity`、`dao`（返回 `LiveData` 的查询自动在子线程执行并推送变化）、`migration/Migrations`、`repository/NoteRepository`（写操作切子线程、回调主线程）。改表结构：改实体 → `VERSION` +1 → 编译生成 `schemas` json → 写 `MIGRATION_旧_新` 加进 `ALL` → 补 `MigrationTest`。不开 destructive 迁移。示例页 `app/.../sample/RoomSampleActivity`。

### lib_view（通用控件）

- **ShapeView 系列**（`com.hjq.shape`）：`ShapeTextView`、`ShapeButton`、`ShapeEditText`、`ShapeImageView`、`ShapeCheckBox`、`ShapeRadioButton`、`ShapeRadioGroup`、`ShapeView`、`ShapeRecyclerView`、`ShapeLinearLayout`、`ShapeFrameLayout`、`ShapeRelativeLayout`、`ShapeConstraintLayout`，属性见 `library/lib_view/document.txt`
- **按比例**：`RatioTextView`、`RatioImageView`、`RatioFrameLayout`、`RatioLinearLayout`、`RatioRelativeLayout`、`RatioConstraintLayout`
- **圆角**：`RoundImageView`、`RoundedImageView`、`RoundLayout`、`RoundCardView`
- **输入**：`RegexEditText`（正则限制输入）、`PasswordEditText`、`PasswordView`（支付密码格）
- **常用控件**：`SettingBar`（设置项行）、`SwitchButton`、`CountdownView`（验证码倒计时）、`SimpleRatingBar`、`DrawableTextView`、`SmartTextView`、`PressAlphaTextView`、`MsgView`（红点角标）、`TriangleView`、`StatuesBar`、`ScaleImageView`、`FastIndexBar`（字母索引）、`FlowLayout`（流式标签）、`SwipeMenuLayout`（侧滑菜单）
- **列表与滚动**：`WrapRecyclerView`（头尾布局）、`TransRecyclerView`、`StopScrollManager`、`CenterLayoutManager`、`PinnedSectionListView`、`NestedScrollWebView`、`NestedViewPager`、`NestedLinearLayout`、`NoScrollViewPager`、`CstViewPager`
- **刷新头尾**：SmartRefreshLayout 经典/Material 头尾，及自定义 `SunHeader`、`GrassHeader`、`ClassicsFooter`，横向刷新 `SmartRefreshHorizontal`
- **Tab**（`com.flyco.tablayout`）：`SlidingTabLayout`、`CommonTabLayout`、`SegmentTabLayout`、`CenterBlockSlidingTabLayout`、`GradientSlidingTabLayout`、`DrawableIndicatorSlidingTabLayout`
- **androidx**：material、recyclerview、constraintlayout、viewpager、viewpager2、cardview

### lib_res（公共资源）

`@dimen/x1~x1080`、`@dimen/y1~y1920`（1080x1920 设计稿像素，已覆盖多分辨率）、公共颜色 `colors.xml`/`colors_ui.xml`、字符串、转场动画（`bottom_in_anim`、`no_anim` 等）、颜色 selector、公共图片。业务独有的资源放自己模块，并加模块前缀（`sq_`、`py_`、`lg_`）。

### lib_startup（启动初始化）

`ModuleInitializer`（`priority()`、`onCreate(Application)`）、`ModuleInitializers`（ServiceLoader 收集并按优先级执行，`App` 已调用）。用到路由的初始化，优先级不能小于 `PRIORITY_ROUTER`。

## 可直接使用的三方库（已通过 lib_base 传递）

| 用途 | 库 |
| --- | --- |
| 响应式 | RxJava3、RxBinding4、RxLifecycle4 |
| 网络 | Retrofit2（RxJava3 适配器、Gson 转换器）、OkHttp |
| JSON | Gson、GsonFactory（容错解析） |
| 路由 | ARouter |
| 数据库 | Room、GreenDao |
| 列表 | BaseRecyclerViewAdapterHelper 3.0.14（不要升到 3.0.15+，和工程 Kotlin 版本不兼容）、SmartRefreshLayout、flexbox |
| 图片 | Glide、GPUImage、Lottie |
| 选图拍照 | PictureSelector 3.x（含 CameraX） |
| 权限 | XXPermissions |
| Toast | ToastUtils |
| 状态栏 | ImmersionBar |
| 其他控件 | youth Banner、AndRatingBar、DragFooterView、androidveil（骨架屏） |
| LiveData | UnPeek-LiveData（`MutableResult`，不倒灌） |
| 调试 | LeakCanary（仅 debug 包，自动生效） |

七牛上传 SDK、PictureSelector 的压缩和裁剪组件只在 `lib_base` 内部使用，上层通过 `MediaPickerUtil` 等封装调用，不直接引用它们的类。
