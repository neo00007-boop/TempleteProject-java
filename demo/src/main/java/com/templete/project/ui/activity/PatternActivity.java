package com.templete.project.ui.activity;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.txt.PatternUtil;
import com.templete.project.databinding.ActivityPatternBinding;

/**
 * {@link PatternUtil} 对照页：每条规则给通过/不通过样例。
 */
public class PatternActivity extends BaseActivity<ActivityPatternBinding> {

    @Override
    protected ActivityPatternBinding viewBinding() {
        return ActivityPatternBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("PatternUtil 正则");
    }

    @Override
    public void initView() {
        section("用户名 isValidUsername",
                "张三", true,
                "abc_01", true,
                "1abc", false,
                "a", false);

        section("密码 isValidPassword",
                "abcd1234", true,
                "Abcd@123", true,
                "1234567", false,
                "aaaa aaa1", false);

        section("邮箱 isValidEmail",
                "a@b.com", true,
                "user.name+1@mail.co", true,
                "a@b", false,
                "@mail.com", false);

        section("手机号 isValidPhone",
                "13800138000", true,
                "19912345678", true,
                "12800138000", false,
                "+8613800138000", false);

        section("验证码 isValidCode",
                "123456", true,
                "000000", true,
                "12345", false,
                "1234567", false);

        section("纯数字 isNumber",
                "0", true,
                "00123", true,
                "12a", false,
                "", false);

        section("纯字母 isLetter",
                "Abc", true,
                "xyz", true,
                "ab1", false,
                "ab c", false);

        section("URL isUrl",
                "https://a.com", true,
                "ftp://a.com/x", true,
                "www.a.com", false,
                "http://a com", false);
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    private void section(String title, Object... samples) {
        TextView head = text(title, 16, true, 0xFF222222);
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headLp.topMargin = dp(16);
        mViewBinding.resultBox.addView(head, headLp);

        for (int i = 0; i + 1 < samples.length; i += 2) {
            String input = String.valueOf(samples[i]);
            boolean expect = (Boolean) samples[i + 1];
            boolean actual = run(title, input);
            String line = "「" + (input.isEmpty() ? "(空)" : input) + "」 → "
                    + (actual ? "通过" : "不通过")
                    + (actual == expect ? "" : "  (期望" + (expect ? "通过" : "不通过") + ")");
            int color = actual == expect
                    ? (actual ? 0xFF2E7D32 : 0xFF616161)
                    : 0xFFC62828;
            TextView row = text(line, 14, false, color);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(6);
            mViewBinding.resultBox.addView(row, lp);
        }
    }

    private boolean run(String title, String input) {
        if (title.contains("Username")) {
            return PatternUtil.isValidUsername(input);
        }
        if (title.contains("Password")) {
            return PatternUtil.isValidPassword(input);
        }
        if (title.contains("Email")) {
            return PatternUtil.isValidEmail(input);
        }
        if (title.contains("Phone")) {
            return PatternUtil.isValidPhone(input);
        }
        if (title.contains("Code")) {
            return PatternUtil.isValidCode(input);
        }
        if (title.contains("isNumber")) {
            return PatternUtil.isNumber(input);
        }
        if (title.contains("isLetter")) {
            return PatternUtil.isLetter(input);
        }
        return PatternUtil.isUrl(input);
    }

    private TextView text(String content, int sp, boolean bold, int color) {
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        if (bold) {
            tv.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return tv;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
