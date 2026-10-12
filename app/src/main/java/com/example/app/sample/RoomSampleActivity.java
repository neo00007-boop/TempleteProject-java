package com.example.app.sample;

import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.databinding.RoomSampleActivityBinding;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.room.AppDatabase;
import com.lib.room.entity.Note;
import com.lib.room.repository.NoteRepository;

import java.util.List;

/**
 * Room 用法示例：增删改查、按标题搜索。列表观察 LiveData，增删改之后不用手动刷新。
 * 表结构和版本迁移见 lib_room 的 AppDatabase、Migrations。
 */
public class RoomSampleActivity extends BaseActivity<RoomSampleActivityBinding> {

    private NoteRepository repository;
    private final NoteAdapter adapter = new NoteAdapter();
    private LiveData<List<Note>> source;
    @Nullable
    private Note selected;

    @Override
    protected RoomSampleActivityBinding viewBinding() {
        return RoomSampleActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("Room 数据库示例");
        repository = new NoteRepository(this);
    }

    @Override
    public void initView() {
        mViewBinding.tvVersion.setText("数据库版本 v" + AppDatabase.VERSION
                + "\nv1 建 note 表；v2 加 create_time 字段；v3 给 title 加索引");
        mViewBinding.rvNote.setLayoutManager(new LinearLayoutManager(this));
        mViewBinding.rvNote.setAdapter(adapter);
    }

    @Override
    public void initEvent() {
        adapter.setOnItemClickListener((a, view, position) -> select(adapter.getItem(position)));
        mViewBinding.btnAdd.setOnClickListener(v -> add());
        mViewBinding.btnUpdate.setOnClickListener(v -> update());
        mViewBinding.btnDelete.setOnClickListener(v -> delete());
        mViewBinding.btnClear.setOnClickListener(v -> repository.deleteAll(count -> {
            toast("已清空 " + count + " 条");
            select(null);
        }));
        mViewBinding.btnSearch.setOnClickListener(v -> observe(text(mViewBinding.etKeyword)));
    }

    @Override
    public void initData() {
        observe("");
    }

    private void add() {
        String title = text(mViewBinding.etTitle);
        if (title.isEmpty()) {
            toast("标题不能为空");
            return;
        }
        repository.insert(new Note(title, text(mViewBinding.etContent)), id -> toast("新增成功，id=" + id));
    }

    private void update() {
        if (selected == null) {
            toast("先点列表选中一条");
            return;
        }
        String title = text(mViewBinding.etTitle);
        if (title.isEmpty()) {
            toast("标题不能为空");
            return;
        }
        selected.title = title;
        selected.content = text(mViewBinding.etContent);
        repository.update(selected, count -> toast(count > 0 ? "已修改" : "这条已经不存在"));
    }

    private void delete() {
        if (selected == null) {
            toast("先点列表选中一条");
            return;
        }
        repository.delete(selected, count -> {
            toast(count > 0 ? "已删除" : "这条已经不存在");
            select(null);
        });
    }

    /** 换查询条件时先移除旧的观察，否则两个结果会交替刷新列表 */
    private void observe(String keyword) {
        if (source != null) {
            source.removeObservers(this);
        }
        source = keyword.isEmpty() ? repository.observeAll() : repository.search(keyword);
        source.observe(this, adapter::setList);
    }

    private void select(@Nullable Note note) {
        selected = note;
        if (note == null) {
            mViewBinding.tvSelected.setText("点列表里的一条选中，再修改或删除");
            return;
        }
        mViewBinding.tvSelected.setText("已选中 id=" + note.id);
        mViewBinding.etTitle.setText(note.title);
        mViewBinding.etContent.setText(note.content);
    }

    private static String text(EditText editText) {
        return editText.getText().toString().trim();
    }
}
