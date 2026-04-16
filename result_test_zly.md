# 一、单元测试用例

## 1. UserServiceImpl

### US-01

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:findByUserID(String userID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testFindByUserID_WhenUserExists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-01 | 触发 `testFindByUserID_WhenUserExists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testFindByUserID_WhenUserExists
void testFindByUserID_WhenUserExists() {
        User user = new User(1, "test_user", "nickname", "12", "mail@qq.com", "123", 0, "pic");
        when(userDao.findByUserID("test_user")).thenReturn(user);

        User result = userService.findByUserID("test_user");
        assertSame(user, result);
        verify(userDao).findByUserID("test_user");
```

### US-02

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:findById(int id)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testFindById_WhenUserExists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-02 | 触发 `testFindById_WhenUserExists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testFindById_WhenUserExists
void testFindById_WhenUserExists() {
        User user = new User(1, "test_user", "nickname", "12", "mail@qq.com", "123", 0, "pic");
        when(userDao.findById(1)).thenReturn(user);

        User result = userService.findById(1);
        assertSame(user, result);
        verify(userDao).findById(1);
```

### US-03

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:findByUserID(Pageable pageable)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testFindByUserIDPageable()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-03 | 触发 `testFindByUserIDPageable()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### US-04

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:checkLogin(String userID, String password)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testCheckLogin_Success()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-04 | 触发 `testCheckLogin_Success()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testCheckLogin_Success
void testCheckLogin_Success() {
        User user = new User(1, "user", "nickname", "pass", "mail@qq.com", "123", 0, "pic");
        when(userDao.findByUserIDAndPassword("user", "pass")).thenReturn(user);

        User result = userService.checkLogin("user", "pass");
        assertSame(user, result);
        verify(userDao).findByUserIDAndPassword("user", "pass");
```

### US-05

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:checkLogin(String userID, String password)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testCheckLogin_Fail()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-05 | 触发 `testCheckLogin_Fail()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testCheckLogin_Fail
void testCheckLogin_Fail() {
        when(userDao.findByUserIDAndPassword("user", "wrong")).thenReturn(null);

        User result = userService.checkLogin("user", "wrong");
        assertNull(result);
        verify(userDao).findByUserIDAndPassword("user", "wrong");
```

### US-06

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:create(User user)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testCreate()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-06 | 触发 `testCreate()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### US-07

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:delByID(int id)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testDelByID()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-07 | 触发 `testDelByID()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testDelByID
void testDelByID() {
        userService.delByID(1);
        verify(userDao).deleteById(1);
```

### US-08

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:updateUser(User user)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testUpdateUser()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-08 | 触发 `testUpdateUser()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testUpdateUser
void testUpdateUser() {
        User user = new User(1, "name", "name", "pass", "mail", "phone", 0, "pic");
        userService.updateUser(user);
        verify(userDao).save(user);
```

### US-09

| 测试对象 | `src.main.java.com.demo.service.impl.UserServiceImpl.java:countUserID(String userID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.UserServiceImplTest.java:testCountUserID()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| US-09 | 触发 `testCountUserID()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testCountUserID
void testCountUserID() {
        when(userDao.countByUserID("user1")).thenReturn(5);
        int count = userService.countUserID("user1");
        assertEquals(5, count);
        verify(userDao).countByUserID("user1");
```

## 2. OrderServiceImpl

### OS-01

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:findById(int OrderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFindById()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-01 | 触发 `testFindById()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testFindById
void testFindById() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);
        assertSame(order, result);
        verify(orderDao).getOne(1);
```

### OS-02

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:findDateOrder(int venueID, LocalDateTime startTime, LocalDateTime startTime2)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFindDateOrder()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-02 | 触发 `testFindDateOrder()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### OS-03

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:findUserOrder(String userID, Pageable pageable)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFindUserOrder()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-03 | 触发 `testFindUserOrder()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### OS-04

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:updateOrder(int orderID, String venueName, LocalDateTime startTime, int hours, String userID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testUpdateOrder()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-04 | 触发 `testUpdateOrder()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### OS-05

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:submit(String venueName, LocalDateTime startTime, int hours, String userID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testSubmit()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-05 | 触发 `testSubmit()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testSubmit
void testSubmit() {
        LocalDateTime time = LocalDateTime.now();
        Venue venue = new Venue(2, "VenueA", "Desc", 100, "pic", "addr", "08:00", "22:00");

        when(venueDao.findByVenueName("VenueA")).thenReturn(venue);

        orderService.submit("VenueA", time, 4, "u1");

        verify(orderDao).save(any(Order.class));
```

### OS-06

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:delOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testDelOrder()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-06 | 触发 `testDelOrder()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testDelOrder
void testDelOrder() {
        orderService.delOrder(1);
        verify(orderDao).deleteById(1);
```

### OS-07

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:confirmOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testConfirmOrder_Exists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-07 | 触发 `testConfirmOrder_Exists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testConfirmOrder_Exists
void testConfirmOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.confirmOrder(1);
        verify(orderDao).updateState(OrderService.STATE_WAIT, 1);
```

### OS-08

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:confirmOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testConfirmOrder_NotExists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-08 | 触发 `testConfirmOrder_NotExists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testConfirmOrder_NotExists
void testConfirmOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(2));
        verify(orderDao, never()).updateState(any(Integer.class), any(Integer.class));
```

### OS-09

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:finishOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFinishOrder_Exists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-09 | 触发 `testFinishOrder_Exists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testFinishOrder_Exists
void testFinishOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_WAIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.finishOrder(1);
        verify(orderDao).updateState(OrderService.STATE_FINISH, 1);
```

### OS-10

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:finishOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFinishOrder_NotExists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-10 | 触发 `testFinishOrder_NotExists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testFinishOrder_NotExists
void testFinishOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.finishOrder(2));
```

### OS-11

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:rejectOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testRejectOrder_Exists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-11 | 触发 `testRejectOrder_Exists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testRejectOrder_Exists
void testRejectOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.rejectOrder(1);
        verify(orderDao).updateState(OrderService.STATE_REJECT, 1);
```

### OS-12

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:rejectOrder(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testRejectOrder_NotExists()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-12 | 触发 `testRejectOrder_NotExists()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
```java
// Test: testRejectOrder_NotExists
void testRejectOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.rejectOrder(2));
```

### OS-13

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:findNoAuditOrder(Pageable pageable)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFindNoAuditOrder()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-13 | 触发 `testFindNoAuditOrder()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### OS-14

| 测试对象 | `src.main.java.com.demo.service.impl.OrderServiceImpl.java:findAuditOrder()` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderServiceImplTest.java:testFindAuditOrder()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OS-14 | 触发 `testFindAuditOrder()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### OVS-01

| 测试对象 | `src.main.java.com.demo.service.impl.OrderVoServiceImpl.java:returnOrderVoByOrderID(int orderID)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderVoServiceImplTest.java:testReturnOrderVoByOrderID()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OVS-01 | 触发 `testReturnOrderVoByOrderID()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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

### OVS-02

| 测试对象 | `src.main.java.com.demo.service.impl.OrderVoServiceImpl.java:returnVo(List<Order> list)` |
| -------- | ------------------------------------------------------------ |
| 测试函数 | `src.test.java.com.demo.service.impl.OrderVoServiceImplTest.java:testReturnVoList()` |
| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
| -------- | -------- | -------- | -------- | ---- |
| OVS-02 | 触发 `testReturnVoList()` 执行逻辑 | 验证交互与功能 | 验证通过 | 正确 |

#### 相关代码片段
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
