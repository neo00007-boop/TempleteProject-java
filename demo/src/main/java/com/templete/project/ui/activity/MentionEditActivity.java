package com.templete.project.ui.activity;

import android.view.View;
import android.widget.TextView;

import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.databinding.ActivityMentionEditBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Telegram 群底部输入的演示页。选人行为在 {@link MentionEditText} 里。
 */
public class MentionEditActivity extends BaseActivity<ActivityMentionEditBinding> {

    @Override
    protected ActivityMentionEditBinding viewBinding() {
        return ActivityMentionEditBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("聊天群@群员");
    }

    @Override
    public void initView() {
        mViewBinding.input.bindMemberList(mViewBinding.memberList);
        mViewBinding.input.setMembers(demoMembers());
        mViewBinding.input.setMentionColor(getResources().getColor(R.color.cl_1692DB));
        mViewBinding.input.setMemberColor(getResources().getColor(R.color.cl_1692DB));
        mViewBinding.input.setAvatarColor(getResources().getColor(R.color.cl_1692DB));
    }

    @Override
    public void initEvent() {
        mViewBinding.send.setOnClickListener(v -> send());
    }

    @Override
    public void initData() {
    }

    private List<MentionEditText.Member> demoMembers() {
        List<MentionEditText.Member> members = new ArrayList<>();
        members.add(new MentionEditText.Member("999", "张@伟@", null, null));
        members.add(new MentionEditText.Member("1000", "张@伟", null, null));
        members.add(new MentionEditText.Member("1001", "张伟", "zhangwei", null));
        members.add(new MentionEditText.Member("1002", "张张", null, null));
        members.add(new MentionEditText.Member("1003", "伟伟", "weiwei", null));
        members.add(new MentionEditText.Member("1004", "张伟伟", null, null));
        members.add(new MentionEditText.Member("1005", "李娜", "lina", null));
        members.add(new MentionEditText.Member("1006", "李李", null, null));
        members.add(new MentionEditText.Member("1007", "娜娜", "nana", null));
        members.add(new MentionEditText.Member("1008", "李娜娜", null, null));
        members.add(new MentionEditText.Member("1009", "王强", "wangqiang", null));
        members.add(new MentionEditText.Member("1010", "王王", null, null));
        members.add(new MentionEditText.Member("1011", "强强", "qiangqiang", null));
        members.add(new MentionEditText.Member("1012", "王强强", null, null));
        members.add(new MentionEditText.Member("1013", "张 伟", null, null));
        members.add(new MentionEditText.Member("1014", "客服", null, null));
        members.add(new MentionEditText.Member("1015", "@客服", null, null));
        members.add(new MentionEditText.Member("1016", "@客@服", null, null));
        members.add(new MentionEditText.Member("1017", "@@客@服", null, null));
        members.add(new MentionEditText.Member("1018", "", null, null));
        return members;
    }

    private void send() {
        // 提交这份 message。演示里没有真接口，回显用的就是这一份。
        MentionEditText.Message message = mViewBinding.input.exportMessage();
        if (message.text.trim().isEmpty()) {
            return;
        }
        TextView bubble = (TextView) getLayoutInflater().inflate(
                R.layout.item_mention_bubble, mViewBinding.messageBox, false);
        mViewBinding.input.show(bubble, message, id -> toast("点击了 " + id));
        mViewBinding.messageBox.addView(bubble);
        mViewBinding.input.setText("");
        mViewBinding.messageScroll.post(() -> mViewBinding.messageScroll.fullScroll(View.FOCUS_DOWN));
    }
}
