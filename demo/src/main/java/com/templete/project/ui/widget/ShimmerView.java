package com.templete.project.ui.widget;

/**
 * @deprecated 已迁移至 {@link com.lib.base.ui.widget.ShimmerView}，保留此类仅避免旧引用编译失败。
 */
@Deprecated
public class ShimmerView extends com.lib.base.ui.widget.ShimmerView {
    public ShimmerView(@androidx.annotation.NonNull android.content.Context context) {
        super(context);
    }

    public ShimmerView(@androidx.annotation.NonNull android.content.Context context,
                       @androidx.annotation.Nullable android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public ShimmerView(@androidx.annotation.NonNull android.content.Context context,
                       @androidx.annotation.Nullable android.util.AttributeSet attrs,
                       int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** @deprecated 使用 {@link #setType(int)} */
    @Deprecated
    public void init(int type) {
        setType(type);
        show();
    }

    /** @deprecated 使用 {@link #start()} */
    @Deprecated
    public void startAnim() {
        start();
    }

    /** @deprecated 使用 {@link #stop()} */
    @Deprecated
    public void stopAnim() {
        stop();
    }
}
