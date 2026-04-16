# 一、单元测试用例

## 1. MessageServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.MessageServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.MessageServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| MS-01 | 调用 `findById(1)`，DAO 返回留言对象 | 返回 DAO 提供的 `Message`，并调用 `getOne(1)` 1 次 | 返回 `Message`，`getOne(1)` 被调用 1 次 | 正确 |
| MS-02 | 调用 `findByUser("u001", pageable)`，用户有留言 | 返回对应分页结果，并调用 `findAllByUserID("u001", pageable)` | 返回分页结果，DAO 调用参数正确 | 正确 |
| MS-03 | 调用 `findByUser("u001", pageable)`，用户无留言 | 返回空分页结果，并调用 `findAllByUserID("u001", pageable)` | 返回空分页结果，DAO 调用参数正确 | 正确 |
| MS-04 | 调用 `create(message)` 保存留言 | 返回 `save(message)` 后生成的 `messageID` | 返回生成的 `messageID=101` | 正确 |
| MS-05 | 调用 `delById(1)` 删除留言 | 调用 `deleteById(1)` 1 次 | `deleteById(1)` 被调用 1 次 | 正确 |
| MS-06 | 调用 `update(message)` 更新留言 | 调用 `save(message)` 1 次 | `save(message)` 被调用 1 次 | 正确 |
| MS-07 | 调用 `confirmMessage(1)`，留言存在 | 调用 `updateState(STATE_PASS, 1)` 1 次 | `updateState(STATE_PASS, 1)` 被调用 1 次 | 正确 |
| MS-08 | 调用 `confirmMessage(404)`，留言不存在 | 抛出 `RuntimeException`，且不调用 `updateState` | 抛出 `RuntimeException`，`updateState` 未被调用 | 正确 |
| MS-09 | 调用 `rejectMessage(1)`，留言存在 | 调用 `updateState(STATE_REJECT, 1)` 1 次 | `updateState(STATE_REJECT, 1)` 被调用 1 次 | 正确 |
| MS-10 | 调用 `rejectMessage(404)`，留言不存在 | 抛出 `RuntimeException`，且不调用 `updateState` | 抛出 `RuntimeException`，`updateState` 未被调用 | 正确 |
| MS-11 | 调用 `findWaitState(pageable)` 查询待审核留言 | 按 `STATE_NO_AUDIT` 查询并返回分页结果 | 按 `STATE_NO_AUDIT` 完成查询并返回结果 | 正确 |
| MS-12 | 调用 `findPassState(pageable)` 查询已通过留言 | 按 `STATE_PASS` 查询并返回分页结果 | 按 `STATE_PASS` 完成查询并返回结果 | 正确 |

### 相关代码片段

#### MS-01
```java
// Service
public Message findById(int messageID) {
    return messageDao.getOne(messageID);
}

// Test
when(messageDao.getOne(1)).thenReturn(message);
Message result = messageService.findById(1);
assertSame(message, result);
verify(messageDao).getOne(1);
```

#### MS-02
```java
// Service
public Page<Message> findByUser(String userID, Pageable pageable) {
    Page<Message> page = messageDao.findAllByUserID(userID, pageable);
    return page;
}

// Test
when(messageDao.findAllByUserID("u001", pageable)).thenReturn(expectedPage);
Page<Message> result = messageService.findByUser("u001", pageable);
assertSame(expectedPage, result);
verify(messageDao).findAllByUserID("u001", pageable);
```

#### MS-03
```java
// Service
public Page<Message> findByUser(String userID, Pageable pageable) {
    Page<Message> page = messageDao.findAllByUserID(userID, pageable);
    return page;
}

// Test
when(messageDao.findAllByUserID("u001", pageable)).thenReturn(emptyPage);
Page<Message> result = messageService.findByUser("u001", pageable);
assertSame(emptyPage, result);
verify(messageDao).findAllByUserID("u001", pageable);
```

#### MS-04
```java
// Service
public int create(Message message) {
    return messageDao.save(message).getMessageID();
}

// Test
when(messageDao.save(toCreate)).thenReturn(saved);
int generatedId = messageService.create(toCreate);
assertEquals(101, generatedId);
verify(messageDao).save(toCreate);
```

#### MS-05
```java
// Service
public void delById(int messageID) {
    messageDao.deleteById(messageID);
}

// Test
messageService.delById(1);
verify(messageDao).deleteById(1);
```

