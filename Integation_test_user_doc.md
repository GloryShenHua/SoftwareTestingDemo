## UserController 集成测试

| 测试对象 | src.main.java.com.demo.controller.user.UserController.java |
|---|---|
| 测试脚本 | src.test.java.com.demo.controller.user.UserControllerIntegrationTest.java |

| 功能点列表 | 用例编号 | 用例描述 | 测试结果 | 预期结果 | 结论 |
|---|---|---|---|---|---|
| 页面访问 | UC-01 | 访问 `/signup` | 返回 `signup` 视图 | 正常渲染注册页 | 通过 |
|  | UC-02 | 访问 `/login` | 返回 `login` 视图 | 正常渲染登录页 | 通过 |
| 登录校验 | UC-03 | 普通用户正确登录 | 返回 `/index`，session 写入 `user` | 进入用户首页分支 | 通过 |
|  | UC-04 | 管理员正确登录 | 返回 `/admin_index`，session 写入 `admin` | 进入管理员首页分支 | 通过 |
|  | UC-05 | 错误密码登录 | 返回 `false` | 登录失败 | 通过 |
|  | UC-11 | `userID` 不存在或为空 | 返回 `false` | 登录失败 | 通过 |
|  | UC-12 | `password` 为空或双方都为空 | 返回 `false` | 登录失败 | 通过 |
| 注册功能 | UC-06 | 正常注册 | 重定向 `login`，记录新增 | 注册成功 | 通过 |
|  | UC-13 | 重复 `userID` 注册 | 仍新增并重定向 | 暴露当前实现缺陷 | 通过 |
|  | UC-14 | 缺失字段/空字符串注册 | 仍可提交并重定向 | 暴露当前实现缺陷 | 通过 |
|  | UC-15 | 校验默认字段 | `picture=""` 且 `isadmin=0` | 默认值正确 | 通过 |
| 资料更新 | UC-07 | `passwordNew` 为空更新资料 | 密码不变，其他字段更新 | 走不改密分支 | 通过 |
|  | UC-16 | `passwordNew` 非空更新资料 | 密码成功更新 | 走改密分支 | 通过 |
|  | UC-17 | 上传非空图片更新资料 | `picture` 更新为 `file/user/*` | 走图片上传分支 | 通过 |
|  | UC-18 | 无 session/伪造 `userID` 更新 | 可更新他人数据 | 暴露权限缺陷 | 通过 |
|  | UC-19 | `userID` 不存在/缺失、缺失文件字段 | 抛 `NullPointerException` | 异常流可观测 | 通过 |
|  | UC-20 | `email`、`phone` 为空 | 更新成功 | 当前实现允许空值 | 通过 |
| 密码检查 | UC-08 | 正确/错误密码检查 | 返回 `true/false` | 与输入匹配 | 通过 |
|  | UC-21 | 不存在/空 `userID` | 抛 `NullPointerException` | 异常流可观测 | 通过 |
|  | UC-22 | 缺失 `password` 参数 | 返回 `false` | 当前实现行为正确记录 | 通过 |
| 用户页与退出 | UC-23 | `/user_info` 有无 session 访问 | 有会话正常，无会话模板异常 | 暴露页面依赖 session 行为 | 通过 |
|  | UC-09 | `/logout.do` | 重定向并清除 `user` | 用户退出正确 | 通过 |
|  | UC-10 | `/quit.do` | 重定向并清除 `admin` | 管理员退出正确 | 通过 |
|  | UC-24 | 错误角色调用退出接口 | 仅清除对应角色键 | 不误清理另一角色 | 通过 |

## MessageController 集成测试

| 测试对象 | src.main.java.com.demo.controller.user.MessageController.java |
|---|---|
| 测试脚本 | src.test.java.com.demo.controller.user.MessageControllerIntegrationTest.java |

