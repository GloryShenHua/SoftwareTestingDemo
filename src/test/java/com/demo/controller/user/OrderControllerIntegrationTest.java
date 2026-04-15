package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.exception.LoginException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 1))
                .andExpect(view().name("order_manage"));
    }

    @Test
    void shouldRenderOrderPlaceWithVenueInfo() throws Exception {
        mockMvc.perform(get("/order_place.do").param("venueID", String.valueOf(venueA.getVenueID())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", org.hamcrest.Matchers.hasProperty("venueID", org.hamcrest.Matchers.is(venueA.getVenueID()))))
                .andExpect(model().attribute("venue", org.hamcrest.Matchers.hasProperty("venueName", org.hamcrest.Matchers.is(venueA.getVenueName()))))
                .andExpect(view().name("order_place"));
    }

    @Test
    void shouldRenderOrderPlacePageWithoutVenueParam() throws Exception {
        mockMvc.perform(get("/order_place"))
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
                .andExpect(jsonPath("$[0].orderID").value(userOrder.getOrderID()))
                .andExpect(jsonPath("$[0].userID").value(normalUser.getUserID()))
                .andExpect(jsonPath("$[0].state").value(2))
                .andExpect(jsonPath("$[0].venueName").value(venueA.getVenueName()))
                .andExpect(jsonPath("$[0].startTime").value("2026-01-20 10:00:00"));
    }

    @Test
    void shouldRequireLoginForGetOrderList() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/getOrderList.do").param("page", "1")))
                .hasCauseInstanceOf(LoginException.class);
    }

    @Test
    void shouldReturnDefaultOrderPageWhenPageMissing() throws Exception {
        mockMvc.perform(get("/getOrderList.do").session(userSession()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFailWhenGetOrderListPageIsZeroOrNegative() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/getOrderList.do")
                        .param("page", "0")
                        .session(userSession())))
                .hasCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> mockMvc.perform(get("/getOrderList.do")
                        .param("page", "-1")
                        .session(userSession())))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnEmptyListWhenGetOrderListPageIsTooLarge() throws Exception {
        mockMvc.perform(get("/getOrderList.do")
                        .param("page", "9999")
                        .session(userSession()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
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
    void shouldAllowZeroAndNegativeAndLargeHoursAsCurrentBehavior() throws Exception {
        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 12:00")
                        .param("hours", "0")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 13:00")
                        .param("hours", "-1")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 14:00")
                        .param("hours", "24")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        assertThat(orderDao.findAll().stream().anyMatch(order -> order.getHours() == 0)).isTrue();
        assertThat(orderDao.findAll().stream().anyMatch(order -> order.getHours() == -1)).isTrue();
        assertThat(orderDao.findAll().stream().anyMatch(order -> order.getHours() == 24)).isTrue();
    }

    @Test
    void shouldRequireLoginForAddOrder() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 11:00")
                        .param("hours", "1")))
                .hasCauseInstanceOf(LoginException.class);
    }

    @Test
    void shouldFailForInvalidAddOrderInputs() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/addOrder.do")
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 11:00")
                        .param("hours", "1")
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("hours", "1")
                        .session(userSession())))
                .hasRootCauseInstanceOf(java.time.format.DateTimeParseException.class);

        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 11:00")
                        .param("hours", "abc")
                        .session(userSession()))
                .andExpect(status().isBadRequest());

        assertThatThrownBy(() -> mockMvc.perform(post("/addOrder.do")
                        .param("venueName", "不存在的场馆")
                        .param("date", "2026-01-21")
                        .param("startTime", "2026-01-21 11:00")
                        .param("hours", "1")
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldFinishOrderAndPersistState() throws Exception {
        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", String.valueOf(userOrder.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        Order finished = orderDao.findByOrderID(userOrder.getOrderID());
        assertThat(finished.getState()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenFinishOrderNotFound() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/finishOrder.do").param("orderID", "999999")))
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldRenderModifyOrderPageWithModelData() throws Exception {
        mockMvc.perform(get("/modifyOrder.do").param("orderID", String.valueOf(userOrder.getOrderID())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", org.hamcrest.Matchers.hasProperty("orderID", org.hamcrest.Matchers.is(userOrder.getOrderID()))))
                .andExpect(view().name("order_edit"));
    }

    @Test
    void shouldModifyOrderAndRedirectAndPersistChanges() throws Exception {
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
    void shouldRequireLoginForModifyOrder() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-22")
                        .param("startTime", "2026-01-22 13:00")
                        .param("hours", "3")
                        .param("orderID", String.valueOf(userOrder.getOrderID()))))
                .hasCauseInstanceOf(LoginException.class);
    }

    @Test
    void shouldFailForInvalidModifyOrderInputs() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-22")
                        .param("startTime", "2026-01-22 13:00")
                        .param("hours", "3")
                        .param("orderID", "999999")
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-22")
                        .param("hours", "3")
                        .param("orderID", String.valueOf(userOrder.getOrderID()))
                        .session(userSession())))
                .hasRootCauseInstanceOf(java.time.format.DateTimeParseException.class);

        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueB.getVenueName())
                        .param("date", "2026-01-22")
                        .param("startTime", "2026-01-22 13:00")
                        .param("hours", "abc")
                        .param("orderID", String.valueOf(userOrder.getOrderID()))
                        .session(userSession()))
                .andExpect(status().isBadRequest());

        assertThatThrownBy(() -> mockMvc.perform(post("/modifyOrder")
                        .param("venueName", "不存在的场馆")
                        .param("date", "2026-01-22")
                        .param("startTime", "2026-01-22 13:00")
                        .param("hours", "3")
                        .param("orderID", String.valueOf(userOrder.getOrderID()))
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);
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
    void shouldThrowWhenDeletingOrderNotFound() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/delOrder.do").param("orderID", "999999")))
                .hasRootCauseInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void shouldReturnVenueOrderInDateRange() throws Exception {
        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", venueA.getVenueName())
                        .param("date", "2026-01-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue.venueID").value(venueA.getVenueID()))
                .andExpect(jsonPath("$.venue.venueName").value(venueA.getVenueName()))
                .andExpect(jsonPath("$.orders.length()").value(1))
                .andExpect(jsonPath("$.orders[0].orderID").value(userOrder.getOrderID()))
                .andExpect(jsonPath("$.orders[0].startTime").value("2026-01-20T10:00:00"));
    }

    @Test
    void shouldReturnEmptyOrderListForDifferentDate() throws Exception {
        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", venueA.getVenueName())
                        .param("date", "2026-01-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue.venueID").value(venueA.getVenueID()))
                .andExpect(jsonPath("$.orders.length()").value(0));
    }

    @Test
    void shouldFailForInvalidVenueOrderQueryParams() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", "不存在的场馆")
                        .param("date", "2026-01-20")))
                .hasRootCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(get("/order/getOrderList.do")
                        .param("date", "2026-01-20")))
                .hasRootCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", venueA.getVenueName())
                        .param("date", "bad-date")))
                .hasRootCauseInstanceOf(java.time.format.DateTimeParseException.class);

        assertThatThrownBy(() -> mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", venueA.getVenueName())))
                .hasRootCauseInstanceOf(java.time.format.DateTimeParseException.class);
    }
}
