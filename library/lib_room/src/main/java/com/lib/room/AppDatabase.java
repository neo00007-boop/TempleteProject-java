package com.lib.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.lib.room.dao.NoteDao;
import com.lib.room.entity.Note;
import com.lib.room.migration.Migrations;

/**
 * 全局唯一的 Room 数据库。新增表：加到 entities，加一个 Dao 方法，再按 {@link Migrations} 的步骤升版本。
 */
@Database(entities = {Note.class}, version = AppDatabase.VERSION)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * 当前数据库版本。只能往上加，改了表结构就 +1。
     * v1 建 note 表；v2 note 加 create_time；v3 note.title 加索引
     */
    public static final int VERSION = 3;
    private static final String NAME = "app_room.db";

    private static volatile AppDatabase sInstance;

    public abstract NoteDao noteDao();

    public static AppDatabase get(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    // 不加 fallbackToDestructiveMigration：漏写迁移时直接崩溃暴露问题，而不是悄悄清空用户数据
                    sInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, NAME)
                            .addMigrations(Migrations.ALL)
                            .build();
                }
            }
        }
        return sInstance;
    }
}
