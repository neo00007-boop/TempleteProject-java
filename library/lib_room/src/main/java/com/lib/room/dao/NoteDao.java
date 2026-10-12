package com.lib.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lib.room.entity.Note;

import java.util.List;

/**
 * 增删改和单条查询是同步方法，Room 不允许在主线程调用，用 {@link com.lib.room.repository.NoteRepository}。
 * 返回 LiveData 的查询 Room 自己切线程，表数据一变就会重新推送。
 */
@Dao
public interface NoteDao {

    /** 返回新行的 id；主键已存在时整行替换 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    /** 按主键更新，返回受影响行数 */
    @Update
    int update(Note note);

    /** 按主键删除，返回受影响行数 */
    @Delete
    int delete(Note note);

    @Query("DELETE FROM note")
    int deleteAll();

    @Query("SELECT * FROM note WHERE id = :id")
    Note getById(long id);

    @Query("SELECT * FROM note ORDER BY create_time DESC, id DESC")
    LiveData<List<Note>> observeAll();

    /** 标题模糊搜索，title 上有索引 */
    @Query("SELECT * FROM note WHERE title LIKE '%' || :keyword || '%' ORDER BY create_time DESC, id DESC")
    LiveData<List<Note>> search(String keyword);
}
