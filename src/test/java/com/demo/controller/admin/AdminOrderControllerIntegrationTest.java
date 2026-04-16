package com.demo.controller.admin;

import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AdminOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderVoService orderVoService;

    @Test
    @DisplayName("GET /reservation_manage: 应返回预定管理页并加载待审核总页数")
    void reservationManageShouldReturnViewAndModelData() throws Exception {
        List<Order> auditOrders = Collections.singletonList(
                new Order(1, "u1", 11, 2,
                        LocalDateTime.of(2026, 4, 8, 10, 0),
                        LocalDateTime.of(2026, 4, 9, 10, 0), 2, 120)
        );
        List<OrderVo> orderVos = Collections.singletonList(
                new OrderVo(1, "u1", 11, "A馆", 2,
                        LocalDateTime.of(2026, 4, 8, 10, 0),
                        LocalDateTime.of(2026, 4, 9, 10, 0), 2, 120)
        );
        Page<Order> noAuditPage = new PageImpl<>(Collections.singletonList(auditOrders.get(0)), PageRequest.of(0, 10), 21);

        when(orderService.findAuditOrder()).thenReturn(auditOrders);
        when(orderVoService.returnVo(auditOrders)).thenReturn(orderVos);
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(noAuditPage);

        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attribute("order_list", orderVos))
                .andExpect(model().attribute("total", 3));

        verify(orderService).findAuditOrder();
        verify(orderVoService).returnVo(auditOrders);
        verify(orderService).findNoAuditOrder(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /admin/getOrderList.do?page=1: 等价类-有效页码，应返回待审核订单列表")
    void getNoAuditOrderShouldReturnOrderVoListForValidPage() throws Exception {
        List<Order> orders = Arrays.asList(
                new Order(10, "u10", 1, 1, LocalDateTime.of(2026, 4, 1, 9, 0), LocalDateTime.of(2026, 4, 2, 9, 0), 1, 50),
                new Order(11, "u11", 2, 1, LocalDateTime.of(2026, 4, 1, 10, 0), LocalDateTime.of(2026, 4, 2, 10, 0), 2, 100)
        );
        List<OrderVo> orderVos = Arrays.asList(
                new OrderVo(10, "u10", 1, "场地1", 1, LocalDateTime.of(2026, 4, 1, 9, 0), LocalDateTime.of(2026, 4, 2, 9, 0), 1, 50),
                new OrderVo(11, "u11", 2, "场地2", 1, LocalDateTime.of(2026, 4, 1, 10, 0), LocalDateTime.of(2026, 4, 2, 10, 0), 2, 100)
        );

        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(new PageImpl<>(orders, PageRequest.of(0, 10), orders.size()));
        when(orderVoService.returnVo(orders)).thenReturn(orderVos);

        mockMvc.perform(get("/admin/getOrderList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderID").value(10))
                .andExpect(jsonPath("$[1].venueName").value("场地2"));

        verify(orderVoService).returnVo(orders);
    }

    @Test
    @DisplayName("GET /admin/getOrderList.do: 边界值-缺省页码，默认按第1页处理")
    void getNoAuditOrderShouldUseDefaultPageWhenMissing() throws Exception {
        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
        when(orderVoService.returnVo(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/getOrderList.do"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /admin/getOrderList.do?page=0: 边界值-非法页码，应抛IllegalArgumentException")
    void getNoAuditOrderShouldReturnBadRequestWhenPageIsZero() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(get("/admin/getOrderList.do").param("page", "0"))
        );
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("POST /passOrder.do: 有效orderID时应返回true并调用确认服务")
    void confirmOrderShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(orderService).confirmOrder(100);

        mockMvc.perform(post("/passOrder.do").param("orderID", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).confirmOrder(100);
    }

    @Test
    @DisplayName("POST /rejectOrder.do: 有效orderID时应返回true并调用拒绝服务")
    void rejectOrderShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(orderService).rejectOrder(200);

        mockMvc.perform(post("/rejectOrder.do").param("orderID", "200"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).rejectOrder(200);
    }

    @Test
    @DisplayName("POST /passOrder.do: 边界值-缺失orderID，应抛IllegalStateException")
    void confirmOrderShouldReturnBadRequestWhenOrderIdMissing() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(post("/passOrder.do"))
        );
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }
}
