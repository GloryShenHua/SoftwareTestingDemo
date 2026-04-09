# 三个 Service 类单元测试实验记录

## 1. 实验目标
对以下的 3 个 service 实现类进行单元测试，涵盖主要功能路径与关键异常边界：
- `UserServiceImpl`
- `OrderServiceImpl`
- `OrderVoServiceImpl`

测试方式采用 **JUnit 5 + Mockito**，验证 service 的运行逻辑与外部 DAO 的交互，完全剔除数据库环境进行隔离测试。

## 2. 测试环境
- JDK：`1.8` / 兼容编译至 `21`
- 构建工具：Maven
- 测试框架：`spring-boot-starter-test`（以 JUnit5 与 Mockito 为主）
- 额外兼容组件：`Lombok 1.18.30`

## 3. 测试过程
1. 切分目标 service 以及调用的相关实体组件。
2. 在 `src/test/java/com/demo/service/impl/` 创建对应的 3 个基础测试类：
   - `UserServiceImplTest`
   - `OrderServiceImplTest`
   - `OrderVoServiceImplTest`
4. 使用 Mockito 设定期望值断言并搭建桩代码，分别设计各个边界状态与抛出异常测试用例。
5. 针对高版本 JDK 的 AST 报错，在 `pom.xml` 中将 lombok 升级兼容；针对原始业务代码实体类增加的内容，补齐了构造函数的空缺。
6. 与全团队的模块进行联跑检测通过。

## 4. 测试用例设计

### 4.1 UserServiceImpl（9 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| US-01 | `findByUserID` | 查询存在的 UserID | 验证返回对象与 DAO 一致，且执行目标查询 |
| US-02 | `findById` | 查询存在的 ID | 验证返回对象与 DAO 一致 |
| US-03 | `findByUserID` | 分页查询非管理员用户 | 验证返回 Page 与 DAO 调用传参一致 |
| US-04 | `checkLogin` | 账号密码完全匹配 | 验证匹配并返回对应 User 单体 |
| US-05 | `checkLogin` | 密码错误或不存在 | 返回 Null |
| US-06 | `create` | 模拟保存新账户 | 验证 `save` 并且返回获取的数据数量 |
| US-07 | `delByID` | 根据ID删除记录 | 验证触发调用了 `deleteById` 一次 |
| US-08 | `updateUser` | 用户信息更新 | 验证触发调用了 `save` 一次 |
| US-09 | `countUserID` | 查询存在关联数量 | 对比校验获得的整数值等于 Mock 数据 |

### 4.2 OrderServiceImpl（12 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| OS-01 | `findById` | 查询存在的订单ID | 验证返回值对象一致 |
| OS-02 | `findDateOrder` | 基于制定日期内区间查找 | 验证获取到对应的列表大小一致 |
| OS-03 | `findUserOrder` | 提取当前用户分页 | 验证所获取的 分页载荷 与查询请求参数一致 |
| OS-04 | `updateOrder` | 前台触发订单修改 | 验证调用了 `save` ，重算的小计以及状态的改变均被覆盖 |
| OS-05 | `submit` | 提交新场馆订单 | 验证订单被组装产生，并正常被保存进数据库 |
| OS-06 | `delOrder` | 删掉不需要的预定 | 验证成功调用 DAO 级 `deleteById` 清理 |
| OS-07 | `confirmOrder` | 确认符合条件的订单 | 验证确认执行，状态切为 WAIT （等待执行）且保存|
| OS-08 | `confirmOrder` | 对空对象的恶意调用 | 抛出 `RuntimeException` |
| OS-09 | `finishOrder` | 宣告订单落幕 | 验证该状态已被覆盖保存为 FINISH |
| OS-10 | `rejectOrder` | 拒绝掉审批期的订单 | 验证被更新为 REJECT 并执行落表 |
| OS-11 | `findNoAuditOrder`| 拉取未审核分页集 | 验证得到对应的 NO_AUDIT 查询数据 |
| OS-12 | `findAuditOrder` | 查询所有正在进行和已过的订单| 验证按状态 FINISH 与 WAIT 获取的结果集 |

### 4.3 OrderVoServiceImpl（2 条）
| 用例ID | 方法 | 场景 | 关键断言 |
| --- | --- | --- | --- |
| OVS-01 | `returnOrderVoByOrderID` | 读取包含连带信息的订单ID | 验证分别读取了 Order 以及 Venue 且被拼装正确 |
| OVS-02 | `returnVo` | 整个列表的视图转换 | 验证输出VO列表大小同步，且底层 `findByVenueID` 执行次数匹配 |

## 5. 执行命令与结果
### 5.1 执行命令
```powershell
mvn test -Dtest="UserServiceImplTest,OrderServiceImplTest,OrderVoServiceImplTest" -B
```

### 5.2 结果摘要
- `UserServiceImplTest`：9/9 通过
- `OrderServiceImplTest`：12/12 通过
- `OrderVoServiceImplTest`：2/2 通过
- 总计：`23` 条用例，`23` 通过，`0` 失败，`0` 错误，`0` 跳过
- Maven 本地化集测结果：`BUILD SUCCESS`

## 6. 产出文件
- `src/test/java/com/demo/service/impl/UserServiceImplTest.java`
- `src/test/java/com/demo/service/impl/OrderServiceImplTest.java`
- `src/test/java/com/demo/service/impl/OrderVoServiceImplTest.java`
- `result_test_zly.md`
- 完善补充入库的：`附件2-测试用例设计文档模板.md`
