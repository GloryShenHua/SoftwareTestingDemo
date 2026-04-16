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

class NewsControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldRenderNewsDetailPage() throws Exception {
        mockMvc.perform(get("/news").param("newsID", String.valueOf(newsA.getNewsID())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", org.hamcrest.Matchers.hasProperty("newsID", org.hamcrest.Matchers.is(newsA.getNewsID()))))
                .andExpect(model().attribute("news", org.hamcrest.Matchers.hasProperty("title", org.hamcrest.Matchers.is("新闻A"))))
                .andExpect(view().name("news"));
    }

    @Test
    void shouldReturnNewsPageByValidEquivalenceClass() throws Exception {
        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("新闻B"));
    }

    @Test
    void shouldReturnDefaultFirstPageWhenPageParameterMissing() throws Exception {
        mockMvc.perform(get("/news/getNewsList"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldFailWhenNewsPageOutOfLowerBoundary() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/news/getNewsList").param("page", "0")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailWhenNewsPageIsNegative() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/news/getNewsList").param("page", "-1")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnEmptyContentWhenNewsPageIsOutOfRange() throws Exception {
        mockMvc.perform(get("/news/getNewsList").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenNewsPageIsNotNumber() throws Exception {
        mockMvc.perform(get("/news/getNewsList").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenNewsIdDoesNotExist() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/news").param("newsID", "999999")))
                .hasRootCauseInstanceOf(javax.persistence.EntityNotFoundException.class);
    }

    @Test
    void shouldReturnBadRequestWhenNewsIdIsMissing() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/news")))
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnBadRequestWhenNewsIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/news").param("newsID", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRenderNewsListPage() throws Exception {
        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 1))
                .andExpect(model().attribute("news_list", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(model().attribute("news_list", org.hamcrest.Matchers.contains(
                        org.hamcrest.Matchers.hasProperty("title", org.hamcrest.Matchers.is("新闻B")),
                        org.hamcrest.Matchers.hasProperty("title", org.hamcrest.Matchers.is("新闻A"))
                )))
                .andExpect(view().name("news_list"));
    }
}