#### MS-06
```java
// Service
public void update(Message message) {
    messageDao.save(message);
}

// Test
messageService.update(message);
verify(messageDao).save(message);
```

#### MS-07
```java
// Service
public void confirmMessage(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    if (message == null) {
        throw new RuntimeException("留言不存在");
    }
    messageDao.updateState(STATE_PASS, message.getMessageID());
}

// Test
when(messageDao.findByMessageID(1)).thenReturn(message);
messageService.confirmMessage(1);
verify(messageDao).updateState(MessageService.STATE_PASS, 1);
```

#### MS-08
```java
// Service
public void confirmMessage(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    if (message == null) {
        throw new RuntimeException("留言不存在");
    }
    messageDao.updateState(STATE_PASS, message.getMessageID());
}

// Test
when(messageDao.findByMessageID(404)).thenReturn(null);
assertThrows(RuntimeException.class, () -> messageService.confirmMessage(404));
verify(messageDao, never()).updateState(MessageService.STATE_PASS, 404);
```

#### MS-09
```java
// Service
public void rejectMessage(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    if (message == null) {
        throw new RuntimeException("留言不存在");
    }
    messageDao.updateState(STATE_REJECT, message.getMessageID());
}

// Test
when(messageDao.findByMessageID(1)).thenReturn(message);
messageService.rejectMessage(1);
verify(messageDao).updateState(MessageService.STATE_REJECT, 1);
```

#### MS-10
```java
// Service
public void rejectMessage(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    if (message == null) {
        throw new RuntimeException("留言不存在");
    }
    messageDao.updateState(STATE_REJECT, message.getMessageID());
}

// Test
when(messageDao.findByMessageID(404)).thenReturn(null);
assertThrows(RuntimeException.class, () -> messageService.rejectMessage(404));
verify(messageDao, never()).updateState(MessageService.STATE_REJECT, 404);
```

#### MS-11
```java
// Service
public Page<Message> findWaitState(Pageable pageable) {
    return messageDao.findAllByState(STATE_NO_AUDIT, pageable);
}

// Test
when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, pageable)).thenReturn(expectedPage);
Page<Message> result = messageService.findWaitState(pageable);
assertSame(expectedPage, result);
verify(messageDao).findAllByState(MessageService.STATE_NO_AUDIT, pageable);
```

#### MS-12
```java
// Service
public Page<Message> findPassState(Pageable pageable) {
    return messageDao.findAllByState(STATE_PASS, pageable);
}

// Test
when(messageDao.findAllByState(MessageService.STATE_PASS, pageable)).thenReturn(expectedPage);
Page<Message> result = messageService.findPassState(pageable);
assertSame(expectedPage, result);
verify(messageDao).findAllByState(MessageService.STATE_PASS, pageable);
```

## 2. MessageVoServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.MessageVoServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.MessageVoServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| MVS-01 | 调用 `returnMessageVoByMessageID(1)`，留言和用户都存在 | 组装出字段正确的 `MessageVo` | `MessageVo` 字段与留言、用户信息一致 | 正确 |
| MVS-02 | 调用 `returnMessageVoByMessageID(404)`，留言不存在 | 抛出 `NullPointerException`，且不查询用户 | 抛出 `NullPointerException`，`userDao` 未被调用 | 正确 |
| MVS-03 | 调用 `returnMessageVoByMessageID(1)`，用户不存在 | 抛出 `NullPointerException` | 抛出 `NullPointerException` | 正确 |
| MVS-04 | 调用 `returnVo(Collections.emptyList())` | 返回空列表，且 DAO 无交互 | 返回空列表，`messageDao` 和 `userDao` 无交互 | 正确 |
| MVS-05 | 调用 `returnVo(...)`，输入 1 条留言 | 返回 1 条 `MessageVo`，字段组装正确 | 返回 1 条 `MessageVo`，字段正确 | 正确 |
| MVS-06 | 调用 `returnVo(...)`，输入多条留言 | 返回数量正确且顺序与输入一致的 `MessageVo` 列表 | 返回 2 条 `MessageVo`，顺序与输入一致 | 正确 |

### 相关代码片段

