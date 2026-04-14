package com.demo.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class NewsControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldRenderNewsDetailPage() throws Exception {
        mockMvc.perform(get("/news").param("newsID", String.valueOf(newsA.getNewsID())))
                .andExpect(status().isOk())
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
    void shouldFailWhenNewsPageOutOfLowerBoundary() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/news/getNewsList").param("page", "0")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRenderNewsListPage() throws Exception {
        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("news_list"));
    }
}
