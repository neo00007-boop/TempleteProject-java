package com.templete.project.ui.activity;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.shape.view.ShapeEditText;
import com.lib.base.util.txt.label.TxtUtil;
import com.lib.base.util.txt.superSoan.SpanData;
import com.templete.project.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Telegram 风格的 @ 人员输入框。人员通过 {@link #setMembers(List)} 传入，
 * 关联列表通过 {@link #bindMemberList(RecyclerView)} 接上。
 */
public class MentionEditText extends ShapeEditText {

    private static final int DEFAULT_MENTION_COLOR = 0xFF2481CC;
    private static final int DEFAULT_MEMBER_TEXT_COLOR = 0xFF222222;
    private static final int DEFAULT_AVATAR_COLOR = 0xFF2481CC;
    private int mentionColor = DEFAULT_MENTION_COLOR;
    private int memberTextColor = DEFAULT_MEMBER_TEXT_COLOR;
    private int avatarColor = DEFAULT_AVATAR_COLOR;
    private RecyclerView memberList;
    private final List<Member> members = new ArrayList<>();
    private final List<Member> shown = new ArrayList<>();
    private boolean applying;
    private long lastMentionDeleteMs;
    private int editStart;
    private int editBefore;
    private int editCount;
    private String shownQuery;

    public MentionEditText(Context context) {
        super(context);
        init(null);
    }

    public MentionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MentionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            android.content.res.TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MentionEditText);
            mentionColor = array.getColor(R.styleable.MentionEditText_mentionTextColor, mentionColor);
            memberTextColor = array.getColor(R.styleable.MentionEditText_mentionMemberColor, memberTextColor);
            avatarColor = array.getColor(R.styleable.MentionEditText_mentionAvatarColor, avatarColor);
            array.recycle();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTextCursorDrawable(R.drawable.mention_cursor);
        }
        setCursorVisible(true);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editStart = start;
                editBefore = before;
                editCount = count;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (applying) {
                    return;
                }
                if (commitExactBySpace(s)) {
                    return;
                }
                dropBrokenMentions(s);
                padSpacesBesideMentions(s);
                syncMemberList();
            }
        });
    }

    /** 设置正文里 @ 标签的颜色。不调用时使用默认色，布局里的 mentionTextColor 会先写入。 */
    public void setMentionColor(int color) {
        mentionColor = color;
        invalidate();
    }

    /** 设置关联列表里名字的颜色。不调用时使用默认色，布局里的 mentionMemberColor 会先写入。 */
    public void setMemberColor(int color) {
        memberTextColor = color;
        refreshMemberList();
    }

    /** 设置没有头像时圆底的颜色。不调用时使用默认色，布局里的 mentionAvatarColor 会先写入。 */
    public void setAvatarColor(int color) {
        avatarColor = color;
        refreshMemberList();
    }

    private void refreshMemberList() {
        if (memberList != null && memberList.getAdapter() != null) {
            memberList.getAdapter().notifyDataSetChanged();
        }
    }

    /** 传入可选人员，之后输入 @ 会按这份数据过滤。 */
    public void setMembers(List<Member> data) {
        members.clear();
        if (data != null) {
            members.addAll(data);
        }
        syncMemberList();
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** 关联列表由页面摆放，行为由输入框接管。 */
    public void bindMemberList(RecyclerView list) {
        memberList = list;
        if (list == null) {
            return;
        }
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setAdapter(new MemberAdapter());
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        syncMemberList();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && deleteMentionBeforeCursor()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection base = super.onCreateInputConnection(outAttrs);
        if (base == null) {
            return null;
        }
        return new InputConnectionWrapper(base, true) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                if (beforeLength == 1 && afterLength == 0 && deleteMentionBeforeCursor()) {
                    return true;
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }

            @Override
            public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
                if (beforeLength == 1 && afterLength == 0 && deleteMentionBeforeCursor()) {
                    return true;
                }
                return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
            }
        };
    }

    private void syncMemberList() {
        if (applying || memberList == null || memberList.getAdapter() == null) {
            return;
        }
        Editable text = getText();
        int cursor = getSelectionEnd();
        Token token = findToken(text, cursor);
        if (token == null) {
            memberList.setVisibility(View.GONE);
            return;
        }
        boolean wasHidden = memberList.getVisibility() != View.VISIBLE;
        String query = token.query;
        boolean queryChanged = !query.equals(shownQuery);
        fillShown(query);
        if (shown.isEmpty()) {
            memberList.setVisibility(View.GONE);
            shownQuery = null;
            return;
        }
        memberList.setVisibility(View.VISIBLE);
        memberList.getAdapter().notifyDataSetChanged();
        if (wasHidden || queryChanged) {
            memberList.post(() -> memberList.scrollToPosition(0));
        }
        shownQuery = query;
    }

    private void fillShown(String query) {
        shown.clear();
        for (Member member : members) {
            if (matches(member, query)) {
                shown.add(member);
            }
        }
    }

    private boolean matches(Member member, String query) {
        String key = query.toLowerCase(Locale.ROOT);
        String name = member.name.toLowerCase(Locale.ROOT);
        if (key.isEmpty() || name.startsWith(key) || member.name.contains(query)) {
            return true;
        }
        return !TextUtils.isEmpty(member.username)
                && member.username.toLowerCase(Locale.ROOT).startsWith(key);
    }

    /** 从紧挨着的人员 @ 看到这次输入的结尾，这段还能配到人。 */
    private boolean extendsMention(Editable text, int mentionEnd, int insertEnd) {
        MentionSpan[] spans = text.getSpans(Math.max(0, mentionEnd - 1), mentionEnd, MentionSpan.class);
        for (MentionSpan span : spans) {
            int start = text.getSpanStart(span);
            if (text.getSpanEnd(span) != mentionEnd || start < 0 || start >= insertEnd) {
                continue;
            }
            if (text.charAt(start) != '@') {
                continue;
            }
            if (hasMatch(text.subSequence(start + 1, insertEnd).toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMatch(String query) {
        for (Member member : members) {
            if (matches(member, query)) {
                return true;
            }
        }
        return false;
    }

    private void insertMember(Member member) {
        Editable text = getText();
        if (text == null) {
            return;
        }
        int cursor = getSelectionEnd();
        Token token = findToken(text, cursor);
        if (token == null) {
            return;
        }
        applyMention(text, token, member, token.end);
    }

    private boolean commitExactBySpace(Editable text) {
        if (editCount != 1 || editBefore != 0 || editStart < 0 || editStart >= text.length()) {
            return false;
        }
        if (!Character.isWhitespace(text.charAt(editStart))) {
            return false;
        }
        Token token = findToken(text, editStart);
        if (token == null || token.end != editStart || token.query.isEmpty()) {
            return false;
        }
        Member member = findExact(token.query);
        if (member == null) {
            return false;
        }
        applyMention(text, token, member, editStart + 1);
        return true;
    }

    private void padSpacesBesideMentions(Editable text) {
        if (editCount <= 0 || editStart < 0 || editStart > text.length()) {
            return;
        }
        int insertEnd = Math.min(text.length(), editStart + editCount);
        if (insertEnd <= editStart) {
            return;
        }
        // 紧挨着已选人员往后续写：续上后还能配到人，就留给关联列表，不在字前面补空格。
        // 配不到人，再把空格补在这个字前面。
        boolean lead = editStart > 0
                && mentionEndsAt(text, editStart)
                && !Character.isWhitespace(text.charAt(editStart))
                && !extendsMention(text, editStart, insertEnd);
        boolean trail = insertEnd < text.length()
                && mentionStartsAt(text, insertEnd)
                && !Character.isWhitespace(text.charAt(insertEnd - 1));
        if (!lead && !trail) {
            return;
        }
        int cursor = getSelectionEnd();
        applying = true;
        if (lead) {
            text.insert(editStart, " ");
            if (cursor >= editStart) {
                cursor++;
            }
            insertEnd++;
        }
        if (trail && insertEnd <= text.length()) {
            text.insert(insertEnd, " ");
            if (cursor > insertEnd) {
                cursor++;
            }
        }
        applying = false;
        int safeCursor = Math.max(0, Math.min(cursor, text.length()));
        setSelection(safeCursor);
    }

    private boolean mentionEndsAt(Editable text, int index) {
        MentionSpan[] spans = text.getSpans(Math.max(0, index - 1), index, MentionSpan.class);
        for (MentionSpan span : spans) {
            if (text.getSpanEnd(span) == index) {
                return true;
            }
        }
        return false;
    }

    private boolean mentionStartsAt(Editable text, int index) {
        if (index < 0 || index >= text.length()) {
            return false;
        }
        int to = Math.min(text.length(), index + 1);
        MentionSpan[] spans = text.getSpans(index, to, MentionSpan.class);
        for (MentionSpan span : spans) {
            if (text.getSpanStart(span) == index) {
                return true;
            }
        }
        return false;
    }

    private Member findExact(String query) {
        for (Member member : members) {
            if (member.name.equals(query)) {
                return member;
            }
        }
        for (Member member : members) {
            if (!TextUtils.isEmpty(member.username) && member.username.equalsIgnoreCase(query)) {
                return member;
            }
        }
        return null;
    }

    private void applyMention(Editable text, Token token, Member member, int replaceEnd) {
        String body = "@" + member.name;
        boolean consumedSpace = replaceEnd > token.end;
        boolean needSpace = !consumedSpace
                && (token.end >= text.length() || !Character.isWhitespace(text.charAt(token.end)));
        String prefix = token.spaceBefore ? " " : "";
        String insert = prefix + ((needSpace || consumedSpace) ? body + " " : body);
        applying = true;
        MentionSpan[] oldSpans = text.getSpans(token.start, Math.max(token.start, replaceEnd), MentionSpan.class);
        for (MentionSpan span : oldSpans) {
            text.removeSpan(span);
        }
        text.replace(token.start, replaceEnd, insert);
        int bodyStart = token.start + prefix.length();
        text.setSpan(new MentionSpan(member.id, member.name), bodyStart, bodyStart + body.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        applying = false;
        int caret = bodyStart + body.length();
        if (needSpace) {
            caret = token.start + insert.length();
        } else if (caret < text.length() && Character.isWhitespace(text.charAt(caret))) {
            caret = caret + 1;
        }
        setSelection(Math.min(caret, text.length()));
        if (memberList != null) {
            memberList.setVisibility(View.GONE);
        }
    }

    private boolean deleteMentionBeforeCursor() {
        long now = SystemClock.uptimeMillis();
        if (now - lastMentionDeleteMs < 50) {
            return true;
        }
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start != end || start <= 0) {
            return false;
        }
        Editable text = getText();
        if (text == null) {
            return false;
        }
        MentionSpan[] spans = text.getSpans(start - 1, start, MentionSpan.class);
        for (MentionSpan span : spans) {
            int spanEnd = text.getSpanEnd(span);
            if (spanEnd != start) {
                continue;
            }
            applying = true;
            text.delete(text.getSpanStart(span), spanEnd);
            applying = false;
            lastMentionDeleteMs = now;
            syncMemberList();
            return true;
        }
        return false;
    }

    private void dropBrokenMentions(Editable text) {
        MentionSpan[] spans = text.getSpans(0, text.length(), MentionSpan.class);
        for (MentionSpan span : spans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);
            if (start < 0 || end < start || !("@" + span.name).contentEquals(text.subSequence(start, end))) {
                text.removeSpan(span);
            }
        }
    }

    private Token findToken(Editable text, int cursor) {
        if (text == null || cursor < 0) {
            return null;
        }
        if (cursor > text.length()) {
            cursor = text.length();
        }
        // 光标前这一段连续非空白里可能有多个 @（名字里也带 @）。
        // 优先用「查询还能配到人」且最长的那个，避免 @张@ 被拆成第二个 @。
        // 都配不到时退回最靠近光标的 @，光秃秃的 @ 仍能拉起全部人员。
        int runStart = cursor;
        for (int i = cursor - 1; i >= 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                break;
            }
            runStart = i;
        }
        if (runStart >= cursor) {
            return null;
        }
        int bestAt = -1;
        int bestLen = -1;
        int lastAt = -1;
        for (int i = runStart; i < cursor; i++) {
            if (text.charAt(i) != '@') {
                continue;
            }
            lastAt = i;
            String query = text.subSequence(i + 1, cursor).toString();
            if (!hasMatch(query)) {
                continue;
            }
            if (query.length() > bestLen) {
                bestLen = query.length();
                bestAt = i;
            }
        }
        if (bestAt < 0) {
            bestAt = lastAt;
        }
        if (bestAt < 0) {
            return null;
        }
        boolean spaceBefore = bestAt > 0 && !Character.isWhitespace(text.charAt(bestAt - 1));
        return new Token(bestAt, cursor, text.subSequence(bestAt + 1, cursor).toString(), spaceBefore);
    }

    /**
     * 取出要提交的内容。
     * <p>
     * 不要把 {@link #getText()} 交给接口。它上面的颜色只在当前进程里有效，序列化之后人员是谁会丢。
     * 交给接口的是 {@link Message}：
     * <ul>
     * <li>{@link Message#text} 纯文本，例如「你好 @张伟 看一下」</li>
     * <li>{@link Message#mentions} 点选成功的人员。每条有 {@link Mention#id}、{@link Mention#offset}、{@link Mention#length}</li>
     * </ul>
     * offset、length 按 Java 的 char 下标计算，直接落在 text 上。范围只盖住「@名字」，后面那个空格不算进去。
     * 手打出来、没有点选成功的 @xx 不会进 mentions，只留在 text 里当普通字。
     */
    public Message exportMessage() {
        Editable text = getText();
        String plain = text == null ? "" : text.toString();
        List<Mention> mentions = new ArrayList<>();
        if (text != null) {
            MentionSpan[] spans = text.getSpans(0, text.length(), MentionSpan.class);
            for (MentionSpan span : spans) {
                int start = text.getSpanStart(span);
                int end = text.getSpanEnd(span);
                if (start < 0 || end <= start || end > plain.length()) {
                    continue;
                }
                if (!("@" + span.name).contentEquals(text.subSequence(start, end))) {
                    continue;
                }
                mentions.add(new Mention(span.id, start, end - start));
            }
            Collections.sort(mentions, (a, b) -> a.offset - b.offset);
        }
        return new Message(plain, mentions);
    }

    /**
     * 把后台返回的同一份数据画到 TextView 上。
     * <p>
     * 后台原样回 {@link Message#text}，另外回 {@link Message#mentions}（id、offset、length 与提交时同一套下标）。
     * 这里按 offset 把 text 切成普通段和人员段，再交给 {@link TxtUtil#setSuperLabel}：
     * 人员段用输入框的 mention 颜色，可点击，{@code click} 拿到的是这个人的 id。
     * 字号传 0，沿用 TextView 自己的字号，不加粗。没有人员时直接 setText，不挂点击。
     */
    public void show(@NonNull TextView tv, @NonNull Message message, @Nullable OnMentionClick click) {
        String text = message.text == null ? "" : message.text;
        List<Mention> mentions = new ArrayList<>();
        if (message.mentions != null) {
            mentions.addAll(message.mentions);
        }
        Collections.sort(mentions, (a, b) -> a.offset - b.offset);
        List<SpanData> parts = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        int cursor = 0;
        for (Mention mention : mentions) {
            int start = mention.offset;
            int end = mention.offset + mention.length;
            if (mention.length <= 0 || start < cursor || end > text.length()) {
                continue;
            }
            if (start > cursor) {
                parts.add(SpanData.build(text.substring(cursor, start), false));
            }
            parts.add(SpanData.build(text.substring(start, end), true));
            ids.add(mention.id);
            cursor = end;
        }
        if (cursor < text.length()) {
            parts.add(SpanData.build(text.substring(cursor), false));
        }
        if (ids.isEmpty()) {
            tv.setText(text);
            return;
        }
        String color = String.format(Locale.US, "#%08X", mentionColor);
        TxtUtil.setSuperLabel(tv, color, 0, true, false, index -> {
            int at = index - 1;
            if (click != null && at >= 0 && at < ids.size()) {
                click.onMentionClick(ids.get(at));
            }
        }, parts.toArray(new SpanData[0]));
    }

    /** 点中气泡里某一个 @ 时回调，参数是提交时带上的人员 id。 */
    public interface OnMentionClick {
        void onMentionClick(String id);
    }

    /**
     * 一次发送的正文。text 是完整纯文本，mentions 只列点选成功的人。
     */
    public static final class Message {
        public final String text;
        public final List<Mention> mentions;

        public Message(String text, List<Mention> mentions) {
            this.text = text == null ? "" : text;
            this.mentions = mentions == null ? Collections.emptyList() : mentions;
        }
    }

    /**
     * 正文里的一个已选人员。
     * offset 是这个「@名字」在 {@link Message#text} 里的起点，length 是它的字数，都不含后面的空格。
     */
    public static final class Mention {
        public final String id;
        public final int offset;
        public final int length;

        public Mention(String id, int offset, int length) {
            this.id = id;
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * 可选人员。id 是后台认的那一个，展示名可以重复，id 不能靠名字反查。
     * username 可空；有则关联列表显示第二行并参与过滤，没有则只显示名字并垂直居中。
     * avatar 有图时显示圆形头像，没有图时用首字和 {@link MentionEditText#setAvatarColor(int)} 的底色。
     */
    public static final class Member {
        public final String id;
        public final String name;
        public final String username;
        public final Drawable avatar;

        public Member(String id, String name, @Nullable String username, @Nullable Drawable avatar) {
            this.id = id;
            this.name = name;
            this.username = username == null ? "" : username;
            this.avatar = avatar;
        }
    }

    private static final class Token {
        final int start;
        final int end;
        final String query;
        final boolean spaceBefore;

        Token(int start, int end, String query, boolean spaceBefore) {
            this.start = start;
            this.end = end;
            this.query = query;
            this.spaceBefore = spaceBefore;
        }
    }

    private final class MentionSpan extends CharacterStyle {
        final String id;
        final String name;

        MentionSpan(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public void updateDrawState(android.text.TextPaint tp) {
            tp.setColor(mentionColor);
            tp.setUnderlineText(false);
        }
    }

    private final class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mention_member, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Member member = shown.get(position);
            holder.name.setText(member.name);
            holder.name.setTextColor(memberTextColor);
            if (TextUtils.isEmpty(member.username)) {
                holder.username.setVisibility(View.GONE);
            } else {
                holder.username.setVisibility(View.VISIBLE);
                holder.username.setText("@" + member.username);
            }
            if (member.avatar != null) {
                holder.avatar.setVisibility(View.GONE);
                holder.avatarImage.setVisibility(View.VISIBLE);
                holder.avatarImage.setImageDrawable(member.avatar);
            } else {
                holder.avatarImage.setVisibility(View.GONE);
                holder.avatarImage.setImageDrawable(null);
                holder.avatar.setVisibility(View.VISIBLE);
                holder.avatar.setText(member.name.substring(0, 1));
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(avatarColor);
                holder.avatar.setBackground(bg);
            }
            holder.itemView.setOnClickListener(v -> insertMember(member));
        }

        @Override
        public int getItemCount() {
            return shown.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final TextView avatar;
            final ImageView avatarImage;
            final TextView name;
            final TextView username;

            Holder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatar);
                avatarImage = itemView.findViewById(R.id.avatarImage);
                name = itemView.findViewById(R.id.name);
                username = itemView.findViewById(R.id.username);
                avatarImage.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, view.getWidth(), view.getHeight());
                    }
                });
                avatarImage.setClipToOutline(true);
            }
        }
    }
}