#### MVS-01
```java
// Service
public MessageVo returnMessageVoByMessageID(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    User user = userDao.findByUserID(message.getUserID());
    MessageVo messageVo = new MessageVo(
        message.getMessageID(), user.getUserID(), message.getContent(),
        message.getTime(), user.getUserName(), user.getPicture(), message.getState()
    );
    return messageVo;
}

// Test
when(messageDao.findByMessageID(1)).thenReturn(message);
when(userDao.findByUserID("u001")).thenReturn(user);
MessageVo vo = messageVoService.returnMessageVoByMessageID(1);
assertEquals("Alice", vo.getUserName());
assertEquals("p1.jpg", vo.getPicture());
```

#### MVS-02
```java
// Service
public MessageVo returnMessageVoByMessageID(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    User user = userDao.findByUserID(message.getUserID());
    ...
}

// Test
when(messageDao.findByMessageID(404)).thenReturn(null);
assertThrows(NullPointerException.class, () -> messageVoService.returnMessageVoByMessageID(404));
verify(userDao, never()).findByUserID(anyString());
```

#### MVS-03
```java
// Service
public MessageVo returnMessageVoByMessageID(int messageID) {
    Message message = messageDao.findByMessageID(messageID);
    User user = userDao.findByUserID(message.getUserID());
    MessageVo messageVo = new MessageVo(... user.getUserName(), user.getPicture(), ...);
    return messageVo;
}

// Test
when(messageDao.findByMessageID(1)).thenReturn(message);
when(userDao.findByUserID("u001")).thenReturn(null);
assertThrows(NullPointerException.class, () -> messageVoService.returnMessageVoByMessageID(1));
```

#### MVS-04
```java
// Service
public List<MessageVo> returnVo(List<Message> messages) {
    List<MessageVo> list = new ArrayList<>();
    for (int i = 0; i < messages.size(); i++) {
        list.add(returnMessageVoByMessageID(messages.get(i).getMessageID()));
    }
    return list;
}

// Test
List<MessageVo> result = messageVoService.returnVo(Collections.emptyList());
assertTrue(result.isEmpty());
verifyNoInteractions(messageDao, userDao);
```

#### MVS-05
```java
// Service
public List<MessageVo> returnVo(List<Message> messages) {
    List<MessageVo> list = new ArrayList<>();
    for (int i = 0; i < messages.size(); i++) {
        list.add(returnMessageVoByMessageID(messages.get(i).getMessageID()));
    }
    return list;
}

// Test
when(messageDao.findByMessageID(11)).thenReturn(found);
when(userDao.findByUserID("u011")).thenReturn(user);
List<MessageVo> result = messageVoService.returnVo(Collections.singletonList(input));
assertEquals(1, result.size());
assertEquals("Bob", result.get(0).getUserName());
```

#### MVS-06
```java
// Service
public List<MessageVo> returnVo(List<Message> messages) {
    List<MessageVo> list = new ArrayList<>();
    for (int i = 0; i < messages.size(); i++) {
        list.add(returnMessageVoByMessageID(messages.get(i).getMessageID()));
    }
    return list;
}

// Test
when(messageDao.findByMessageID(1)).thenReturn(found1);
when(messageDao.findByMessageID(2)).thenReturn(found2);
when(userDao.findByUserID("u001")).thenReturn(user1);
when(userDao.findByUserID("u002")).thenReturn(user2);
List<MessageVo> result = messageVoService.returnVo(Arrays.asList(input1, input2));
assertEquals(2, result.size());
assertEquals("Alice", result.get(0).getUserName());
assertEquals("Carol", result.get(1).getUserName());
```

