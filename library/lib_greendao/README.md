# lib_greendao（GreenDao 数据库）

GreenDao 数据库，内置省市区数据。老功能在用；新功能建议用 `lib_room`。

## 依赖

| 依赖 | 方式 | 用途 |
| --- | --- | --- |
| `lib_startup` | api | 启动时打开数据库 |
| greendao | api | 数据库框架 |
| converter-gson、GsonFactory | api | json 解析，GsonFactory 解析出错时不崩溃，会打印出错字段 |

## 主要内容

```
com.greendao.db
├── bean/            实体：CityBean、CityDataBean、DemoBean
├── helper/          GreenDao 生成的 DaoMaster、DaoSession、各 Dao，不要手改
├── util/
│   ├── LocalRepository   数据库入口单例：getCityBeanDao()、getDaoSession() 等
│   ├── DbHelper          库名 translate-db，onUpgrade 处理升级
│   ├── DBUpdateHelper    升级时迁移旧数据
│   └── GsonUtil          全局 Gson（容错解析）
└── init/DbInitializer    启动时打开数据库，后台导入一次 res/raw/province.json
```

## 用法

```java
CityBeanDao dao = LocalRepository.getInstance().getCityBeanDao();
List<CityBean> list = dao.queryBuilder().list();
```

`DbInitializer` 已登记到启动初始化，不需要手动 `init`。

## 新增表 / 升级

1. 在 `bean/` 加 `@Entity` 实体
2. `build.gradle` 里 `greendao { schemaVersion }` +1
3. 编译，`helper/` 下自动生成对应 Dao
4. 在 `LocalRepository` 加一个 `getXxxDao()`
5. 要保留老数据的表，加到 `DbHelper.onUpgrade` 里 `DBUpdateHelper.migrate(db, ...)` 的参数中（目前只有 `DemoBeanDao`）。没列进去的表升级后会被重建，数据清空

## 注意

- 生成代码直接输出到 `src/main/java`（`targetGenDir`），`helper/` 下的文件改了会被覆盖
- 查询是同步的，不要在主线程做大量读写
