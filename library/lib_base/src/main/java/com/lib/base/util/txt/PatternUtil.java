package com.lib.base.util.txt;

import java.util.regex.Pattern;

/**
 * 常用输入格式校验。
 * <p>
 * 这里只做客户端格式粗筛，不能当安全校验：密码强度、邮箱是否真实存在、
 * 手机号是否已注册，都要以后台为准。null 一律返回 false。
 * <p>
 * 各规则都预编译成 {@link Pattern}，可直接反复调用。
 */
public final class PatternUtil {

    private PatternUtil() {
    }

    /**
     * 用户名：总长 2~20。
     * 首位只能是中文或英文字母，后面可跟中文、英文、数字、下划线。
     * 例：张三、abc_01 通过；1abc、a、超长名 不通过。
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5a-zA-Z0-9_]{1,19}$"
    );

    /**
     * 密码：总长 8~20，只允许英文、数字和常见特殊字符。
     * 不强制大小写/数字/符号混用，纯 aaaaaaaa 也会通过，强度需另做。
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$"
    );

    /**
     * 邮箱：本地部分 + @ + 域名 + 后缀（后缀至少 2 位字母）。
     * 覆盖常见写法，不解析 IP 域名、注释等冷门形态。
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * 中国大陆手机号：11 位，1 开头，第二位 3~9。
     * 不带 +86、空格、横线；带区号要先自己去掉再验。
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );

    /**
     * 验证码：恰好 6 位数字。
     */
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "^\\d{6}$"
    );

    /**
     * 纯数字：一位及以上，允许前导 0。
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "^\\d+$"
    );

    /**
     * 纯英文字母：一位及以上，大小写均可，不含空格和其它字符。
     */
    private static final Pattern LETTER_PATTERN = Pattern.compile(
            "^[a-zA-Z]+$"
    );

    /**
     * URL：http / https / ftp 开头，后面非空白即可。
     * 只拦明显非法串，不做连通性或域名合法性检查。
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s]+$"
    );

    /** @see #USERNAME_PATTERN */
    public static boolean isValidUsername(String username) {
        return username != null
                && USERNAME_PATTERN.matcher(username).matches();
    }

    /** @see #PASSWORD_PATTERN */
    public static boolean isValidPassword(String password) {
        return password != null
                && PASSWORD_PATTERN.matcher(password).matches();
    }

    /** @see #EMAIL_PATTERN */
    public static boolean isValidEmail(String email) {
        return email != null
                && EMAIL_PATTERN.matcher(email).matches();
    }

    /** @see #PHONE_PATTERN */
    public static boolean isValidPhone(String phone) {
        return phone != null
                && PHONE_PATTERN.matcher(phone).matches();
    }

    /** @see #CODE_PATTERN */
    public static boolean isValidCode(String code) {
        return code != null
                && CODE_PATTERN.matcher(code).matches();
    }

    /** @see #NUMBER_PATTERN */
    public static boolean isNumber(String value) {
        return value != null
                && NUMBER_PATTERN.matcher(value).matches();
    }

    /** @see #LETTER_PATTERN */
    public static boolean isLetter(String value) {
        return value != null
                && LETTER_PATTERN.matcher(value).matches();
    }

    /** @see #URL_PATTERN */
    public static boolean isUrl(String value) {
        return value != null
                && URL_PATTERN.matcher(value).matches();
    }
}
