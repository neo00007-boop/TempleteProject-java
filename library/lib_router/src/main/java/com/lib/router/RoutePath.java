package com.lib.router;

/**
 * 全部路由路径和路由参数名。各模块之间跳转只用这里的常量，不需要依赖对方模块。
 * <p>
 * 规则：
 * 1. 路径一级目录即 ARouter 分组，每个模块用自己的分组，不能和其它模块重复
 * 2. 常量名以模块名开头，按模块分段，新增页面加到对应模块段里
 * 3. 跨模块调用的能力（服务接口）仍放在各模块的 xxx_api 里
 */
public final class RoutePath {

    private RoutePath() {
    }

    // ==================== 广场 module_square ====================

    private static final String SQUARE = "/module_square";
    /**
     * 广场首页
     */
    public static final String SQUARE_ACTIVITY = SQUARE + "/SquareActivity";
    /**
     * 广场服务，接口见 module_square_api 的 SquareService
     */
    public static final String SQUARE_SERVICE = SQUARE + "/SquareService";

    // ==================== 播放 module_player ====================

    private static final String PLAYER = "/module_player";
    /**
     * 播放页，参数：{@link #PLAYER_KEY_VIDEO_ID}
     */
    public static final String PLAYER_ACTIVITY = PLAYER + "/PlayerActivity";
    /**
     * 播放页参数：视频 id（String）
     */
    public static final String PLAYER_KEY_VIDEO_ID = "videoId";

    // ==================== 登录 module_login ====================

    private static final String LOGIN = "/module_login";
    /**
     * 登录页，参数：{@link #LOGIN_KEY_DATA}
     */
    public static final String LOGIN_ACTIVITY = LOGIN + "/LoginActivity";
    /**
     * 登录页参数（String）
     */
    public static final String LOGIN_KEY_DATA = "data";

    // ==================== 示例 demo ====================

    /**
     * demo 首页
     */
    public static final String DEMO_MAIN = "/demo/MainActivity";
}
