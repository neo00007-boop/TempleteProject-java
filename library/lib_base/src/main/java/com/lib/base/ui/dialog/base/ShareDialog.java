package com.lib.base.ui.dialog.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.R;
import com.lib.base.bean.Platform;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author: Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/03/23
 * desc   : 分享对话框
 */
public final class ShareDialog {

    public static final class Builder extends BaseDialog.Builder<Builder> {

        private final RecyclerView mRecyclerView;
        private final ShareAdapter mAdapter;

        //        private final ShareAction mShareAction;
        private final ShareBean mCopyLink;

//        @Nullable
//        private UmengShare.OnShareListener mListener;

        public Builder(Activity activity) {
            super(activity);

            setContentView(R.layout.share_dialog);

            final List<ShareBean> data = new ArrayList<>();
            data.add(new ShareBean(getDrawable(R.drawable.share_wechat_ic), getString(R.string.share_platform_wechat), Platform.WECHAT));
            data.add(new ShareBean(getDrawable(R.drawable.share_moment_ic), getString(R.string.share_platform_moment), Platform.CIRCLE));
            data.add(new ShareBean(getDrawable(R.drawable.share_qq_ic), getString(R.string.share_platform_qq), Platform.QQ));
            data.add(new ShareBean(getDrawable(R.drawable.share_qzone_ic), getString(R.string.share_platform_qzone), Platform.QZONE));

            mCopyLink = new ShareBean(getDrawable(R.drawable.share_link_ic), getString(R.string.share_platform_link), Platform.DEFAULTS);

            mAdapter = new ShareAdapter(activity);
            mAdapter.setList(data);
            mAdapter.setOnItemClickListener((adapter, view, position) -> {
               /* Platform platform = mAdapter.getItem(position).sharePlatform;
                if (platform != null) {
                    UmengClient.share(getActivity(), platform, mShareAction, mListener);
                } else {
                    if (mShareAction.getShareContent().getShareType() == ShareContent.WEB_STYLE) {
                        // 复制到剪贴板
                        getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("url", mShareAction.getShareContent().mMedia.toUrl()));
                        ToastUtils.show(R.string.share_platform_copy_hint);
                    }
                }
                dismiss();*/
            });

            mRecyclerView = findViewById(R.id.rv_share_list);
            mRecyclerView.setLayoutManager(new GridLayoutManager(activity, data.size()));
            mRecyclerView.setAdapter(mAdapter);

//            mShareAction = new ShareAction(activity);
        }

        /**
         * 分享网页链接：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu7F51u9875u94FEu63A51
         */
        /*public Builder setShareLink(UMWeb content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享图片：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu56FEu72473
         */
       /* public Builder setShareImage(UMImage content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享纯文本：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu7EAFu6587u672C5
         */
       /* public Builder setShareText(String content) {
            mShareAction.withText(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享音乐：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu97F3u4E507
         */
       /* public Builder setShareMusic(UMusic content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享视频：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu89C6u98916
         */
        /*public Builder setShareVideo(UMVideo content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享 Gif 表情：https://developer.umeng.com/docs/128606/detail/193883#h2--gif-8
         */
        /*public Builder setShareEmoji(UMEmoji content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享微信小程序：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu5C0Fu7A0Bu5E8F2
         */
        /*public Builder setShareMin(UMMin content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 分享 QQ 小程序：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu5C0Fu7A0Bu5E8F2
         */
        /*public Builder setShareMin(UMQQMini content) {
            mShareAction.withMedia(content);
            refreshShareOptions();
            return this;
        }*/

        /**
         * 设置回调监听器
         */
        /*public Builder setListener(UmengShare.OnShareListener listener) {
            mListener = listener;
            return this;
        }*/

        /**
         * 刷新分享选项
         */
        private void refreshShareOptions() {
            /*switch (mShareAction.getShareContent().getShareType()) {
                case ShareContent.WEB_STYLE:
                    if (!mAdapter.containsItem(mCopyLink)) {
                        mAdapter.add(mCopyLink);
                        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mAdapter.getItemCount()));
                    }
                    break;
                default:
                    if (mAdapter.containsItem(mCopyLink)) {
                        mAdapter.remove(mCopyLink);
                        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mAdapter.getItemCount()));
                    }
                    break;
            }*/
        }
    }

    private static class ShareAdapter extends BaseQuickAdapter<ShareBean, BaseViewHolder> {

        private ShareAdapter(Context context) {
            super(R.layout.share_item);
        }

        @Override
        protected void convert(@NonNull BaseViewHolder holder, ShareBean item) {
            if (item == null) {
                return;
            }
            holder.<ImageView>getView(R.id.iv_share_image).setImageDrawable(item.shareIcon);
            holder.setText(R.id.tv_share_text, item.shareName);
        }
    }

    private static class ShareBean {

        /**
         * 分享图标
         */
        final Drawable shareIcon;
        /**
         * 分享名称
         */
        final String shareName;
        /**
         * 分享平台
         */
        final int sharePlatform;

        private ShareBean(Drawable icon, String name, int platform) {
            shareIcon = icon;
            shareName = name;
            sharePlatform = platform;
        }
    }
}
