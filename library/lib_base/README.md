# lib_base（UI 基础层）

页面基类、通用工具和 Application 基类。业务模块和壳工程只依赖这一个基础库，就能用到路由、网络、数据库、控件和公共资源。功能介绍见同目录的 `document.txt`。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_router` | api | 路由；`LoginUtil` 打开登录页也要用 |
| `lib_network` | api | 网络 |
| `lib_greendao`、`lib_room` | api | 数据库 |
| `lib_view` | api | 通用控件，同时带出 `lib_res` |
| XXPermissions、PictureSelector、RxLifecycle、BRVAH 等 | api / implementation | 权限、选图、生命周期、列表适配器等 |

用 `api` 依赖的库会传给依赖 `lib_base` 的模块，所以业务模块不用再单独声明这些基础库。

## 主要内容

```
com.lib.base
├── config/       App（Application 基类）、AppConfig（地址、SP 键）、ActivityLifecycleCallback
├── ui/
│   ├── activity/     BaseActivity、BaseMvvmActivity、BaseMvpActivity、BaseFreshListActivity 等
│   ├── fragment/     BaseFragment、BaseMvvmFragment、BaseFreshListFragment 等
│   ├── dialog/、pop/ 对话框、弹窗
│   ├── action/       Toast、日志、标题栏、键盘等能力接口，基类已实现
│   └── widget/       Banner、视频播放等控件
├── mvvm/、mvp/   BaseViewModel、GlobalViewModel、RxPresenter
├── rxjava/       RxUtils：线程切换、绑定生命周期、加载框
├── util/         LoginUtil、SPUtil、AppUtil、DateUtil、LiveDataBus、PermissionUtil 等
├── glide/        Glide 配置和图片变换
└── adapter/、bean/、listener/
```

## 用法

壳工程的 Application 继承 `App`：

```java
public class MainApp extends App { }
```

页面继承 `BaseActivity`，用 ViewBinding：

```java
public class XxxActivity extends BaseActivity<XxxActivityBinding> {
    @Override
    protected XxxActivityBinding viewBinding() {
        return XxxActivityBinding.inflate(getLayoutInflater());
    }

    @Override public void inits() { setTitleStr("标题"); }
    @Override public void initView() { }
    @Override public void initEvent() { }
    @Override public void initData() { }
}
```

网络请求配合 `RxUtils`：

```java
LoginHttp.getInstance().login(phone, pwd)
        .compose(RxUtils.toSimpleSingle(this, null, false))   // 切线程、绑定页面生命周期
        .subscribe(bean -> { }, throwable -> { });
```

## 注意

- `App.onCreate()` 里完成了启动初始化（`ModuleInitializers`）、网络、Toast、全局 ViewModel 等，子类只需继承
- `GlobalViewModel` 的构造需要 `Application`，创建时必须用 `AndroidViewModelFactory`
- 登录相关只用 `LoginUtil`：`toLogin()`、`isLogin()`，不要直接依赖 `module_login`
