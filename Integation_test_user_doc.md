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
  - **等价类划分**：登录成功/失败、已登录/未登录、有效分页/非法分页、存在数据/删除后数据、有效输入/非法输入（空值、缺失、不存在关联数据）。
  - **边界值分析**：分页参数 `page=1`（有效下边界）、`page=0`、`page=-1`（越界）；`page` 超大值；预约 `hours=1`（最小正值边界）。
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
| UC-11 | UserController | `POST /loginCheck.do` | 等价类划分 | `userID` 不存在/为空 | 返回 `false`，不写入 session | 与预期一致 | 通过 |
| UC-12 | UserController | `POST /loginCheck.do` | 等价类划分 | `password` 为空/双方都为空 | 返回 `false`，不写入 session | 与预期一致 | 通过 |
| UC-13 | UserController | `POST /register.do` | 等价类划分 | 重复 `userID` 注册 | 按当前实现可重复注册并重定向 | 与预期一致 | 通过 |
| UC-14 | UserController | `POST /register.do` | 等价类+边界值 | 缺失/空字符串注册字段 | 按当前实现可写入并重定向 | 与预期一致 | 通过 |
| UC-15 | UserController | `POST /register.do` | 更强断言 | 注册默认属性校验 | `picture=\"\"` 且 `isadmin=0` | 与预期一致 | 通过 |
| UC-16 | UserController | `POST /updateUser.do` | 判定覆盖 | `passwordNew` 非空分支 | 密码被更新并重定向 | 与预期一致 | 通过 |
| UC-17 | UserController | `POST /updateUser.do` | 判定覆盖 | 上传非空图片分支 | `picture` 更新为 `file/user/*` | 与预期一致 | 通过 |
| UC-18 | UserController | `POST /updateUser.do` | 缺陷暴露 | 无 session/伪造 `userID` 更新 | 按当前实现可更新他人数据 | 与预期一致 | 通过 |
| UC-19 | UserController | `POST /updateUser.do` | 异常流+等价类 | `userID` 不存在/缺失、缺失文件字段 | 抛 `NullPointerException`（当前实现行为） | 与预期一致 | 通过 |
| UC-20 | UserController | `POST /updateUser.do` | 等价类划分 | `email`/`phone` 为空 | 按当前实现允许并成功更新 | 与预期一致 | 通过 |
| UC-21 | UserController | `GET /checkPassword.do` | 异常类覆盖 | 不存在/空 `userID` | 抛 `NullPointerException` | 与预期一致 | 通过 |
| UC-22 | UserController | `GET /checkPassword.do` | 等价类划分 | 缺失 `password` | 返回 `false` | 与预期一致 | 通过 |
| UC-23 | UserController | `GET /user_info` | 等价类+异常流 | 有/无 `session.user` 访问 | 有会话正常渲染，无会话模板渲染异常 | 与预期一致 | 通过 |
| UC-24 | UserController | `GET /logout.do`/`GET /quit.do` | 判定覆盖+更强断言 | 角色正确/错误调用退出接口 | 仅清除对应角色 session，另一角色保持不变 | 与预期一致 | 通过 |
| MC-01 | MessageController | `GET /message_list` | 判定覆盖 | 未登录访问留言主页 | 抛 `LoginException` | 与预期一致 | 通过 |
| MC-02 | MessageController | `GET /message_list` | 等价类+语句覆盖 | 已登录访问留言主页 | 返回 `message_list` 视图 | 与预期一致 | 通过 |
| MC-03 | MessageController | `GET /message/getMessageList?page=1` | 等价类+边界值 | 查询公开留言第一页 | 仅返回通过状态留言（state=2） | 与预期一致 | 通过 |
| MC-04 | MessageController | `GET /message/getMessageList?page=0` | 边界值+判定覆盖 | 分页越界 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| MC-05 | MessageController | `GET /message/findUserList` | 等价类+判定覆盖 | 查询当前用户留言 | 返回当前用户留言集合 | 与预期一致 | 通过 |
| MC-06 | MessageController | `POST /sendMessage` | 语句覆盖 | 新增留言 | 重定向 `/message_list`，state=1 | 与预期一致 | 通过 |
| MC-07 | MessageController | `POST /modifyMessage.do` | 语句覆盖 | 修改留言 | 返回 `true`，内容更新且状态置为1 | 与预期一致 | 通过 |
| MC-08 | MessageController | `POST /delMessage.do` | 语句覆盖 | 删除留言 | 返回 `true`，数据库记录被删除 | 与预期一致 | 通过 |
| MC-09 | MessageController | `GET /message/getMessageList` | 边界值分析 | 不传 `page` 参数 | 使用默认值 `page=1` 并返回结果 | 与预期一致 | 通过 |
| MC-10 | MessageController | `GET /message/getMessageList?page=-1` | 边界值+判定覆盖 | `page` 为负数 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| MC-11 | MessageController | `GET /message/getMessageList?page=9999` | 边界值分析 | `page` 超大值 | 返回空列表 | 与预期一致 | 通过 |
| MC-12 | MessageController | `GET /message/getMessageList?page=abc` | 等价类划分 | `page` 非数字 | 返回 `400 Bad Request` | 与预期一致 | 通过 |
| MC-13 | MessageController | `GET /message/findUserList?page=1` | 判定覆盖 | 未登录访问用户留言列表 | 抛 `LoginException` | 与预期一致 | 通过 |
| MC-14 | MessageController | `GET /message/findUserList?page=1` | 语句覆盖+更强断言 | 登录用户访问用户留言列表 | 每条记录均属于当前用户，且包含 state=1/2 并按时间倒序返回 | 与预期一致 | 通过 |
| MC-15 | MessageController | `POST /sendMessage` | 等价类划分 | `content=""` | 按当前实现成功写入空字符串内容 | 与预期一致 | 通过 |
| MC-16 | MessageController | `POST /sendMessage` | 等价类划分 | 缺失 `content` 参数 | 按当前实现成功写入 `null` 内容 | 与预期一致 | 通过 |
| MC-17 | MessageController | `POST /sendMessage` | 等价类划分 | `userID` 不存在 | 按当前实现成功写入记录（不做外键校验） | 与预期一致 | 通过 |
| MC-18 | MessageController | `POST /modifyMessage.do` | 判定覆盖+异常流 | 不存在的 `messageID` | 抛 `EntityNotFoundException` | 与预期一致 | 通过 |
| MC-19 | MessageController | `POST /modifyMessage.do` | 等价类划分 | 空/缺失 `content` | 按当前实现可更新为 `""` 或 `null` | 与预期一致 | 通过 |
| MC-20 | MessageController | `POST /delMessage.do` | 异常类覆盖 | 删除不存在 `messageID` | 抛 `EmptyResultDataAccessException` | 与预期一致 | 通过 |
| OC-01 | OrderController | `GET /order_manage` | 判定覆盖 | 未登录访问订单页 | 抛 `LoginException` | 与预期一致 | 通过 |
| OC-02 | OrderController | `GET /order_manage` | 等价类+语句覆盖 | 已登录访问订单页 | 返回 `order_manage` 视图 | 与预期一致 | 通过 |
| OC-03 | OrderController | `GET /order_place.do` | 语句覆盖 | 按 venueID 打开预约页 | 返回 `order_place` 且包含场馆信息 | 与预期一致 | 通过 |
| OC-04 | OrderController | `GET /getOrderList.do?page=1` | 等价类+边界值 | 查询订单第一页 | 返回当前用户订单列表 | 与预期一致 | 通过 |
| OC-05 | OrderController | `POST /addOrder.do` | 边界值+语句覆盖 | `hours=1` 提交订单（最小有效边界） | 重定向 `order_manage`，订单新增且总价正确 | 与预期一致 | 通过 |
| OC-06 | OrderController | `POST /finishOrder.do` | 语句覆盖 | 完成订单 | 订单状态更新为 3 | 与预期一致 | 通过 |
| OC-07 | OrderController | `POST /modifyOrder` | 语句覆盖 | 修改订单 | 重定向 `order_manage`，场馆/时长/时间更新 | 与预期一致 | 通过 |
| OC-08 | OrderController | `POST /delOrder.do` | 语句覆盖 | 删除订单 | 返回 `true`，订单删除 | 与预期一致 | 通过 |
| OC-09 | OrderController | `GET /order/getOrderList.do` | 等价类+边界值 | 查询指定场馆指定日期订单 | 返回 `venue + 当日订单` 结构且内容正确 | 与预期一致 | 通过 |
| OC-10 | OrderController | `GET /getOrderList.do` | 判定覆盖 | 未登录查询订单列表 | 抛 `LoginException` | 与预期一致 | 通过 |
| OC-11 | OrderController | `GET /getOrderList.do` | 边界值分析 | 不传 `page` | 使用默认页 `1` 并成功返回 | 与预期一致 | 通过 |
| OC-12 | OrderController | `GET /getOrderList.do?page=0/-1` | 边界值+判定覆盖 | 非法页码 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| OC-13 | OrderController | `GET /getOrderList.do?page=9999` | 边界值分析 | 超大页码 | 返回空列表 | 与预期一致 | 通过 |
| OC-14 | OrderController | `GET /order_manage` | 更强断言 | 已登录订单页模型校验 | model 包含 `total` 且值正确 | 与预期一致 | 通过 |
| OC-15 | OrderController | `GET /order_place.do` | 更强断言 | 预约页模型校验 | model 包含正确 `venue` | 与预期一致 | 通过 |
| OC-16 | OrderController | `GET /order_place` | 语句覆盖 | 无参数预约页入口 | 返回 `order_place` 视图 | 与预期一致 | 通过 |
| OC-17 | OrderController | `POST /addOrder.do` | 判定覆盖 | 未登录提交订单 | 抛 `LoginException` | 与预期一致 | 通过 |
| OC-18 | OrderController | `POST /addOrder.do` | 边界值分析 | `hours=0/-1/24` | 按当前实现均可提交并持久化 | 与预期一致 | 通过 |
| OC-19 | OrderController | `POST /addOrder.do` | 等价类+异常流 | 缺失参数/非法参数/不存在场馆 | 分别触发 `NPE`、`DateTimeParseException` 或 `400`（当前实现行为） | 与预期一致 | 通过 |
| OC-20 | OrderController | `POST /finishOrder.do` | 语句覆盖+更强断言 | 完成订单响应断言 | 状态 200 且响应体为空，订单状态变 3 | 与预期一致 | 通过 |
| OC-21 | OrderController | `POST /finishOrder.do` | 异常类覆盖 | 完成不存在订单 | 抛 `RuntimeException` | 与预期一致 | 通过 |
| OC-22 | OrderController | `GET /modifyOrder.do` | 语句覆盖+更强断言 | 修改页入口模型校验 | model 包含 `order` 与 `venue` | 与预期一致 | 通过 |
| OC-23 | OrderController | `POST /modifyOrder` | 判定覆盖+等价类 | 未登录、非法参数、不存在订单/场馆 | 触发 `LoginException`/`NPE`/`DateTimeParseException`/`400`（当前实现行为） | 与预期一致 | 通过 |
| OC-24 | OrderController | `POST /delOrder.do` | 异常类覆盖 | 删除不存在订单 | 抛 `EmptyResultDataAccessException` | 与预期一致 | 通过 |
| OC-25 | OrderController | `GET /order/getOrderList.do` | 边界值+等价类 | 不同日期、缺失/非法参数、不存在场馆 | 空列表或抛 `NPE`/`DateTimeParseException`（当前实现行为） | 与预期一致 | 通过 |
| NC-01 | NewsController | `GET /news` | 语句覆盖 | 查看新闻详情 | 返回 `news` 视图 | 与预期一致 | 通过 |
| NC-02 | NewsController | `GET /news/getNewsList?page=1` | 等价类+边界值 | 查询新闻列表第一页 | 返回分页 JSON，排序正确 | 与预期一致 | 通过 |
| NC-03 | NewsController | `GET /news/getNewsList?page=0` | 边界值+判定覆盖 | 分页越界 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| NC-04 | NewsController | `GET /news_list` | 语句覆盖 | 打开新闻列表页 | 返回 `news_list` 视图 | 与预期一致 | 通过 |
| NC-05 | NewsController | `GET /news` | 等价类+更强断言 | 详情页模型断言 | `model.news` 存在且 `newsID/title` 正确 | 与预期一致 | 通过 |
| NC-06 | NewsController | `GET /news?newsID=999999` | 等价类+异常流 | 不存在新闻 ID | 渲染阶段抛 `EntityNotFoundException` | 与预期一致 | 通过 |
| NC-07 | NewsController | `GET /news` | 等价类+异常流 | 缺失 `newsID` | 参数绑定阶段抛 `IllegalStateException`（当前实现行为） | 与预期一致 | 通过 |
| NC-08 | NewsController | `GET /news?newsID=abc` | 等价类划分 | 非数字 `newsID` | 返回 `400 Bad Request` | 与预期一致 | 通过 |
| NC-09 | NewsController | `GET /news/getNewsList` | 边界值分析 | 不传 `page` | 走默认值 `page=1` 并成功返回 | 与预期一致 | 通过 |
| NC-10 | NewsController | `GET /news/getNewsList?page=-1` | 边界值+判定覆盖 | 负数页码 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| NC-11 | NewsController | `GET /news/getNewsList?page=2` | 边界值分析 | 页码超范围 | 返回空 `content`（成功响应） | 与预期一致 | 通过 |
| NC-12 | NewsController | `GET /news/getNewsList?page=abc` | 等价类划分 | 非数字页码 | 返回 `400 Bad Request` | 与预期一致 | 通过 |
| NC-13 | NewsController | `GET /news_list` | 语句覆盖+更强断言 | 列表页模型断言 | `news_list/total` 存在且按时间倒序（新闻B在前） | 与预期一致 | 通过 |
| VC-01 | VenueController | `GET /venue` | 语句覆盖 | 查看场馆详情 | 返回 `venue` 视图 | 与预期一致 | 通过 |
| VC-02 | VenueController | `GET /venuelist/getVenueList?page=1` | 等价类+边界值 | 查询场馆列表第一页 | 返回分页 JSON 且内容正确 | 与预期一致 | 通过 |
| VC-03 | VenueController | `GET /venuelist/getVenueList?page=0` | 边界值+判定覆盖 | 分页越界 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| VC-04 | VenueController | `GET /venue_list` | 语句覆盖 | 打开场馆列表页 | 返回 `venue_list` 视图 | 与预期一致 | 通过 |
| VC-05 | VenueController | `GET /venue` | 更强断言 | 详情页模型校验 | `model.venue` 存在且 `venueID/venueName` 正确 | 与预期一致 | 通过 |
| VC-06 | VenueController | `GET /venue?venueID=999999` | 等价类+异常流 | 不存在场馆 ID | 渲染阶段抛 `EntityNotFoundException` | 与预期一致 | 通过 |
| VC-07 | VenueController | `GET /venue` | 等价类+异常流 | 缺失 `venueID` | 参数绑定阶段抛 `IllegalStateException`（当前实现行为） | 与预期一致 | 通过 |
| VC-08 | VenueController | `GET /venue?venueID=abc` | 等价类划分 | 非数字 `venueID` | 返回 `400 Bad Request` | 与预期一致 | 通过 |
| VC-09 | VenueController | `GET /venuelist/getVenueList` | 边界值分析 | 不传 `page` | 走默认值 `page=1` 并成功返回 | 与预期一致 | 通过 |
| VC-10 | VenueController | `GET /venuelist/getVenueList?page=-1` | 边界值+判定覆盖 | 负数页码 | 抛 `IllegalArgumentException` | 与预期一致 | 通过 |
| VC-11 | VenueController | `GET /venuelist/getVenueList?page=2` | 边界值分析 | 页码超范围 | 返回空 `content`（成功响应） | 与预期一致 | 通过 |
| VC-12 | VenueController | `GET /venuelist/getVenueList?page=abc` | 等价类划分 | 非数字页码 | 返回 `400 Bad Request` | 与预期一致 | 通过 |
| VC-13 | VenueController | `GET /venue_list` | 语句覆盖+更强断言 | 列表页模型与顺序校验 | `venue_list/total` 存在且顺序为场馆A、场馆B | 与预期一致 | 通过 |


### 4. 执行结论

- 本次共执行 93 条 user controllers 集成测试用例，全部通过。
- MessageController 用例已补齐：
  - 未登录分支；
  - 分页边界与异常输入；
  - `sendMessage`/`modifyMessage`/`delMessage` 的非法输入与异常流；
  - 更强结果断言（所属用户、状态覆盖、顺序）。

- 运行命令：

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
```

