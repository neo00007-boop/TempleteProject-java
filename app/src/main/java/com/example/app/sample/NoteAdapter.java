package com.example.app.sample;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.app.R;
import com.lib.room.entity.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteAdapter extends BaseQuickAdapter<Note, BaseViewHolder> {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public NoteAdapter() {
        super(R.layout.room_sample_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, Note note) {
        holder.setText(R.id.tv_title, "#" + note.id + " " + note.title);
        holder.setText(R.id.tv_content, note.content);
        holder.setText(R.id.tv_time, note.createTime == 0
                ? "无创建时间（v2 之前写入的老数据）"
                : format.format(new Date(note.createTime)));
    }
}
