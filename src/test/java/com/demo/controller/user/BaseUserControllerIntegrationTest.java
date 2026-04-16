package com.demo.controller.user;

import com.demo.dao.MessageDao;
import com.demo.dao.NewsDao;
import com.demo.dao.OrderDao;
import com.demo.dao.UserDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseUserControllerIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserDao userDao;
    @Autowired
    protected VenueDao venueDao;
    @Autowired
    protected NewsDao newsDao;
    @Autowired
    protected MessageDao messageDao;
    @Autowired
    protected OrderDao orderDao;

    protected User normalUser;
    protected User adminUser;
    protected Venue venueA;
    protected Venue venueB;
    protected News newsA;
    protected News newsB;
    protected Message passMessage;
    protected Message pendingMessage;
    protected Order userOrder;

    @BeforeEach
    void initData() {
        orderDao.deleteAll();
        messageDao.deleteAll();
        newsDao.deleteAll();
        venueDao.deleteAll();
        userDao.deleteAll();

        normalUser = new User();
        normalUser.setUserID("userA");
        normalUser.setUserName("普通用户A");
        normalUser.setPassword("Pass123");
        normalUser.setEmail("userA@test.com");
        normalUser.setPhone("13800000001");
        normalUser.setIsadmin(0);
        normalUser.setPicture("");
        normalUser = userDao.save(normalUser);

        adminUser = new User();
        adminUser.setUserID("adminA");
        adminUser.setUserName("管理员A");
        adminUser.setPassword("Admin123");
        adminUser.setEmail("adminA@test.com");
        adminUser.setPhone("13800000002");
        adminUser.setIsadmin(1);
        adminUser.setPicture("");
        adminUser = userDao.save(adminUser);

        venueA = new Venue();
        venueA.setVenueName("场馆A");
        venueA.setDescription("场馆A描述");
        venueA.setPrice(200);
        venueA.setPicture("");
        venueA.setAddress("地址A");
        venueA.setOpen_time("09:00");
        venueA.setClose_time("20:00");
        venueA = venueDao.save(venueA);

        venueB = new Venue();
        venueB.setVenueName("场馆B");
        venueB.setDescription("场馆B描述");
        venueB.setPrice(300);
        venueB.setPicture("");
        venueB.setAddress("地址B");
        venueB.setOpen_time("10:00");
        venueB.setClose_time("21:00");
        venueB = venueDao.save(venueB);

        newsA = new News();
        newsA.setTitle("新闻A");
        newsA.setContent("新闻A内容");
        newsA.setTime(LocalDateTime.of(2026, 1, 10, 10, 0));
        newsA = newsDao.save(newsA);

        newsB = new News();
        newsB.setTitle("新闻B");
        newsB.setContent("新闻B内容");
        newsB.setTime(LocalDateTime.of(2026, 1, 11, 10, 0));
        newsB = newsDao.save(newsB);

        passMessage = new Message();
        passMessage.setUserID(normalUser.getUserID());
        passMessage.setContent("已通过留言");
        passMessage.setState(2);
        passMessage.setTime(LocalDateTime.of(2026, 1, 12, 8, 30));
        passMessage = messageDao.save(passMessage);

        pendingMessage = new Message();
        pendingMessage.setUserID(normalUser.getUserID());
        pendingMessage.setContent("待审核留言");
        pendingMessage.setState(1);
        pendingMessage.setTime(LocalDateTime.of(2026, 1, 13, 8, 30));
        pendingMessage = messageDao.save(pendingMessage);

        userOrder = new Order();
        userOrder.setUserID(normalUser.getUserID());
        userOrder.setVenueID(venueA.getVenueID());
        userOrder.setState(2);
        userOrder.setOrderTime(LocalDateTime.of(2026, 1, 14, 9, 0));
        userOrder.setStartTime(LocalDateTime.of(2026, 1, 20, 10, 0));
        userOrder.setHours(2);
        userOrder.setTotal(400);
        userOrder = orderDao.save(userOrder);
    }

    protected MockHttpSession userSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", normalUser);
        return session;
    }

    protected MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", adminUser);
        return session;
    }
}
