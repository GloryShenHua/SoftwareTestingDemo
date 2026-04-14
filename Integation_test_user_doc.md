## user controllers 集成测试报告

### 1. 测试范围与目标

- 测试对象：`src/main/java/com/demo/controller/user` 下全部 5 个控制器  
  - `UserController`
  - `MessageController`
  - `OrderController`
  - `NewsController`
  - `VenueController`
- 测试方式：`SpringBootTest + MockMvc + H2(in-memory)`，覆盖 Controller-Service-Repository 链路。
- 测试脚本：
  - `src/test/java/com/demo/controller/user/UserControllerIntegrationTest.java`
  - `src/test/java/com/demo/controller/user/MessageControllerIntegrationTest.java`
  - `src/test/java/com/demo/controller/user/OrderControllerIntegrationTest.java`
  - `src/test/java/com/demo/controller/user/NewsControllerIntegrationTest.java`
  - `src/test/java/com/demo/controller/user/VenueControllerIntegrationTest.java`
  - 公共数据基类：`src/test/java/com/demo/controller/user/BaseUserControllerIntegrationTest.java`

### 2. 测试设计说明

- 黑盒技术：
  - **等价类划分**：登录成功/失败、已登录/未登录、有效分页/非法分页、存在数据/删除后数据。
  - **边界值分析**：分页参数 `page=1`（有效下边界）与 `page=0`（越界）；预约 `hours=1`（最小正值边界）。
- 白盒技术：
  - **语句覆盖**：覆盖各接口主要业务语句（查询、创建、更新、删除、跳转、session 操作）。
  - **判定覆盖**：覆盖关键分支判定：
    - 登录分支：普通用户 / 管理员 / 登录失败
    - 会话分支：已登录 / 未登录（抛 `LoginException`）
    - 参数分支：分页合法 / 非法
    - 更新分支：密码为空不更新、留言状态重置、订单状态流转

### 3. 用例明细