## 3. VenueServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.VenueServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.VenueServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| VS-01 | 调用 `findByVenueID(1)`，DAO 返回场馆对象 | 返回 DAO 提供的 `Venue`，并调用 `getOne(1)` 1 次 | 返回 `Venue`，`getOne(1)` 被调用 1 次 | 正确 |
| VS-02 | 调用 `findByVenueName("Gym")`，场馆存在 | 返回对应场馆对象 | 返回对应场馆对象 | 正确 |
| VS-03 | 调用 `findByVenueName("NotExists")`，场馆不存在 | 返回 `null` | 返回 `null` | 正确 |
| VS-04 | 调用 `findAll(pageable)` 进行分页查询 | 返回分页结果，并调用 `findAll(pageable)` | 返回分页结果，DAO 调用正确 | 正确 |
| VS-05 | 调用 `findAll()` 查询场馆列表 | 返回列表结果，并调用 `findAll()` | 返回列表结果，DAO 调用正确 | 正确 |
| VS-06 | 调用 `create(venue)` 保存场馆 | 返回 `save(venue)` 后生成的 `venueID` | 返回生成的 `venueID=10` | 正确 |
| VS-07 | 调用 `update(venue)` 更新场馆 | 调用 `save(venue)` 1 次 | `save(venue)` 被调用 1 次 | 正确 |
| VS-08 | 调用 `delById(1)` 删除场馆 | 调用 `deleteById(1)` 1 次 | `deleteById(1)` 被调用 1 次 | 正确 |
| VS-09 | 调用 `countVenueName("Gym")`，无重名 | 返回 `0` | 返回 `0` | 正确 |
| VS-10 | 调用 `countVenueName("Gym")`，存在重名 | 返回正数计数 | 返回 `2` | 正确 |

### 相关代码片段

#### VS-01
```java
// Service
public Venue findByVenueID(int id) {
    return venueDao.getOne(id);
}

// Test
when(venueDao.getOne(1)).thenReturn(venue);
Venue result = venueService.findByVenueID(1);
assertSame(venue, result);
verify(venueDao).getOne(1);
```

#### VS-02
```java
// Service
public Venue findByVenueName(String venueName) {
    return venueDao.findByVenueName(venueName);
}

// Test
when(venueDao.findByVenueName("Gym")).thenReturn(venue);
Venue result = venueService.findByVenueName("Gym");
assertSame(venue, result);
verify(venueDao).findByVenueName("Gym");
```

#### VS-03
```java
// Service
public Venue findByVenueName(String venueName) {
    return venueDao.findByVenueName(venueName);
}

// Test
when(venueDao.findByVenueName("NotExists")).thenReturn(null);
Venue result = venueService.findByVenueName("NotExists");
assertNull(result);
verify(venueDao).findByVenueName("NotExists");
```

#### VS-04
```java
// Service
public Page<Venue> findAll(Pageable pageable) {
    return venueDao.findAll(pageable);
}

// Test
when(venueDao.findAll(pageable)).thenReturn(expectedPage);
Page<Venue> result = venueService.findAll(pageable);
assertSame(expectedPage, result);
verify(venueDao).findAll(pageable);
```

#### VS-05
```java
// Service
public List<Venue> findAll() {
    return venueDao.findAll();
}

// Test
when(venueDao.findAll()).thenReturn(venues);
List<Venue> result = venueService.findAll();
assertSame(venues, result);
verify(venueDao).findAll();
```

#### VS-06
```java
// Service
public int create(Venue venue) {
    return venueDao.save(venue).getVenueID();
}

// Test
when(venueDao.save(toCreate)).thenReturn(saved);
int generatedId = venueService.create(toCreate);
assertEquals(10, generatedId);
verify(venueDao).save(toCreate);
```

#### VS-07
```java
// Service
public void update(Venue venue) {
    venueDao.save(venue);
}

// Test
venueService.update(venue);
verify(venueDao).save(venue);
```

#### VS-08
```java
// Service
public void delById(int id) {
    venueDao.deleteById(id);
}

// Test
venueService.delById(1);
verify(venueDao).deleteById(1);
```

#### VS-09
```java
// Service
public int countVenueName(String venueName) {
    return venueDao.countByVenueName(venueName);
}

// Test
when(venueDao.countByVenueName("Gym")).thenReturn(0);
int count = venueService.countVenueName("Gym");
assertEquals(0, count);
verify(venueDao).countByVenueName("Gym");
```

#### VS-10
```java
// Service
public int countVenueName(String venueName) {
    return venueDao.countByVenueName(venueName);
}

// Test
when(venueDao.countByVenueName("Gym")).thenReturn(2);
int count = venueService.countVenueName("Gym");
assertEquals(2, count);
verify(venueDao).countByVenueName("Gym");
```

