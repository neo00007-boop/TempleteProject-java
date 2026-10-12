# lib_room（Room 数据库）

Room 数据库：表结构、Dao、版本迁移。和 `lib_greendao` 并列，新功能建议用这里。用法示例见 app 的「Room 数据库示例」页（`app/.../sample/RoomSampleActivity`）。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| room-runtime | api | 数据库框架 |
| room-compiler | annotationProcessor | 根据注解生成 Dao 实现 |
| lifecycle-livedata | api | 查询返回 LiveData |
| room-testing | androidTestImplementation | 迁移测试 |

## 主要内容

```
com.lib.room
├── AppDatabase                 全局唯一的库 app_room.db，VERSION 是当前版本
├── entity/Note                 示例表 note
├── dao/NoteDao                 增删改查；返回 LiveData 的查询会在数据变化时自动推送
├── migration/Migrations        版本迁移
└── repository/NoteRepository   写操作切到子线程，结果回调主线程

schemas/                        每个版本的表结构 json，要提交到 git
src/androidTest/.../MigrationTest   验证旧版本能升级到最新
```

## 用法

```java
NoteRepository repository = new NoteRepository(context);

// 观察列表：Room 自己在子线程查，结果回到主线程；表一变就重新推送
repository.observeAll().observe(lifecycleOwner, list -> adapter.setList(list));

// 增删改：回调在主线程
repository.insert(new Note("标题", "内容"), id -> { });
repository.update(note, count -> { });
repository.delete(note, count -> { });
```

不要在主线程直接调 `NoteDao` 的同步方法（insert / update / delete / getById），Room 会直接报错。

## 版本迭代

当前是 v3：v1 建 note 表；v2 加 `create_time`；v3 给 `title` 加索引。改表结构的步骤：

1. 改 `@Entity`（加字段、加表、加索引）
2. `AppDatabase.VERSION` +1，编译一次，`schemas/` 下会多一个新版本的 json，提交到 git
3. 在 `Migrations` 里写 `MIGRATION_旧_新`，SQL 照着新 json 里的 `createSql` 写，并加进 `ALL`
4. 在 `MigrationTest` 里补一条从旧版本升级的用例，连设备运行：

```
./gradlew :library:lib_room:connectedDebugAndroidTest
```

## 注意

- 没开 `fallbackToDestructiveMigration`：漏写迁移时会直接崩溃暴露问题，不会悄悄清空用户数据
- 已发布的迁移不要改，只能新增
- 新加字段的 `defaultValue` 要和迁移 SQL 里的 `DEFAULT` 一致，否则 Room 校验表结构不通过
- 部分手机用 Gradle 跑设备测试会中止，可以手动安装测试包后用 `adb shell am instrument -w com.lib.room.test/androidx.test.runner.AndroidJUnitRunner` 运行
