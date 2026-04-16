package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testFindById() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);
        assertSame(order, result);
        verify(orderDao).getOne(1);
    }

    @Test
    void testFindDateOrder() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<Order> list = Collections.emptyList();
        when(orderDao.findByVenueIDAndStartTimeIsBetween(2, start, end)).thenReturn(list);

        List<Order> result = orderService.findDateOrder(2, start, end);
        assertSame(list, result);
        verify(orderDao).findByVenueIDAndStartTimeIsBetween(2, start, end);
    }

    @Test
    void testFindUserOrder() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(orderDao.findAllByUserID("u1", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("u1", pageable);
        assertSame(page, result);
        verify(orderDao).findAllByUserID("u1", pageable);
    }

    @Test
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
    }

    @Test
    void testSubmit() {
        LocalDateTime time = LocalDateTime.now();
        Venue venue = new Venue(2, "VenueA", "Desc", 100, "pic", "addr", "08:00", "22:00");

        when(venueDao.findByVenueName("VenueA")).thenReturn(venue);

        orderService.submit("VenueA", time, 4, "u1");

        verify(orderDao).save(any(Order.class));
    }

    @Test
    void testDelOrder() {
        orderService.delOrder(1);
        verify(orderDao).deleteById(1);
    }

    @Test
    void testConfirmOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.confirmOrder(1);
        verify(orderDao).updateState(OrderService.STATE_WAIT, 1);
    }

    @Test
    void testConfirmOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(2));
        verify(orderDao, never()).updateState(any(Integer.class), any(Integer.class));
    }

    @Test
    void testFinishOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_WAIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.finishOrder(1);
        verify(orderDao).updateState(OrderService.STATE_FINISH, 1);
    }

    @Test
    void testFinishOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.finishOrder(2));
    }

    @Test
    void testRejectOrder_Exists() {
        Order order = new Order(1, "u", 2, OrderService.STATE_NO_AUDIT, LocalDateTime.now(), LocalDateTime.now(), 2, 200);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.rejectOrder(1);
        verify(orderDao).updateState(OrderService.STATE_REJECT, 1);
    }

    @Test
    void testRejectOrder_NotExists() {
        when(orderDao.findByOrderID(2)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.rejectOrder(2));
    }

    @Test
    void testFindNoAuditOrder() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);
        assertSame(page, result);
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    void testFindAuditOrder() {
        List<Order> list = Arrays.asList(new Order(), new Order());
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenReturn(list);

        List<Order> result = orderService.findAuditOrder();
        assertSame(list, result);
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
    }
}
