package com.demo.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class VenueControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldRenderVenueDetailPage() throws Exception {
        mockMvc.perform(get("/venue").param("venueID", String.valueOf(venueA.getVenueID())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", org.hamcrest.Matchers.hasProperty("venueID", org.hamcrest.Matchers.is(venueA.getVenueID()))))
                .andExpect(model().attribute("venue", org.hamcrest.Matchers.hasProperty("venueName", org.hamcrest.Matchers.is(venueA.getVenueName()))))
                .andExpect(view().name("venue"));
    }

    @Test
    void shouldReturnVenuePageForValidInput() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].venueID").value(venueA.getVenueID()))
                .andExpect(jsonPath("$.content[0].venueName").value("场馆A"))
                .andExpect(jsonPath("$.content[1].venueID").value(venueB.getVenueID()))
                .andExpect(jsonPath("$.content[1].venueName").value("场馆B"));
    }

    @Test
    void shouldReturnDefaultFirstPageWhenPageMissing() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldFailWhenVenuePageOutOfBoundary() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/venuelist/getVenueList").param("page", "0")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailWhenVenuePageIsNegative() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/venuelist/getVenueList").param("page", "-1")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnEmptyContentWhenVenuePageIsTooLarge() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenVenuePageIsNotNumber() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenVenueIdDoesNotExist() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/venue").param("venueID", "999999")))
                .hasRootCauseInstanceOf(javax.persistence.EntityNotFoundException.class);
    }

    @Test
    void shouldFailWhenVenueIdMissing() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/venue")))
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnBadRequestWhenVenueIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/venue").param("venueID", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRenderVenueListPage() throws Exception {
        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 1))
                .andExpect(model().attribute("venue_list", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(model().attribute("venue_list", org.hamcrest.Matchers.contains(
                        org.hamcrest.Matchers.hasProperty("venueName", org.hamcrest.Matchers.is("场馆A")),
                        org.hamcrest.Matchers.hasProperty("venueName", org.hamcrest.Matchers.is("场馆B"))
                )))
                .andExpect(view().name("venue_list"));
    }
}
