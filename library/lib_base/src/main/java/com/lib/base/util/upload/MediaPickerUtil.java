package com.lib.base.util.upload;

import android.content.Context;
import android.graphics.Color;

import com.lib.base.R;
import com.luck.picture.lib.animators.AnimationType;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.manager.PictureCacheManager;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * R.style.picture_default_style
 * R.style.picture_white_style
 * R.style.picture_QQ_style
 * R.style.picture_Sina_style
 * R.style.picture_WeChat_style
 */
public class MediaPickerUtil {

    public static final int STYLE_DEFAULT = 0;
    public static final int STYLE_WHITE = 1;
    public static final int STYLE_QQ = 2;
    public static final int STYLE_WECHAT = 3;
    public static int styleSelector = STYLE_WECHAT;

    public static final int ANIM_DEFAULT = 0;
    public static final int ANIM_UP = 1;
    public static int styleWindowAnimation = ANIM_DEFAULT;

    public static final int RECYCLER_ANIM_DEFAULT = 0;
    public static final int RECYCLER_ANIM_ALPHA = 1;
    public static final int RECYCLER_SLIDE_IN = 1;
    public static int animRecyclerView = RECYCLER_ANIM_ALPHA;

    /**
     * RecyclerView Anim
     *
     * @return
     */
    private static int getAnimRecyclerView() {
        if (animRecyclerView == RECYCLER_ANIM_ALPHA) {
            return AnimationType.ALPHA_IN_ANIMATION;
        } else if (animRecyclerView == RECYCLER_SLIDE_IN) {
            return AnimationType.SLIDE_IN_BOTTOM_ANIMATION;
        } else {
            return AnimationType.DEFAULT_ANIMATION;
        }
    }

    /**
     * 获取主题风格
     *
     * @param context
     * @return
     */
    private static PictureSelectorStyle getStyle(Context context) {
        //默认主题
        PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
        //style
        if (styleSelector == STYLE_WHITE) {
            setWhiteStyle(context, pictureSelectorStyle);
        } else if (styleSelector == STYLE_QQ) {
            setQqStyle(context, pictureSelectorStyle);
        } else if (styleSelector == STYLE_WECHAT) {
            setWeChatStyle(context, pictureSelectorStyle);
        }
        //anim
        if (styleWindowAnimation == ANIM_DEFAULT) {
            setDefaultWindowAnim(pictureSelectorStyle);
        } else {
            setWUpindowAnim(pictureSelectorStyle);
        }
        return pictureSelectorStyle;
    }

    /**
     * 底部弹出
     *
     * @param pictureSelectorStyle
     */
    private static void setWUpindowAnim(PictureSelectorStyle pictureSelectorStyle) {
        PictureWindowAnimationStyle animationStyle = new PictureWindowAnimationStyle();
        animationStyle.setActivityEnterAnimation(R.anim.ps_anim_up_in);
        animationStyle.setActivityExitAnimation(R.anim.ps_anim_down_out);
        pictureSelectorStyle.setWindowAnimationStyle(animationStyle);
    }

    /**
     * 默认
     *
     * @param pictureSelectorStyle
     */
    private static void setDefaultWindowAnim(PictureSelectorStyle pictureSelectorStyle) {
        PictureWindowAnimationStyle defaultAnimationStyle = new PictureWindowAnimationStyle();
        defaultAnimationStyle.setActivityEnterAnimation(R.anim.ps_anim_enter);
        defaultAnimationStyle.setActivityExitAnimation(R.anim.ps_anim_exit);
        pictureSelectorStyle.setWindowAnimationStyle(defaultAnimationStyle);
    }

