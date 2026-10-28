package com.lib.base.util.upload;

import android.content.Context;

import com.lib.base.R;
import com.lib.base.util.DebugUtil;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.config.SelectLimitType;
import com.luck.picture.lib.interfaces.OnSelectLimitTipsListener;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.util.upload
 * @author      xwchen
 * Date         2022/2/9.
 */

class MeOnSelectLimitTipsListener implements OnSelectLimitTipsListener {

    @Override
    public boolean onSelectLimitTips(Context context, PictureSelectionConfig config, int limitType) {
        if (limitType == SelectLimitType.SELECT_MAX_VIDEO_SELECT_LIMIT) {
            DebugUtil.toast(context.getString(R.string.ps_message_video_max_num, String.valueOf(config.maxVideoSelectNum)));
            return true;
        }
        return false;
    }
}
