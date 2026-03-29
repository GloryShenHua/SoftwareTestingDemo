# 四个 Service 类单元测试实验记录

## 1. 实验目标
对以下 4 个 service 实现类进行单元测试，覆盖主要功能路径与关键异常路径：
- `MessageServiceImpl`
- `MessageVoServiceImpl`
- `VenueServiceImpl`
- `NewsServiceImpl`

测试方式采用 **JUnit 5 + Mockito**，只验证 service 逻辑与 DAO 交互，不启动 Spring 容器与数据库。

## 2. 测试环境
- JDK：`1.8`
- 构建工具：Maven
- 测试框架：`spring-boot-starter-test`（含 JUnit5 / Mockito）
- 执行日期：`2026-03-29`

## 3. 测试过程
1. 阅读 `agent.md`，确认目标 service 与建议覆盖范围。
2. 阅读四个 service 实现及对应 DAO/Entity，提取每个方法的输入、分支、预期 DAO 调用。
3. 在 `src/test/java/com/demo/service/impl/` 新建 4 个测试类：
   - `MessageServiceImplTest`
   - `MessageVoServiceImplTest`
   - `VenueServiceImplTest`
   - `NewsServiceImplTest`
4. 使用 Mockito 模拟 DAO 返回值，分别设计正常路径与异常路径用例。
5. 执行指定测试类，避免 `demoApplicationTests` 对数据库环境的依赖影响本次 service 单测。
6. 修复一次编码问题：PowerShell 写入文件默认带 BOM，导致 Java 编译报 `非法字符 '\ufeff'`；随后将测试文件改为 UTF-8 无 BOM 后重新执行。

## 4. 测试用例设计

### 4.1 MessageServiceImpl（12 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| MS-01 | `findById` | DAO 返回 Message | 返回对象与 DAO 返回一致；校验 `getOne` 被调用 |
| MS-02 | `findByUser` | 用户有留言 | 返回分页对象一致；校验 `findAllByUserID` |
| MS-03 | `findByUser` | 用户无留言 | 返回空分页；校验 DAO 调用参数 |
| MS-04 | `create` | 保存成功 | 返回 `save(...).messageID` |
| MS-05 | `delById` | 删除留言 | 校验 `deleteById` 被调用 |
| MS-06 | `update` | 更新留言 | 校验 `save` 被调用 |
| MS-07 | `confirmMessage` | 留言存在 | 校验 `updateState(STATE_PASS, id)` |
| MS-08 | `confirmMessage` | 留言不存在 | 抛出 `RuntimeException`；不调用 `updateState` |
| MS-09 | `rejectMessage` | 留言存在 | 校验 `updateState(STATE_REJECT, id)` |
| MS-10 | `rejectMessage` | 留言不存在 | 抛出 `RuntimeException`；不调用 `updateState` |
| MS-11 | `findWaitState` | 查询待审核 | 校验按 `STATE_NO_AUDIT` 查询 |
| MS-12 | `findPassState` | 查询已通过 | 校验按 `STATE_PASS` 查询 |

### 4.2 MessageVoServiceImpl（6 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| MVS-01 | `returnMessageVoByMessageID` | message/user 都存在 | `MessageVo` 字段组装正确（message + user） |
| MVS-02 | `returnMessageVoByMessageID` | message 不存在 | 抛出 `NullPointerException`；不查询 user |
| MVS-03 | `returnMessageVoByMessageID` | user 不存在 | 抛出 `NullPointerException` |
| MVS-04 | `returnVo` | 输入空列表 | 返回空列表；DAO 无交互 |
| MVS-05 | `returnVo` | 输入 1 条 message | 返回 1 条 VO；字段正确 |
| MVS-06 | `returnVo` | 输入多条 message | 返回 VO 数量正确且顺序与输入一致 |

### 4.3 VenueServiceImpl（10 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| VS-01 | `findByVenueID` | DAO 返回场馆 | 返回对象一致；校验 `getOne` |
| VS-02 | `findByVenueName` | 场馆存在 | 返回对象一致 |
| VS-03 | `findByVenueName` | 场馆不存在 | 返回 `null` |
| VS-04 | `findAll(Pageable)` | 分页查询 | 返回分页对象一致 |
| VS-05 | `findAll()` | 列表查询 | 返回列表对象一致 |
| VS-06 | `create` | 保存成功 | 返回 `venueID` |
| VS-07 | `update` | 更新场馆 | 校验 `save` 调用 |
| VS-08 | `delById` | 删除场馆 | 校验 `deleteById` 调用 |
| VS-09 | `countVenueName` | 无重名 | 返回 0 |
| VS-10 | `countVenueName` | 有重名 | 返回正数计数 |

### 4.4 NewsServiceImpl（6 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| NS-01 | `findAll(Pageable)` | 有新闻数据 | 返回分页对象一致 |
| NS-02 | `findAll(Pageable)` | 无新闻数据 | 返回空分页 |
| NS-03 | `findById` | 按 ID 查询 | 返回对象一致；校验 `getOne` |
| NS-04 | `create` | 保存新闻 | 返回 `newsID` |
| NS-05 | `delById` | 删除新闻 | 校验 `deleteById` 调用 |
| NS-06 | `update` | 更新新闻 | 校验 `save` 调用 |

## 5. 执行命令与结果
### 5.1 执行命令
```powershell
mvn "-Dtest=MessageServiceImplTest,MessageVoServiceImplTest,VenueServiceImplTest,NewsServiceImplTest" test
```

### 5.2 结果摘要
- `MessageServiceImplTest`：12/12 通过
- `MessageVoServiceImplTest`：6/6 通过
- `VenueServiceImplTest`：10/10 通过
- `NewsServiceImplTest`：6/6 通过
- 总计：`34` 条用例，`34` 通过，`0` 失败，`0` 错误，`0` 跳过
- Maven 结果：`BUILD SUCCESS`

## 6. 产出文件
- `src/test/java/com/demo/service/impl/MessageServiceImplTest.java`
- `src/test/java/com/demo/service/impl/MessageVoServiceImplTest.java`
- `src/test/java/com/demo/service/impl/VenueServiceImplTest.java`
- `src/test/java/com/demo/service/impl/NewsServiceImplTest.java`
- `result.md`