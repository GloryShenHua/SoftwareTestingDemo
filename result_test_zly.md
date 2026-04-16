# 一、单元测试用例

## 1. UserServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
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

## 2. OrderServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
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

## 3. OrderVoServiceImpl

| 测试对象 | `src.main.java.com.demo.service.impl.OrderVoServiceImpl.java` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderVoServiceImplTest.java` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
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

## 4. 测试结果汇总

| 项目 | 结果 |
| --- | --- |
| 执行命令 | `mvn "-Dtest=UserServiceImplTest,OrderServiceImplTest,OrderVoServiceImplTest" test` |
| UserServiceImplTest | 9/9 通过 |
| OrderServiceImplTest | 14/14 通过 |
| OrderVoServiceImplTest | 2/2 通过 |
| 总计 | 25/25 通过 |
| Maven 结果 | `BUILD SUCCESS` |
