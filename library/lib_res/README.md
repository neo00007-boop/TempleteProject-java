# lib_res（公共资源）

所有界面模块共用的资源，不放代码。由 `lib_view` 用 `api` 带出，依赖 `lib_base` 的模块都能直接用。

## 依赖

无。

## 主要内容

```
res/
├── values/
│   ├── lay_x.xml、lay_y.xml    屏幕适配尺寸 x1~x1080、y1~y1920（设计稿 1080x1920 的像素值）
│   ├── colors.xml、colors_ui.xml
│   ├── dimens.xml、strings.xml、integers.xml、bools.xml
├── values-宽x高/                各分辨率的 lay_x、lay_y，覆盖 480x320 到 3200x1440
├── anim/                       转场动画（如 bottom_in_anim、no_anim）
├── color/                      颜色 selector
└── drawable、drawable-xxhdpi、mipmap-xxhdpi   公共图片和 shape
```

## 用法

按 1080x1920 设计稿标注的像素直接写：宽度方向用 `x`，高度方向用 `y`。

```xml
android:layout_width="@dimen/x300"
android:layout_marginTop="@dimen/y40"
android:textSize="@dimen/x45"
android:textColor="@color/cl_333333"
```

## 注意

- 只放多个模块都会用到的资源，某个业务模块独有的资源放它自己的模块，并加前缀
- 图片用 WebP（`cwebp` 默认参数），点 9 图保留 PNG
- 工程没开 nonTransitiveRClass，上层模块直接用 `R.xxx` 就能引用这里的资源