| 功能点列表 | 用例编号 | 用例描述 | 测试结果 | 预期结果 | 结论 |
|---|---|---|---|---|---|
| 留言页访问 | MC-01 | 未登录访问 `/message_list` | 抛 `LoginException` | 未登录禁止访问 | 通过 |
|  | MC-02 | 已登录访问 `/message_list` | 返回 `message_list` 视图 | 正常渲染 | 通过 |
| 公开留言分页 | MC-03 | `page=1` 查询公开留言 | 仅返回 state=2 留言 | 公开列表正确 | 通过 |
|  | MC-09 | 不传 `page` | 默认 `page=1` 返回成功 | 默认值生效 | 通过 |
|  | MC-04 | `page=0` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | MC-10 | `page=-1` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | MC-11 | `page=9999` | 返回空列表 | 超范围空页 | 通过 |
|  | MC-12 | `page=abc` | 返回 400 | 类型错误处理正确 | 通过 |
| 用户留言分页 | MC-05 | 登录用户查询 `/message/findUserList` | 返回当前用户留言 | 用户隔离正确 | 通过 |
|  | MC-14 | 校验列表内容与顺序 | `userID` 全匹配，含 state=1/2，倒序返回 | 结果内容正确 | 通过 |
|  | MC-13 | 未登录查询用户留言 | 抛 `LoginException` | 未登录禁止访问 | 通过 |
| 留言新增 | MC-06 | 正常新增留言 | 重定向并写入 state=1 | 新增成功 | 通过 |
|  | MC-15 | `content=""` | 成功写入空字符串 | 当前实现行为记录 | 通过 |
|  | MC-16 | 缺失 `content` | 成功写入 null | 当前实现行为记录 | 通过 |
|  | MC-17 | `userID` 不存在 | 成功写入记录 | 当前实现行为记录 | 通过 |
| 留言修改删除 | MC-07 | 修改留言 | 返回 `true`，内容和状态更新 | 修改成功 | 通过 |
|  | MC-18 | 修改不存在 `messageID` | 抛 `EntityNotFoundException` | 异常流可观测 | 通过 |
|  | MC-19 | 空/缺失 `content` 修改 | 更新为 `""` 或 `null` | 当前实现行为记录 | 通过 |
|  | MC-08 | 删除存在留言 | 返回 `true` 且记录删除 | 删除成功 | 通过 |
|  | MC-20 | 删除不存在留言 | 抛 `EmptyResultDataAccessException` | 异常流可观测 | 通过 |

## OrderController 集成测试

| 测试对象 | src.main.java.com.demo.controller.user.OrderController.java |
|---|---|
| 测试脚本 | src.test.java.com.demo.controller.user.OrderControllerIntegrationTest.java |

| 功能点列表 | 用例编号 | 用例描述 | 测试结果 | 预期结果 | 结论 |
|---|---|---|---|---|---|
| 订单管理页 | OC-01 | 未登录访问 `/order_manage` | 抛 `LoginException` | 未登录禁止访问 | 通过 |
|  | OC-02 | 已登录访问 `/order_manage` | 返回视图，model 含 `total` | 页面数据正确 | 通过 |
| 下单入口页 | OC-03 | `/order_place.do?venueID=...` | 返回视图并带 `venue` | 场馆信息正确 | 通过 |
|  | OC-16 | `/order_place` 无参数访问 | 返回 `order_place` 视图 | 页面正常 | 通过 |
| 订单列表分页 | OC-04 | 登录用户 `page=1` 查询 | 返回当前用户订单 | 列表正确 | 通过 |
|  | OC-10 | 未登录查询 `/getOrderList.do` | 抛 `LoginException` | 未登录禁止访问 | 通过 |
|  | OC-11 | 不传 `page` | 默认 `page=1` 返回成功 | 默认值生效 | 通过 |
|  | OC-12 | `page=0/-1` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | OC-13 | `page=9999` | 返回空列表 | 超范围空页 | 通过 |
| 订单新增 | OC-05 | `hours=1` 正常下单 | 重定向并新增订单 | 下单成功 | 通过 |
|  | OC-17 | 未登录下单 | 抛 `LoginException` | 未登录禁止提交 | 通过 |
|  | OC-18 | `hours=0/-1/24` | 均可提交并落库 | 当前实现行为记录 | 通过 |
|  | OC-19 | 缺参/非法参数/不存在场馆 | 抛异常或 400 | 异常流可观测 | 通过 |
| 订单状态与编辑 | OC-20 | 完成订单 | 200 且响应体空，状态变 3 | 状态流转正确 | 通过 |
|  | OC-21 | 完成不存在订单 | 抛 `RuntimeException` | 异常流可观测 | 通过 |
|  | OC-22 | `/modifyOrder.do` 页面入口 | model 含 `order` 和 `venue` | 编辑页数据正确 | 通过 |
|  | OC-07 | 正常修改订单 | 重定向并持久化更新 | 修改成功 | 通过 |
|  | OC-23 | 未登录/非法参数修改 | 抛异常或 400 | 异常流可观测 | 通过 |
| 订单删除 | OC-08 | 删除存在订单 | 返回 `true` 且删除成功 | 删除成功 | 通过 |
|  | OC-24 | 删除不存在订单 | 抛 `EmptyResultDataAccessException` | 异常流可观测 | 通过 |
| 场馆日期订单查询 | OC-09 | 正常查询命中日期 | 返回 `venue+orders` 且内容正确 | 查询成功 | 通过 |
|  | OC-25 | 不同日期/缺参/非法参数/不存在场馆 | 返回空列表或抛异常 | 异常流与边界可观测 | 通过 |

