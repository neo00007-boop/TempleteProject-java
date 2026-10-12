# demo（基础库示例）

演示基础库的用法：控件、Tab、下拉刷新列表、对话框、弹窗、Glide、Lottie、沉浸式、MVP / MVVM 等。只演示基础库，代码里不引用业务模块。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_base` | implementation | 全部基础库 |
| `module_login` | runtimeOnly | 只打进包，给登录拦截示例用；代码里引用不到 |

## 主要内容

```
com.templete.project
├── DemoMainApp                Application，继承 lib_base 的 App
└── ui/
    ├── activity/              各个示例页，启动页是 MainTemplateActivity（路由 RoutePath.DEMO_MAIN）
    ├── fragment/、widget/、draw/
```

## 运行

```
./gradlew :demo:assembleGuanfangDebug
```

## 注意

- 业务模块（广场、播放、登录）的用法示例在 `app` 里，不要加到这里
- 要演示新的基础库能力，在 `ui/activity/` 加页面，并从首页加入口
