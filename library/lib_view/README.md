# lib_view（通用控件）

通用 UI 控件：ShapeView 系列（xml 里直接写圆角、描边、渐变，不用再写 shape 文件）、各种 Tab 控件，以及 androidx 常用控件库。详细属性见同目录的 `document.txt`。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_res` | api | 公共颜色、尺寸，随本库一起带给上层 |
| material、recyclerview、constraintlayout、viewpager、viewpager2、cardview | api | androidx 常用控件 |
| SmartRefreshLayout | api | 下拉刷新、上拉加载 |

## 主要内容

```
com.hjq.shape          ShapeView 系列（包名沿用开源库）
├── view/              ShapeTextView、ShapeButton、ShapeImageView 等
├── layout/            ShapeLinearLayout、ShapeConstraintLayout 等
└── builder、drawable、styleable   属性解析和 Drawable 构建

com.flyco.tablayout    Tab 控件（源自 FlycoTabLayout）
├── SlidingTabLayout、CommonTabLayout、SegmentTabLayout
└── GradientSlidingTabLayout、DrawableIndicatorSlidingTabLayout 等
```

## 用法

```xml
<com.hjq.shape.view.ShapeTextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:shape_radius="@dimen/x20"
    app:shape_solidColor="@color/white"
    app:shape_strokeColor="@color/cl_999"
    app:shape_strokeWidth="1px" />
```

Tab 控件的用法见 demo 首页的几个 tablayout 示例。

## 注意

- 命名空间是 `com.hjq.shape`，本库代码里引用资源用 `com.hjq.shape.R`
- Tab 控件的属性定义在 `res/values/tablayout_attrs.xml`
