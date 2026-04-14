package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.exception.LoginException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class OrderControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldRequireLoginForOrderManage() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/order_manage")))
                .hasCauseInstanceOf(LoginException.class);
    }

    @Test
    void shouldLoadOrderManageForLoggedInUser() throws Exception {
        mockMvc.perform(get("/order_manage").session(userSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("order_manage"));
    }

    @Test
    void shouldRenderOrderPlaceWithVenueInfo() throws Exception {
        mockMvc.perform(get("/order_place.do").param("venueID", String.valueOf(venueA.getVenueID())))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"));
    }

    @Test
    void shouldReturnCurrentUserOrderList() throws Exception {
        mockMvc.perform(get("/getOrderList.do")
                        .param("page", "1")
                        .session(userSession()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderID").value(userOrder.getOrderID()));
    }

    @Test
    void shouldAddOrderWithBoundaryHourValue() throws Exception {
        int before = orderDao.findAll().size();

        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 11:00")
                        .param("hours", "1")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        assertThat(orderDao.findAll().size()).isEqualTo(before + 1);
        Order added = orderDao.findAll().stream()
                .filter(order -> order.getVenueID() == venueB.getVenueID() && order.getHours() == 1)
                .findFirst()
                .orElse(null);
        assertThat(added).isNotNull();
        assertThat(added.getTotal()).isEqualTo(venueB.getPrice());
    }

    @Test
    void shouldFinishOrderAndPersistState() throws Exception {
        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", String.valueOf(userOrder.getOrderID())))
                .andExpect(status().isOk());

        Order finished = orderDao.findByOrderID(userOrder.getOrderID());
        assertThat(finished.getState()).isEqualTo(3);
    }

    @Test
    void shouldModifyOrderAndReturnTrue() throws Exception {
        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-22")
                        .param("startTime", "2026-01-22 13:00")
                        .param("hours", "3")
                        .param("orderID", String.valueOf(userOrder.getOrderID()))
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        Order updated = orderDao.findByOrderID(userOrder.getOrderID());
        assertThat(updated.getVenueID()).isEqualTo(venueB.getVenueID());
        assertThat(updated.getHours()).isEqualTo(3);
        assertThat(updated.getStartTime()).isEqualTo(LocalDateTime.of(2026, 1, 22, 13, 0));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", String.valueOf(userOrder.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertThat(orderDao.findByOrderID(userOrder.getOrderID())).isNull();
    }

    @Test
    void shouldReturnVenueOrderInDateRange() throws Exception {
        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", venueA.getVenueName())
                        .param("date", "2026-01-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue.venueID").value(venueA.getVenueID()))
                .andExpect(jsonPath("$.orders.length()").value(1))
                .andExpect(jsonPath("$.orders[0].orderID").value(userOrder.getOrderID()));
    }
}