## 4. NewsServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.NewsServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.NewsServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| NS-01 | 调用 `findAll(pageable)`，存在新闻数据 | 返回分页结果，并调用 `findAll(pageable)` | 返回分页结果，DAO 调用正确 | 正确 |
| NS-02 | 调用 `findAll(pageable)`，不存在新闻数据 | 返回空分页结果 | 返回空分页结果 | 正确 |
| NS-03 | 调用 `findById(1)`，DAO 返回新闻对象 | 返回 DAO 提供的 `News`，并调用 `getOne(1)` 1 次 | 返回 `News`，`getOne(1)` 被调用 1 次 | 正确 |
| NS-04 | 调用 `create(news)` 保存新闻 | 返回 `save(news)` 后生成的 `newsID` | 返回生成的 `newsID=88` | 正确 |
| NS-05 | 调用 `delById(1)` 删除新闻 | 调用 `deleteById(1)` 1 次 | `deleteById(1)` 被调用 1 次 | 正确 |
| NS-06 | 调用 `update(news)` 更新新闻 | 调用 `save(news)` 1 次 | `save(news)` 被调用 1 次 | 正确 |

### 相关代码片段

#### NS-01
```java
// Service
public Page<News> findAll(Pageable pageable) {
    return newsDao.findAll(pageable);
}

// Test
when(newsDao.findAll(pageable)).thenReturn(expectedPage);
Page<News> result = newsService.findAll(pageable);
assertSame(expectedPage, result);
verify(newsDao).findAll(pageable);
```

#### NS-02
```java
// Service
public Page<News> findAll(Pageable pageable) {
    return newsDao.findAll(pageable);
}

// Test
when(newsDao.findAll(pageable)).thenReturn(emptyPage);
Page<News> result = newsService.findAll(pageable);
assertSame(emptyPage, result);
verify(newsDao).findAll(pageable);
```

#### NS-03
```java
// Service
public News findById(int newsID) {
    return newsDao.getOne(newsID);
}

// Test
when(newsDao.getOne(1)).thenReturn(news);
News result = newsService.findById(1);
assertSame(news, result);
verify(newsDao).getOne(1);
```

#### NS-04
```java
// Service
public int create(News news) {
    return newsDao.save(news).getNewsID();
}

// Test
when(newsDao.save(toCreate)).thenReturn(saved);
int generatedId = newsService.create(toCreate);
assertEquals(88, generatedId);
verify(newsDao).save(toCreate);
```

#### NS-05
```java
// Service
public void delById(int newsID) {
    newsDao.deleteById(newsID);
}

// Test
newsService.delById(1);
verify(newsDao).deleteById(1);
```

#### NS-06
```java
// Service
public void update(News news) {
    newsDao.save(news);
}

// Test
newsService.update(news);
verify(newsDao).save(news);
```

---

# 二、单元测试用例 


| 项目 | 结果 |
| --- | --- |
| 执行命令 | `mvn "-Dtest=MessageServiceImplTest,MessageVoServiceImplTest,VenueServiceImplTest,NewsServiceImplTest" test` |
| MessageServiceImplTest | 12/12 通过 |
| MessageVoServiceImplTest | 6/6 通过 |
| VenueServiceImplTest | 10/10 通过 |
| NewsServiceImplTest | 6/6 通过 |
| 总计 | 34/34 通过 |
| Maven 结果 | `BUILD SUCCESS` |

## 5. UserServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| US-01 | **findByUserID**：查询存在的 UserID | 返回对应的 User 对象 | 返回预期目标对象 | 正确 |
| US-02 | **findById**：查询存在的 ID | 返回对应的 User 对象 | 返回预期目标对象 | 正确 |
| US-03 | **findByUserID**：分页查询非管理员用户 | 返回含有用户列表的 Page | 正确获取有效分页数据 | 正确 |
| US-04 | **checkLogin**：用户名和密码完全匹配成功 | 返回该用户 User 对象 | 认证逻辑有效，返回指定对象 | 正确 |
| US-05 | **checkLogin**：用户名存在但密码错误 | 返回 Null | 阻截异常流，返回Null | 正确 |
| US-06 | **create**：传入有效 User 对象进行创建 | 返回模拟保存后的用户总记录数 | DAO存储工作流运行正常 | 正确 |
| US-07 | **delByID**：删除存在的 ID | 调用 dao 的 deleteById | 目标路径调用成功且未抛异常 | 正确 |
| US-08 | **updateUser**：传入修改后的 User 实体 | 触发 dao 的 save 保存逻辑 | 调用链运行无异常 | 正确 |
| US-09 | **countUserID**：查询对应 UserID 目前的数量 | 返回校验后的记录数量整数 | 统计数字断言完全匹配 | 正确 |

