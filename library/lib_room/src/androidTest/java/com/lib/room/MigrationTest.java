package com.lib.room;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.lib.room.migration.Migrations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * 按 schemas/ 里导出的旧版本表结构建库、写入老数据，跑迁移后由 Room 校验表结构和最新版本一致。
 * 需要连设备：./gradlew :library:lib_room:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class MigrationTest {
    private static final String DB_NAME = "migration-test";

    @Rule
    public MigrationTestHelper helper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(), AppDatabase.class);

    @Test
    public void migrate1ToLatest() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(DB_NAME, 1);
        db.execSQL("INSERT INTO note (title, content) VALUES ('v1 数据', 'v1 写入')");
        db.close();

        db = helper.runMigrationsAndValidate(DB_NAME, AppDatabase.VERSION, true, Migrations.ALL);
        try (Cursor cursor = db.query("SELECT title, content, create_time FROM note")) {
            assertTrue(cursor.moveToFirst());
            assertEquals("v1 数据", cursor.getString(0));
            assertEquals("v1 写入", cursor.getString(1));
            assertEquals(0, cursor.getLong(2));
        }
    }

    @Test
    public void migrate2ToLatest() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(DB_NAME, 2);
        db.execSQL("INSERT INTO note (title, content, create_time) VALUES ('v2 数据', 'v2 写入', 100)");
        db.close();

        db = helper.runMigrationsAndValidate(DB_NAME, AppDatabase.VERSION, true, Migrations.ALL);
        try (Cursor cursor = db.query("SELECT create_time FROM note WHERE title = 'v2 数据'")) {
            assertTrue(cursor.moveToFirst());
            assertEquals(100, cursor.getLong(0));
        }
    }
}