| 用例编号 | 控制器 | 接口 | 设计技术 | 用例描述 | 预期结果 | 执行结果 | 结论 |
|---|---|---|---|---|---|---|---|
| UC-01 | UserController | `GET /signup` | 语句覆盖 | 打开注册页 | 返回 `signup` 视图 | 与预期一致 | 通过 |
| UC-02 | UserController | `GET /login` | 语句覆盖 | 打开登录页 | 返回 `login` 视图 | 与预期一致 | 通过 |
| UC-03 | UserController | `POST /loginCheck.do` | 等价类+判定覆盖 | 普通用户正确凭据登录 | 返回 `/index`，session 写入 `user` | 与预期一致 | 通过 |
| UC-04 | UserController | `POST /loginCheck.do` | 等价类+判定覆盖 | 管理员正确凭据登录 | 返回 `/admin_index`，session 写入 `admin` | 与预期一致 | 通过 |
| UC-05 | UserController | `POST /loginCheck.do` | 等价类+判定覆盖 | 错误密码登录 | 返回 `false` | 与预期一致 | 通过 |
| UC-06 | UserController | `POST /register.do` | 等价类+语句覆盖 | 新用户注册 | 重定向 `login`，数据库新增用户 | 与预期一致 | 通过 |
| UC-07 | UserController | `POST /updateUser.do` | 判定覆盖 | `passwordNew` 为空时更新资料 | 重定向 `user_info`，密码保持不变 | 与预期一致 | 通过 |
| UC-08 | UserController | `GET /checkPassword.do` | 等价类+判定覆盖 | 校验密码正确/错误 | 返回 `true/false` | 与预期一致 | 通过 |
| UC-09 | UserController | `GET /logout.do` | 语句覆盖 | 用户退出 | 重定向 `/index`，清除 `user` | 与预期一致 | 通过 |
| UC-10 | UserController | `GET /quit.do` | 语句覆盖 | 管理员退出 | 重定向 `/index`，清除 `admin` | 与预期一致 | 通过 |
| MC-01 | MessageController | `GET /message_list` | 判定覆盖 | 未登录访问留言主页 | 抛 `LoginException` | 与预期一致 | 通过 |
| MC-02 | MessageController | `GET /message_list` | 等价类+语句覆盖 | 已登录访问留言主页 | 返回 `message_list` 视图 | 与预期一致 | 通过 |
| MC-03 | MessageController | `GET /message/getMessageList?page=1` | 等价类+边界值 | 查询公开留言第一页 | 仅返回通过状态留言（state=2） | 与预期一致 | 通过 |
| MC-04 | MessageController | `GET /message/getMessageList?page=0` | 边界值+判定覆盖 | 分页越界 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| MC-05 | MessageController | `GET /message/findUserList` | 等价类+判定覆盖 | 查询当前用户留言 | 返回当前用户留言集合 | 与预期一致 | 通过 |
| MC-06 | MessageController | `POST /sendMessage` | 语句覆盖 | 新增留言 | 重定向 `/message_list`，state=1 | 与预期一致 | 通过 |
| MC-07 | MessageController | `POST /modifyMessage.do` | 语句覆盖 | 修改留言 | 返回 `true`，内容更新且状态置为1 | 与预期一致 | 通过 |
| MC-08 | MessageController | `POST /delMessage.do` | 语句覆盖 | 删除留言 | 返回 `true`，数据库记录被删除 | 与预期一致 | 通过 |
| OC-01 | OrderController | `GET /order_manage` | 判定覆盖 | 未登录访问订单页 | 抛 `LoginException` | 与预期一致 | 通过 |
| OC-02 | OrderController | `GET /order_manage` | 等价类+语句覆盖 | 已登录访问订单页 | 返回 `order_manage` 视图 | 与预期一致 | 通过 |
| OC-03 | OrderController | `GET /order_place.do` | 语句覆盖 | 按 venueID 打开预约页 | 返回 `order_place` 且包含场馆信息 | 与预期一致 | 通过 |
| OC-04 | OrderController | `GET /getOrderList.do?page=1` | 等价类+边界值 | 查询订单第一页 | 返回当前用户订单列表 | 与预期一致 | 通过 |
| OC-05 | OrderController | `POST /addOrder.do` | 边界值+语句覆盖 | `hours=1` 提交订单（最小有效边界） | 重定向 `order_manage`，订单新增且总价正确 | 与预期一致 | 通过 |
| OC-06 | OrderController | `POST /finishOrder.do` | 语句覆盖 | 完成订单 | 订单状态更新为 3 | 与预期一致 | 通过 |
| OC-07 | OrderController | `POST /modifyOrder` | 语句覆盖 | 修改订单 | 重定向 `order_manage`，场馆/时长/时间更新 | 与预期一致 | 通过 |
| OC-08 | OrderController | `POST /delOrder.do` | 语句覆盖 | 删除订单 | 返回 `true`，订单删除 | 与预期一致 | 通过 |
| OC-09 | OrderController | `GET /order/getOrderList.do` | 等价类+边界值 | 查询指定场馆指定日期订单 | 返回 `venue + 当日订单` 结构且内容正确 | 与预期一致 | 通过 |
| NC-01 | NewsController | `GET /news` | 语句覆盖 | 查看新闻详情 | 返回 `news` 视图 | 与预期一致 | 通过 |
| NC-02 | NewsController | `GET /news/getNewsList?page=1` | 等价类+边界值 | 查询新闻列表第一页 | 返回分页 JSON，排序正确 | 与预期一致 | 通过 |
| NC-03 | NewsController | `GET /news/getNewsList?page=0` | 边界值+判定覆盖 | 分页越界 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| NC-04 | NewsController | `GET /news_list` | 语句覆盖 | 打开新闻列表页 | 返回 `news_list` 视图 | 与预期一致 | 通过 |
| VC-01 | VenueController | `GET /venue` | 语句覆盖 | 查看场馆详情 | 返回 `venue` 视图 | 与预期一致 | 通过 |
| VC-02 | VenueController | `GET /venuelist/getVenueList?page=1` | 等价类+边界值 | 查询场馆列表第一页 | 返回分页 JSON 且内容正确 | 与预期一致 | 通过 |
| VC-03 | VenueController | `GET /venuelist/getVenueList?page=0` | 边界值+判定覆盖 | 分页越界 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| VC-04 | VenueController | `GET /venue_list` | 语句覆盖 | 打开场馆列表页 | 返回 `venue_list` 视图 | 与预期一致 | 通过 |


- 运行命令：

```bash
mvn test
```