### 相关代码片段

#### US-01 (testFindByUserID_WhenUserExists)
```java
// Test: testFindByUserID_WhenUserExists
void testFindByUserID_WhenUserExists() {
        User user = new User(1, "test_user", "nickname", "12", "mail@qq.com", "123", 0, "pic");
        when(userDao.findByUserID("test_user")).thenReturn(user);

        User result = userService.findByUserID("test_user");
        assertSame(user, result);
        verify(userDao).findByUserID("test_user");
```

#### US-02 (testFindById_WhenUserExists)
```java
// Test: testFindById_WhenUserExists
void testFindById_WhenUserExists() {
        User user = new User(1, "test_user", "nickname", "12", "mail@qq.com", "123", 0, "pic");
        when(userDao.findById(1)).thenReturn(user);

        User result = userService.findById(1);
        assertSame(user, result);
        verify(userDao).findById(1);
```

#### US-03 (testFindByUserIDPageable)
```java
// Test: testFindByUserIDPageable
void testFindByUserIDPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(page);

        Page<User> result = userService.findByUserID(pageable);
        assertSame(page, result);
        verify(userDao).findAllByIsadmin(0, pageable);
```

#### US-04 (testCheckLogin_Success)
```java
// Test: testCheckLogin_Success
void testCheckLogin_Success() {
        User user = new User(1, "user", "nickname", "pass", "mail@qq.com", "123", 0, "pic");
        when(userDao.findByUserIDAndPassword("user", "pass")).thenReturn(user);

        User result = userService.checkLogin("user", "pass");
        assertSame(user, result);
        verify(userDao).findByUserIDAndPassword("user", "pass");
```

#### US-05 (testCheckLogin_Fail)
```java
// Test: testCheckLogin_Fail
void testCheckLogin_Fail() {
        when(userDao.findByUserIDAndPassword("user", "wrong")).thenReturn(null);

        User result = userService.checkLogin("user", "wrong");
        assertNull(result);
        verify(userDao).findByUserIDAndPassword("user", "wrong");
```

#### US-06 (testCreate)
```java
// Test: testCreate
void testCreate() {
        User user = new User(1, "name", "name", "pass", "mail", "phone", 0, "pic");
        when(userDao.findAll()).thenReturn(Arrays.asList(user));

        int total = userService.create(user);
        assertEquals(1, total);
        verify(userDao).save(user);
        verify(userDao).findAll();
```

#### US-07 (testDelByID)
```java
// Test: testDelByID
void testDelByID() {
        userService.delByID(1);
        verify(userDao).deleteById(1);
```

#### US-08 (testUpdateUser)
```java
// Test: testUpdateUser
void testUpdateUser() {
        User user = new User(1, "name", "name", "pass", "mail", "phone", 0, "pic");
        userService.updateUser(user);
        verify(userDao).save(user);
```

#### US-09 (testCountUserID)
```java
// Test: testCountUserID
void testCountUserID() {
        when(userDao.countByUserID("user1")).thenReturn(5);
        int count = userService.countUserID("user1");
        assertEquals(5, count);
        verify(userDao).countByUserID("user1");
```

