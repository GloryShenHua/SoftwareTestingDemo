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

## 5. 测试结果汇总

| 项目 | 结果 |
| --- | --- |
| 执行命令 | `mvn "-Dtest=MessageServiceImplTest,MessageVoServiceImplTest,VenueServiceImplTest,NewsServiceImplTest" test` |
| MessageServiceImplTest | 12/12 通过 |
| MessageVoServiceImplTest | 6/6 通过 |
| VenueServiceImplTest | 10/10 通过 |
| NewsServiceImplTest | 6/6 通过 |
| 总计 | 34/34 通过 |
| Maven 结果 | `BUILD SUCCESS` |
