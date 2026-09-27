package com.templete.project.ui.activity;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.txt.label.TxtUtil;
import com.lib.base.util.txt.superSoan.SpanData;
import com.templete.project.R;
import com.templete.project.databinding.TxtActivityBinding;

/**
 * PackageName  com.templete.project.ui.activity
 * ProjectName  TempleteProject-java
 * Date         2022/2/15.
 *
 * @author xwchen
 */

public class TxtActivity extends BaseActivity<TxtActivityBinding> {
    @Override
    public void inits() {
        setTitleStr("繁琐的文本标签");
    }

    @Override
    public void initView() {
        /**
         * 多标签上色
         */
        TxtUtil.setMultipleLabelColor(mViewBinding.tv3, "#FF0000", mViewBinding.tv3.getText().toString(), "abc", "标签");
        /**
         * 多标签上色并加粗
         */
        TxtUtil.setMultipleLabelColorBold(mViewBinding.tv14, "#FF0000", true, mViewBinding.tv14.getText().toString(), "abc", "标签");
        /**
         * 行首增加标签
         */
        TxtUtil.addStartLabels(this, mViewBinding.tv4, mViewBinding.tv4.getText().toString(), (int) getDimen(R.dimen.x50), "标签1");
        /**
         * 行首增加双标签
         */
        TxtUtil.addStartLabels(this, mViewBinding.tv5, mViewBinding.tv5.getText().toString(), (int) getDimen(R.dimen.x50), "标签1", "标签2", "标签3", "标签4");
        /**
         * 修改字体粗细,默认值0.3,注意不要跟粗体一起使用
         */
        TxtUtil.setTextBold(mViewBinding.tv6, "默认0.1粗细的字体");
        /**
         * 修改字体粗细,修改为2.0,注意不要跟粗体一起使用
         */
        TxtUtil.setTextBold(mViewBinding.tv7, "2.0粗细的字体", (float) 2.0);
        /**
         * 超级富文本,可以修改多个标签,包括颜色,字号,粗体,可点击
         */
        TxtUtil.setSuperLabel(mViewBinding.tv8, "#FF0000", (int) getDimen(R.dimen.x75), true, true,
                index -> {
                    // todo
                    toast("点击了第" + index + "个标签");
                },
                SpanData.build("这是一", false),
                SpanData.build("段超级文本", true),
                SpanData.build(",有颜色的", false),
                SpanData.build("+粗体+", true),
                SpanData.build("大字号", false),
                SpanData.build("的都可以点击", true),
                SpanData.build(",可以对多个", false),
                SpanData.build("标签", true),
                SpanData.build("进行修改,就问你溜不溜", false));

        TxtUtil.setImageSpan(mViewBinding.tv9, mViewBinding.tv9.getText().toString(), R.drawable.ic_vip_tag, R.drawable.ic_vip_tag, R.drawable.ic_vip_tag);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
    }

    @Override
    protected TxtActivityBinding viewBinding() {
        return TxtActivityBinding.inflate(getLayoutInflater());
    }
}
