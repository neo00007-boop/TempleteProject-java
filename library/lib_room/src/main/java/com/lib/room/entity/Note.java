package com.lib.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 示例表 note。字段、索引的每次改动都要同步改 {@link com.lib.room.AppDatabase#VERSION} 并补迁移，
 * 见 {@link com.lib.room.migration.Migrations}。
 */
@Entity(tableName = "note", indices = {@Index(value = "title")})
public class Note {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "content")
    public String content;

    /** v2 新增。defaultValue 要和迁移里 ALTER TABLE 的 DEFAULT 一致，否则 Room 校验表结构不通过 */
    @ColumnInfo(name = "create_time", defaultValue = "0")
    public long createTime;

    public Note() {
    }

    @Ignore
    public Note(@NonNull String title, String content) {
        this.title = title;
        this.content = content;
    }
}