## 6. OrderServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| OS-01 | **findById**：查询已存在订单ID | 返回预期 Order 对象 | 获得完整的业务实体 | 正确 |
| OS-02 | **findDateOrder**：在给定场馆和时间区间查询 | 返回查找到的 Order 列表 | 集合获取完备 | 正确 |
| OS-03 | **findUserOrder**：查询某特定用户的订单并分页 | 返回该用户的 Order 分页数据 | 有效触发并返回预期Page | 正确 |
| OS-04 | **updateOrder**：修改场馆、时间等参数 | 状态更新为未审核并落表重算实付 | 计算正确且保存参数一致 | 正确 |
| OS-05 | **submit**：提交包含有效场馆的新订单 | 验证生成的新订单被构造为对象并写入 | 成功模拟提交交互逻辑 | 正确 |
| OS-06 | **delOrder**：删除某确定的订单ID | 清理业务逻辑有效，调用 dao | 预期被删除 | 正确 |
| OS-07 | **confirmOrder**：确认已被提交的有效订单 | 订单状态修改为 WAIT 并更新执行 | 状态正确更替为等待 | 正确 |
| OS-08 | **confirmOrder**：确认一个无效或空订单 | 主动向外抛出 RuntimeException | 安全特性通过测试 | 正确 |
| OS-09 | **finishOrder**：完结处于正常状态订单 | 订单状态安全地变更为 FINISH | 流程顺利关闭 | 正确 |
| OS-10 | **finishOrder**：对一个根本不存在的订单尝试完结 | 主动向外抛出 RuntimeException | 合法拦截错误操作 | 正确 |
| OS-11 | **rejectOrder**：拒绝已存在的待审核订单 | 订单状态切换到 REJECT(被拒绝) | 异常路径状态流正确 | 正确 |
| OS-12 | **rejectOrder**：试图拒绝空订单触发逻辑防御 | 主动向外抛出抛出 RuntimeException | 成功被服务阻挡 | 正确 |
| OS-13 | **findNoAuditOrder**：获取当前未经过审核的订单分页 | 基于状态为未审核输出 Page | 拉取数据符合目标约束 | 正确 |
| OS-14 | **findAuditOrder**：查询处于 FINISH 或 WAIT 状态订单 | 返回包含已审核的特定 Order 列表 | 复合约束过滤执行正确 | 正确 |

### 相关代码片段

#### OS-01 (testFindById)
```java
// Test: testFindById
void testFindById() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);
        assertSame(order, result);
        verify(orderDao).getOne(1);
```

#### OS-02 (testFindDateOrder)
```java
// Test: testFindDateOrder
void testFindDateOrder() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<Order> list = Collections.emptyList();
        when(orderDao.findByVenueIDAndStartTimeIsBetween(2, start, end)).thenReturn(list);

        List<Order> result = orderService.findDateOrder(2, start, end);
        assertSame(list, result);
        verify(orderDao).findByVenueIDAndStartTimeIsBetween(2, start, end);
```

#### OS-03 (testFindUserOrder)
```java
// Test: testFindUserOrder
void testFindUserOrder() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(orderDao.findAllByUserID("u1", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("u1", pageable);
        assertSame(page, result);
        verify(orderDao).findAllByUserID("u1", pageable);
```

#### OS-04 (testUpdateOrder)
```java
// Test: testUpdateOrder
void testUpdateOrder() {
        LocalDateTime time = LocalDateTime.now();
        Venue venue = new Venue(2, "VenueA", "Desc", 100, "pic", "addr", "08:00", "22:00");
        Order order = new Order(1, "oldU", 1, 0, time, time, 1, 100);

        when(venueDao.findByVenueName("VenueA")).thenReturn(venue);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.updateOrder(1, "VenueA", time, 3, "newU");

        assertEquals(OrderService.STATE_NO_AUDIT, order.getState());
        assertEquals(3, order.getHours());
        assertEquals(2, order.getVenueID());
        assertEquals("newU", order.getUserID());
        assertEquals(300, order.getTotal());
        verify(orderDao).save(order);
```

#### OS-05 (testSubmit)
```java
// Test: testSubmit
void testSubmit() {
        LocalDateTime time = LocalDateTime.now();
        Venue venue = new Venue(2, "VenueA", "Desc", 100, "pic", "addr", "08:00", "22:00");

        when(venueDao.findByVenueName("VenueA")).thenReturn(venue);

        orderService.submit("VenueA", time, 4, "u1");

        verify(orderDao).save(any(Order.class));
```

#### OS-06 (testDelOrder)
```java
// Test: testDelOrder
void testDelOrder() {
        orderService.delOrder(1);
        verify(orderDao).deleteById(1);
```

#### OS-07 (testConfirmOrder_Exists)
```java
// Test: testConfirmOrder_Exists
void testConfirmOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.confirmOrder(1);
        verify(orderDao).updateState(OrderService.STATE_WAIT, 1);
```

#### OS-08 (testConfirmOrder_NotExists)
```java
// Test: testConfirmOrder_NotExists
void testConfirmOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(2));
        verify(orderDao, never()).updateState(any(Integer.class), any(Integer.class));
```

#### OS-09 (testFinishOrder_Exists)
```java
// Test: testFinishOrder_Exists
void testFinishOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_WAIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.finishOrder(1);
        verify(orderDao).updateState(OrderService.STATE_FINISH, 1);
```

