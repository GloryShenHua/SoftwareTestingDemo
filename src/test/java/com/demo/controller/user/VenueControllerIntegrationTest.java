package com.demo.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class VenueControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldRenderVenueDetailPage() throws Exception {
        mockMvc.perform(get("/venue").param("venueID", String.valueOf(venueA.getVenueID())))
                .andExpect(status().isOk())
                .andExpect(view().name("venue"));
    }

    @Test
    void shouldReturnVenuePageForValidInput() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].venueName").value("场馆A"));
    }

    @Test
    void shouldFailWhenVenuePageOutOfBoundary() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/venuelist/getVenueList").param("page", "0")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRenderVenueListPage() throws Exception {
        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"));
    }
}
