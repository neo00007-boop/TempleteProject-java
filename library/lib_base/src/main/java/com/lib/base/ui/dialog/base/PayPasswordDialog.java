package com.lib.base.ui.dialog.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.QuickViewHolder;
import com.lib.base.R;
import com.lib.base.ui.dialog.inject.SingleClick;
import com.hjq.shape.view.textview.PasswordView;
import com.lib.base.util.Arrays;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *   @author: Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/12/2
 *    desc   : 支付密码对话框
 */
public final class PayPasswordDialog {

    public static final class Builder
            extends BaseDialog.Builder<Builder> {

        /** 输入键盘文本 */
        private static final String[] KEYBOARD_TEXT = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", ""};

        @Nullable
        private OnListener mListener;
        private boolean mAutoDismiss = true;
        private final LinkedList<String> mRecordList = new LinkedList<>();

        private final TextView mTitleView;
        private final ImageView mCloseView;

        private final TextView mSubTitleView;
        private final TextView mMoneyView;

        private final PasswordView mPasswordView;
        private final RecyclerView mRecyclerView;
        private final KeyboardAdapter mAdapter;

        public Builder(Context context) {
            super(context);
            setContentView(R.layout.pay_password_dialog);
            setCancelable(false);

            mTitleView = findViewById(R.id.tv_pay_title);
            mCloseView = findViewById(R.id.iv_pay_close);
            mSubTitleView = findViewById(R.id.tv_pay_sub_title);
            mMoneyView = findViewById(R.id.tv_pay_money);
            mPasswordView = findViewById(R.id.pw_pay_view);
            mRecyclerView = findViewById(R.id.rv_pay_list);
            setOnClickListener(mCloseView);

            mRecyclerView.setLayoutManager(new GridLayoutManager(context, 3));
            mAdapter = new KeyboardAdapter(getCtx());
            mAdapter.submitList(Arrays.asList(KEYBOARD_TEXT));
            mAdapter.setOnItemClickListener((adapter, view, position) -> onKeyboardItemClick(position));
            mRecyclerView.setAdapter(mAdapter);
        }

        public Builder setTitle(@StringRes int id) {
            return setTitle(getString(id));
        }

        public Builder setTitle(CharSequence title) {
            mTitleView.setText(title);
            return this;
        }

        public Builder setSubTitle(@StringRes int id) {
            return setSubTitle(getString(id));
        }

        public Builder setSubTitle(CharSequence subTitle) {
            mSubTitleView.setText(subTitle);
            return this;
        }

        public Builder setMoney(@StringRes int id) {
            return setMoney(getString(id));
        }

        public Builder setMoney(CharSequence money) {
            mMoneyView.setText(money);
            return this;
        }

        public Builder setAutoDismiss(boolean dismiss) {
            mAutoDismiss = dismiss;
            return this;
        }

        public Builder setListener(OnListener listener) {
            mListener = listener;
            return this;
        }

        private void onKeyboardItemClick(int position) {
            switch (mAdapter.getItemViewType(position)) {
                case KeyboardAdapter.TYPE_DELETE:
                    // 点击回退按钮删除
                    if (mRecordList.size() != 0) {
                        mRecordList.removeLast();
                    }
                    break;
                case KeyboardAdapter.TYPE_EMPTY:
                    // 点击空白的地方不做任何操作
                    break;
                default:
                    // 判断密码是否已经输入完毕
                    if (mRecordList.size() < PasswordView.PASSWORD_COUNT) {
                        // 点击数字，显示在密码行
                        mRecordList.add(KEYBOARD_TEXT[position]);
                    }

                    // 判断密码是否已经输入完毕
                    if (mRecordList.size() == PasswordView.PASSWORD_COUNT) {
                        postDelayed(() -> {
                            if (mAutoDismiss) {
                                dismiss();
                            }
                            // 获取输入的支付密码
                            StringBuilder password = new StringBuilder();
                            for (String s : mRecordList) {
                                password.append(s);
                            }
                            if (mListener == null) {
                                return;
                            }
                            mListener.onCompleted(getDialog(), password.toString());
                        }, 300);
                    }
                    break;
            }
            mPasswordView.setPassWordLength(mRecordList.size());
        }

        @SingleClick
        @Override
        public void onClick(View view) {
            if (view == mCloseView) {
                if (mAutoDismiss) {
                    dismiss();
                }

                if (mListener == null) {
                    return;
                }
                mListener.onCancel(getDialog());
            }
        }
    }

    private static final class KeyboardAdapter extends BaseQuickAdapter<String, QuickViewHolder> {

        /** 数字按钮条目 */
        private static final int TYPE_NORMAL = 0;
        /** 删除按钮条目 */
        private static final int TYPE_DELETE = 1;
        /** 空按钮条目 */
        private static final int TYPE_EMPTY = 2;

        private KeyboardAdapter(Context context) {
            super();
        }

        @Override
        protected int getItemViewType(int position, @NonNull List<? extends String> list) {
            switch (position) {
                case 9:
                    return TYPE_EMPTY;
                case 11:
                    return TYPE_DELETE;
                default:
                    return TYPE_NORMAL;
            }
        }

        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_DELETE:
                    return new QuickViewHolder(R.layout.pay_password_delete_item, parent);
                case TYPE_EMPTY:
                    return new QuickViewHolder(R.layout.pay_password_empty_item, parent);
                default:
                    return new ViewHolder(parent);
            }
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable String item) {
            if (holder instanceof ViewHolder) {
                ((ViewHolder) holder).onBind(item);
            }
        }

        private static final class ViewHolder extends QuickViewHolder {

            private final TextView mTextView;

            private ViewHolder(@NonNull ViewGroup parent) {
                super(R.layout.pay_password_normal_item, parent);
                mTextView = (TextView) itemView;
            }

            void onBind(@Nullable String item) {
                mTextView.setText(item);
            }
        }
    }

    public interface OnListener {

        /**
         * 输入完成时回调
         *
         * @param password      输入的密码
         */
        void onCompleted(BaseDialog dialog, String password);

        /**
         * 点击取消时回调
         */
        default void onCancel(BaseDialog dialog) {}
    }
}