#### OS-10 (testFinishOrder_NotExists)
```java
// Test: testFinishOrder_NotExists
void testFinishOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.finishOrder(2));
```

#### OS-11 (testRejectOrder_Exists)
```java
// Test: testRejectOrder_Exists
void testRejectOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.rejectOrder(1);
        verify(orderDao).updateState(OrderService.STATE_REJECT, 1);
```

#### OS-12 (testRejectOrder_NotExists)
```java
// Test: testRejectOrder_NotExists
void testRejectOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.rejectOrder(2));
```

#### OS-13 (testFindNoAuditOrder)
```java
// Test: testFindNoAuditOrder
void testFindNoAuditOrder() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);
        assertSame(page, result);
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
```

#### OS-14 (testFindAuditOrder)
```java
// Test: testFindAuditOrder
void testFindAuditOrder() {
        List<Order> list = Arrays.asList(new Order(), new Order());
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenReturn(list);

        List<Order> result = orderService.findAuditOrder();
        assertSame(list, result);
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
```

## 7. OrderVoServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.OrderVoServiceImpl.java` |
| --- | --- |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderVoServiceImplTest.java` |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| --- | --- | --- | --- | --- |
| OVS-01 | **returnOrderVoByOrderID**：传入包含关联数据的源订单ID | 返回组装含 Venue 外拓数据的 OrderVo | VO转换核心逻辑完整执行 | 正确 |
| OVS-02 | **returnVo**：将大量源实体转换成页面显示视图 | 针对 List 内的每个元素依次触发合并并返回 | 列表长度与内容断言相等 | 正确 |

### 相关代码片段

#### OVS-01 (testReturnOrderVoByOrderID)
```java
// Test: testReturnOrderVoByOrderID
void testReturnOrderVoByOrderID() {
        LocalDateTime time = LocalDateTime.of(2026, 4, 1, 10, 0);
        Order order = new Order(1, "user1", 2, 0, time, time, 2, 200);
        Venue venue = new Venue(2, "VenueA", "Desc", 100, "pic", "addr", "08:00", "22:00");

        when(orderDao.findByOrderID(1)).thenReturn(order);
        when(venueDao.findByVenueID(2)).thenReturn(venue);

        OrderVo result = orderVoService.returnOrderVoByOrderID(1);

        assertNotNull(result);
        assertEquals(1, result.getOrderID());
        assertEquals("user1", result.getUserID());
        assertEquals(2, result.getVenueID());
        assertEquals("VenueA", result.getVenueName());
        assertEquals(0, result.getState());
        assertEquals(2, result.getHours());
        assertEquals(200, result.getTotal());

        verify(orderDao).findByOrderID(1);
        verify(venueDao).findByVenueID(2);
```

#### OVS-02 (testReturnVoList)
```java
// Test: testReturnVoList
void testReturnVoList() {
        LocalDateTime time = LocalDateTime.of(2026, 4, 1, 10, 0);
        Order order1 = new Order(1, "user1", 2, 0, time, time, 2, 200);
        Order order2 = new Order(2, "user2", 2, 0, time, time, 3, 300);
        Venue venue = new Venue(2, "VenueA", "Desc", 100, "pic", "addr", "08:00", "22:00");

        when(orderDao.findByOrderID(1)).thenReturn(order1);
        when(orderDao.findByOrderID(2)).thenReturn(order2);
        when(venueDao.findByVenueID(2)).thenReturn(venue);

        List<Order> orders = Arrays.asList(order1, order2);
        List<OrderVo> result = orderVoService.returnVo(orders);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderID());
        assertEquals(2, result.get(1).getOrderID());

        verify(orderDao).findByOrderID(1);
        verify(orderDao).findByOrderID(2);
        verify(venueDao, org.mockito.Mockito.times(2)).findByVenueID(2);
```

## 8. 整体测试结果汇总

| 项目 | 结果 |
| --- | --- |
| 执行命令 | `mvn "-Dtest=UserServiceImplTest,OrderServiceImplTest,OrderVoServiceImplTest" test` |
| UserServiceImplTest | 9/9 通过 |
| OrderServiceImplTest | 14/14 通过 |
| OrderVoServiceImplTest | 2/2 通过 |
| 总计 | 25/25 通过 |
| Maven 结果 | `BUILD SUCCESS` |