    /**
     * 微信经典风格
     *
     * @param context
     * @param pictureSelectorStyle
     */
    private static void setWeChatStyle(Context context, PictureSelectorStyle pictureSelectorStyle) {
        SelectMainStyle numberSelectMainStyle = new SelectMainStyle();
        numberSelectMainStyle.setSelectNumberStyle(true);
        numberSelectMainStyle.setPreviewSelectNumberStyle(false);
        numberSelectMainStyle.setPreviewDisplaySelectGallery(true);
        numberSelectMainStyle.setSelectBackground(R.drawable.ps_default_num_selector);
        numberSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_preview_checkbox_selector);
        numberSelectMainStyle.setSelectNormalBackgroundResources(R.drawable.ps_select_complete_normal_bg);
        numberSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        numberSelectMainStyle.setSelectNormalText(context.getString(R.string.ps_send));
        numberSelectMainStyle.setAdapterPreviewGalleryBackgroundResource(R.drawable.ps_preview_gallery_bg);
        numberSelectMainStyle.setAdapterPreviewGalleryItemSize(DensityUtil.dip2px(context, 52));
        numberSelectMainStyle.setPreviewSelectText(context.getString(R.string.ps_select));
        numberSelectMainStyle.setPreviewSelectTextSize(14);
        numberSelectMainStyle.setPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberSelectMainStyle.setPreviewSelectMarginRight(DensityUtil.dip2px(context, 6));
        numberSelectMainStyle.setSelectBackgroundResources(R.drawable.ps_select_complete_bg);
        numberSelectMainStyle.setSelectText(context.getString(R.string.ps_send_num));
        numberSelectMainStyle.setSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_black));
        numberSelectMainStyle.setCompleteSelectRelativeTop(true);
        numberSelectMainStyle.setPreviewSelectRelativeBottom(true);
        numberSelectMainStyle.setAdapterItemIncludeEdge(false);

        // 头部TitleBar 风格
        TitleBarStyle numberTitleBarStyle = new TitleBarStyle();
        numberTitleBarStyle.setHideCancelButton(true);
        numberTitleBarStyle.setAlbumTitleRelativeLeft(true);
        numberTitleBarStyle.setTitleAlbumBackgroundResource(R.drawable.ps_album_bg);
        numberTitleBarStyle.setTitleDrawableRightResource(R.drawable.ps_ic_grey_arrow);
        numberTitleBarStyle.setPreviewTitleLeftBackResource(R.drawable.ps_ic_normal_back);

        // 底部NavBar 风格
        BottomNavBarStyle numberBottomNavBarStyle = new BottomNavBarStyle();
        numberBottomNavBarStyle.setBottomPreviewNarBarBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_half_grey));
        numberBottomNavBarStyle.setBottomPreviewNormalText(context.getString(R.string.ps_preview));
        numberBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        numberBottomNavBarStyle.setBottomPreviewNormalTextSize(16);
        numberBottomNavBarStyle.setCompleteCountTips(false);
        numberBottomNavBarStyle.setBottomPreviewSelectText(context.getString(R.string.ps_preview_num));
        numberBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));

        pictureSelectorStyle.setTitleBarStyle(numberTitleBarStyle);
        pictureSelectorStyle.setBottomBarStyle(numberBottomNavBarStyle);
        pictureSelectorStyle.setSelectMainStyle(numberSelectMainStyle);
    }

    /**
     * qq风格
     *
     * @param context
     * @param pictureSelectorStyle
     */
    private static void setQqStyle(Context context, PictureSelectorStyle pictureSelectorStyle) {
        TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
        blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_blue));

        BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
        numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.picture_num_oval_blue);
        numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));

        SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
        numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectNumberStyle(true);
        numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);
        numberBlueSelectMainStyle.setSelectBackground(R.drawable.picture_checkbox_num_selector);
        numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectText(context.getString(R.string.ps_completed));

        pictureSelectorStyle.setTitleBarStyle(blueTitleBarStyle);
        pictureSelectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
        pictureSelectorStyle.setSelectMainStyle(numberBlueSelectMainStyle);
    }

    /**
     * 白色主题
     *
     * @param context
     * @param pictureSelectorStyle
     */
    private static void setWhiteStyle(Context context, PictureSelectorStyle pictureSelectorStyle) {
        TitleBarStyle whiteTitleBarStyle = new TitleBarStyle();
        whiteTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_white));
        whiteTitleBarStyle.setTitleDrawableRightResource(R.drawable.ic_orange_arrow_down);
        whiteTitleBarStyle.setTitleLeftBackResource(R.drawable.ps_ic_black_back);
        whiteTitleBarStyle.setTitleTextColor(ContextCompat.getColor(context, R.color.ps_color_black));
        whiteTitleBarStyle.setTitleCancelTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        whiteTitleBarStyle.setDisplayTitleBarLine(true);

        BottomNavBarStyle whiteBottomNavBarStyle = new BottomNavBarStyle();
        whiteBottomNavBarStyle.setBottomNarBarBackgroundColor(Color.parseColor("#EEEEEE"));
        whiteBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));

        whiteBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        whiteBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_fa632d));
        whiteBottomNavBarStyle.setCompleteCountTips(false);
        whiteBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        whiteBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));

        SelectMainStyle selectMainStyle = new SelectMainStyle();
        selectMainStyle.setStatusBarColor(ContextCompat.getColor(context, R.color.ps_color_white));
        selectMainStyle.setDarkStatusBarBlack(true);
        selectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        selectMainStyle.setSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_fa632d));
        selectMainStyle.setSelectText(context.getString(R.string.ps_done_front_num));
        selectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_white));

        pictureSelectorStyle.setTitleBarStyle(whiteTitleBarStyle);
        pictureSelectorStyle.setBottomBarStyle(whiteBottomNavBarStyle);
        pictureSelectorStyle.setSelectMainStyle(selectMainStyle);
    }

    // 单独拍照
    public static void takeMedia(Context context, int chooseMode, boolean customCamera, boolean crop, MeOnResultCallbackListener callbackListener) {
        PictureSelector.create(context)
                .openCamera(chooseMode)
                .setCameraInterceptListener(customCamera ? new MeOnCameraInterceptListener() : null)//自定义相机
                .setCropEngine(crop ? new ImageCropEngine() : null)//裁剪
                .setCompressEngine(new ImageCompressEngine())//压缩
                .setSandboxFileEngine(new MeSandboxFileEngine())//沙箱路径
                .isOriginalControl(false)//原图
                .setOutputAudioDir(Util.getSandboxAudioOutputPath())
                //.setSelectedData(mAdapter.getData());// 相册已选数据
                .setRecordVideoMaxSecond(10)
                .setRecordVideoMinSecond(2)
                .forResult(callbackListener);
    }

    /** 外部预览删除回调（预览页点删除后）。 */
    public interface OnPreviewDeleteListener {
        void onDelete(int position);
    }

    // 预览图片、视频、音频（显示下载、删除）
    public static void preview(Context context, int position, ArrayList<LocalMedia> data) {
        preview(context, position, data, true, true, null);
    }

    /**
     * @param showDelete   标题栏是否显示删除
     * @param showDownload 是否显示下载（false 则隐藏下载入口）
     * @param deleteListener 删除后额外回调；为 null 时仍会从 data 中 remove
     */
    public static void preview(Context context, int position, ArrayList<LocalMedia> data,
                               boolean showDelete, boolean showDownload,
                               @Nullable OnPreviewDeleteListener deleteListener) {
        if (data == null || data.isEmpty()) {
            return;
        }
        int safePos = Math.max(0, Math.min(position, data.size() - 1));
        PictureSelector.create(context)
                .openPreview()
                .setImageEngine(GlideEngine.createGlideEngine())
                .setSelectorUIStyle(getStyle(context))
                .setLanguage(LanguageConfig.CHINESE)
                .isPreviewFullScreenMode(true)
                .isHidePreviewDownload(!showDownload)
                .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
                    @Override
                    public void onPreviewDelete(int deletePosition) {
                        if (deletePosition >= 0 && deletePosition < data.size()) {
                            data.remove(deletePosition);
                        }
                        if (deleteListener != null) {
                            deleteListener.onDelete(deletePosition);
                        }
                    }

                    @Override
                    public boolean onLongPressDownload(LocalMedia media) {
                        // false：走 PictureSelector 默认下载逻辑
                        return false;
                    }
                })
                .startActivityPreview(safePos, showDelete, data);
    }

    /**
     * 网络/本地路径列表预览（九宫格等场景），默认显示下载与删除。
     */
    public static void preview(@NonNull Context context, int position, @Nullable List<String> urls) {
        preview(context, position, urls, true, true, null);
    }

    public static void preview(@NonNull Context context, int position, @Nullable List<String> urls,
                               boolean showDelete, boolean showDownload,
                               @Nullable OnPreviewDeleteListener deleteListener) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        ArrayList<LocalMedia> data = new ArrayList<>(urls.size());
        for (String url : urls) {
            if (url == null || url.length() == 0) {
                continue;
            }
            LocalMedia media = LocalMedia.generateLocalMedia(url, "image/jpeg");
            if (media == null) {
                media = new LocalMedia();
                media.setPath(url);
                media.setMimeType("image/jpeg");
            }
            data.add(media);
        }
        if (data.isEmpty()) {
            return;
        }
        // 删除时同步回 urls（若为可修改 List）
        OnPreviewDeleteListener wrap = deletePosition -> {
            if (urls instanceof ArrayList && deletePosition >= 0 && deletePosition < urls.size()) {
                urls.remove(deletePosition);
            }
            if (deleteListener != null) {
                deleteListener.onDelete(deletePosition);
            }
        };
        int safePos = Math.max(0, Math.min(position, data.size() - 1));
        preview(context, safePos, data, showDelete, showDownload, wrap);
    }

    // 进入相册
    public static void getMedia(Context context, int chooseMode, boolean customCamera, boolean crop, int maxNum, MeOnResultCallbackListener callbackListener) {
        PictureSelector.create(context)
                .openGallery(chooseMode)
                .setSelectorUIStyle(getStyle(context))//UI
                .setImageEngine(GlideEngine.createGlideEngine())//glide
                .setCropEngine(crop ? new ImageCropEngine() : null)//裁剪
                .setCompressEngine(new ImageCompressEngine())//压缩
                .setSandboxFileEngine(new MeSandboxFileEngine())//自定义沙盒路径
                .setCameraInterceptListener(customCamera ? new MeOnCameraInterceptListener() : null)//自定义相机
                .setSelectLimitTipsListener(new MeOnSelectLimitTipsListener())//多选提示
                .setEditMediaInterceptListener(new MeOnMediaEditInterceptListener())//自定义编辑
                //.setExtendLoaderEngine(getExtendLoaderEngine())
                //.setInjectLayoutResourceListener(new MeOnInjectLayoutResourceListener())//注入view
                .setSelectionMode(maxNum > 1 ? SelectModeConfig.MULTIPLE : SelectModeConfig.SINGLE)
                .setLanguage(LanguageConfig.CHINESE)
                .setOutputCameraDir(chooseMode == SelectMimeType.ofAudio() ? Util.getSandboxAudioOutputPath() : Util.getSandboxCameraOutputPath())
                .setOutputAudioDir(chooseMode == SelectMimeType.ofAudio() ? Util.getSandboxAudioOutputPath() : Util.getSandboxCameraOutputPath())
                .setQuerySandboxDir(chooseMode == SelectMimeType.ofAudio() ? Util.getSandboxAudioOutputPath() : Util.getSandboxCameraOutputPath())
                .isDisplayTimeAxis(true)//显示资源时间轴
                .isOnlyObtainSandboxDir(false)//查询指定目录
                .isPageStrategy(true)//分页模式
                .isOriginalControl(false)//是否开启原图功能
                .isDisplayCamera(true)//展示相机
                .isOpenClickSound(true)//true
                .isFastSlidingSelect(true)//滑动选择
                //.setOutputCameraImageFileName("luck.jpeg")
                //.setOutputCameraVideoFileName("luck.mp4")
                .isWithSelectVideoImage(true)//图片视频同选
                .isPreviewFullScreenMode(true)//全屏预览(点击)
                .isPreviewZoomEffect(true)//预览缩放效果
                .isPreviewImage(true)//是否预览图片
                .isPreviewVideo(true)//是否预览视频
                .isPreviewAudio(true)//是否预览音频
                //.setQueryOnlyMimeType(PictureMimeType.ofGIF())
                .isMaxSelectEnabledMask(true)//是否显示蒙层(达到最大可选数量)
                .isDirectReturnSingle(true)//单选模式直接返回
                .setMaxSelectNum(maxNum)
                .setRecyclerAnimationMode(getAnimRecyclerView())
                .isGif(false)//过滤gif
                .isWebp(false)//过滤Webp
                .isBmp(false)//过滤Bmp
                .isEmptyResultReturn(true)//支持未选择返回
                //.setSelectedData(mAdapter.getData());
                //forSelectResult(selectionModel);
                //.setVideoQuality(1)//0模糊,1清晰
                .setFilterMaxFileSize(chooseMode == SelectMimeType.ofVideo() ? 100 * 1024 : 5 * 1024)//过滤最大文件(KB),视频100MB,图片5MB
                .forResult(callbackListener);
    }

    /**
     * 如果要获取缓存大小,到内部查看源码即可.
     *
     * @param context
     */
    public static void clearCache(Context context) {
        //老版本
        //包括裁剪和压缩后的缓存，要在上传成功后调用，type 指的是图片or视频缓存取决于你设置的ofImage或ofVideo 注意：需要系统sd卡权限
        //PictureFileUtils.deleteCacheDirFile(context,type);
        // 清除所有缓存 例如：压缩、裁剪、视频、音频所生成的临时文件
        //PictureFileUtils.deleteAllCacheDirFile(context);

        //当前版本
        PictureCacheManager.deleteAllCacheDirFile(context);
    }
}
