package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderVoServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderVoServiceImpl orderVoService;

    @Test
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
    }

    @Test
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
    }
}
