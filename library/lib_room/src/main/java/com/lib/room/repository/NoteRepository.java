package com.lib.room.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.lib.room.AppDatabase;
import com.lib.room.dao.NoteDao;
import com.lib.room.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * note 表的读写入口：写操作放到单线程里按顺序执行，结果回调到主线程；列表用 LiveData 观察。
 */
public class NoteRepository {

    public interface Callback<T> {
        void onResult(T result);
    }

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private final NoteDao dao;

    public NoteRepository(@NonNull Context context) {
        dao = AppDatabase.get(context).noteDao();
    }

    public LiveData<List<Note>> observeAll() {
        return dao.observeAll();
    }

    public LiveData<List<Note>> search(@NonNull String keyword) {
        return dao.search(keyword);
    }

    /** 回调新行 id */
    public void insert(@NonNull Note note, @Nullable Callback<Long> callback) {
        IO.execute(() -> {
            if (note.createTime == 0) {
                note.createTime = System.currentTimeMillis();
            }
            post(callback, dao.insert(note));
        });
    }

    /** 回调受影响行数，0 表示这条已经不存在 */
    public void update(@NonNull Note note, @Nullable Callback<Integer> callback) {
        IO.execute(() -> post(callback, dao.update(note)));
    }

    public void delete(@NonNull Note note, @Nullable Callback<Integer> callback) {
        IO.execute(() -> post(callback, dao.delete(note)));
    }

    public void deleteAll(@Nullable Callback<Integer> callback) {
        IO.execute(() -> post(callback, dao.deleteAll()));
    }

    public void getById(long id, @NonNull Callback<Note> callback) {
        IO.execute(() -> post(callback, dao.getById(id)));
    }

    private static <T> void post(@Nullable Callback<T> callback, T result) {
        if (callback != null) {
            MAIN.post(() -> callback.onResult(result));
        }
    }
}