## NewsController 集成测试

| 测试对象 | src.main.java.com.demo.controller.user.NewsController.java |
|---|---|
| 测试脚本 | src.test.java.com.demo.controller.user.NewsControllerIntegrationTest.java |

| 功能点列表 | 用例编号 | 用例描述 | 测试结果 | 预期结果 | 结论 |
|---|---|---|---|---|---|
| 新闻详情页 | NC-01 | 正常访问 `/news?newsID=有效ID` | 返回 `news` 视图并带正确模型 | 详情渲染正确 | 通过 |
|  | NC-06 | `newsID` 不存在 | 渲染阶段抛 `EntityNotFoundException` | 异常流可观测 | 通过 |
|  | NC-07 | 缺失 `newsID` | 参数绑定抛 `IllegalStateException` | 异常流可观测 | 通过 |
|  | NC-08 | `newsID=abc` | 返回 400 | 类型错误处理正确 | 通过 |
| 新闻分页接口 | NC-02 | `page=1` 查询 | 返回分页 JSON，排序正确 | 分页正确 | 通过 |
|  | NC-09 | 不传 `page` | 默认 `page=1` 返回成功 | 默认值生效 | 通过 |
|  | NC-03 | `page=0` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | NC-10 | `page=-1` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | NC-11 | `page=2` | 返回空 `content` | 超范围空页 | 通过 |
|  | NC-12 | `page=abc` | 返回 400 | 类型错误处理正确 | 通过 |
| 新闻列表页 | NC-04 | 访问 `/news_list` | 返回视图，model 含 `news_list` 与 `total` | 页面数据正确 | 通过 |
|  | NC-13 | 校验列表顺序与总页数 | 新闻B在前，total正确 | 排序与统计正确 | 通过 |

## VenueController 集成测试

| 测试对象 | src.main.java.com.demo.controller.user.VenueController.java |
|---|---|
| 测试脚本 | src.test.java.com.demo.controller.user.VenueControllerIntegrationTest.java |

| 功能点列表 | 用例编号 | 用例描述 | 测试结果 | 预期结果 | 结论 |
|---|---|---|---|---|---|
| 场馆详情页 | VC-01 | 正常访问 `/venue?venueID=有效ID` | 返回 `venue` 视图并带正确模型 | 详情渲染正确 | 通过 |
|  | VC-06 | `venueID` 不存在 | 渲染阶段抛 `EntityNotFoundException` | 异常流可观测 | 通过 |
|  | VC-07 | 缺失 `venueID` | 参数绑定抛 `IllegalStateException` | 异常流可观测 | 通过 |
|  | VC-08 | `venueID=abc` | 返回 400 | 类型错误处理正确 | 通过 |
| 场馆分页接口 | VC-02 | `page=1` 查询 | 返回分页 JSON，按 venueID 升序 | 分页与排序正确 | 通过 |
|  | VC-09 | 不传 `page` | 默认 `page=1` 返回成功 | 默认值生效 | 通过 |
|  | VC-03 | `page=0` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | VC-10 | `page=-1` | 抛 `IllegalArgumentException` | 越界异常 | 通过 |
|  | VC-11 | `page=2` | 返回空 `content` | 超范围空页 | 通过 |
|  | VC-12 | `page=abc` | 返回 400 | 类型错误处理正确 | 通过 |
| 场馆列表页 | VC-04 | 访问 `/venue_list` | 返回视图，model 含 `venue_list` 与 `total` | 页面数据正确 | 通过 |
|  | VC-13 | 校验列表顺序和长度 | 顺序为场馆A、场馆B，长度与 total 正确 | 结果正确 | 通过 |

