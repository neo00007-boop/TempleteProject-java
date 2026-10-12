package com.lib.room.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * 数据库版本迁移。改表结构的步骤：
 * 1. 改 @Entity（加字段、加表、加索引）
 * 2. AppDatabase.VERSION +1，编译一次，schemas/ 下会多一个新版本的 json，提交到 git
 * 3. 在这里写 MIGRATION_旧_新，SQL 照着新 json 里的 createSql 写，并加进 {@link #ALL}
 * 4. 在 androidTest 的 MigrationTest 里补一条从旧版本升上来的用例
 * 已发布的迁移不要改，只能新增。
 */
public final class Migrations {

    /** v1 → v2：note 加 create_time，老数据填 0 */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `note` ADD COLUMN `create_time` INTEGER NOT NULL DEFAULT 0");
        }
    };

    /** v2 → v3：note.title 加索引，加快标题搜索 */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_title` ON `note` (`title`)");
        }
    };

    /** 全部迁移。Room 会自动串起来，比如 v1 的用户直接升到最新版本 */
    public static final Migration[] ALL = {MIGRATION_1_2, MIGRATION_2_3};

    private Migrations() {
    }
}
