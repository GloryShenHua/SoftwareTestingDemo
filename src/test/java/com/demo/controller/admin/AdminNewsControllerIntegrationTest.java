package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AdminNewsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    @Test
    @DisplayName("GET /news_manage: 应返回新闻管理页并携带总页数")
    void newsManageShouldReturnViewAndTotalPages() throws Exception {
        Page<News> page = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 23);
        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attribute("total", 1));

        verify(newsService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /news_add: 应返回新增页面")
    void newsAddShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    @Test
    @DisplayName("GET /news_edit: 有效newsID时应返回编辑页并携带news")
    void newsEditShouldReturnEditViewAndNews() throws Exception {
        News news = new News(9, "title-9", "content-9", LocalDateTime.of(2026, 4, 8, 12, 0));
        when(newsService.findById(9)).thenReturn(news);

        mockMvc.perform(get("/news_edit").param("newsID", "9"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attribute("news", news));

        verify(newsService).findById(9);
    }

    @Test
    @DisplayName("GET /newsList.do?page=1: 等价类-有效页码，应返回新闻列表")
    void newsListShouldReturnNewsForValidPage() throws Exception {
        List<News> list = Arrays.asList(
                new News(1, "n1", "c1", LocalDateTime.of(2026, 4, 1, 10, 0)),
                new News(2, "n2", "c2", LocalDateTime.of(2026, 4, 1, 11, 0))
        );
        when(newsService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(list, Pageable.unpaged(), list.size()));

        mockMvc.perform(get("/newsList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].newsID").value(1))
                .andExpect(jsonPath("$[1].title").value("n2"));

        verify(newsService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /newsList.do: 边界值-缺省页码，默认按第1页处理")
    void newsListShouldUseDefaultPageWhenMissing() throws Exception {
        when(newsService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0));

        mockMvc.perform(get("/newsList.do"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /newsList.do?page=0: 边界值-非法页码，应抛IllegalArgumentException")
    void newsListShouldReturnBadRequestWhenPageIsZero() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(get("/newsList.do").param("page", "0"))
        );
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("POST /delNews.do: 有效newsID时返回true并调用删除服务")
    void delNewsShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(newsService).delById(3);

        mockMvc.perform(post("/delNews.do").param("newsID", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(newsService).delById(3);
    }

    @Test
    @DisplayName("POST /modifyNews.do: 有效输入时应更新并重定向")
    void modifyNewsShouldUpdateAndRedirect() throws Exception {
        News news = new News(5, "old", "old-content", LocalDateTime.of(2026, 1, 1, 0, 0));
        when(newsService.findById(5)).thenReturn(news);
        doNothing().when(newsService).update(any(News.class));

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "5")
                        .param("title", "new-title")
                        .param("content", "new-content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "news_manage"));

        verify(newsService).findById(5);
        verify(newsService).update(any(News.class));
    }

    @Test
    @DisplayName("POST /addNews.do: 等价类-正常输入，应创建并重定向")
    void addNewsShouldCreateAndRedirect() throws Exception {
        when(newsService.create(any(News.class))).thenReturn(1);

        mockMvc.perform(post("/addNews.do")
                        .param("title", "hello")
                        .param("content", "world"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "news_manage"));

        verify(newsService).create(any(News.class));
    }

    @Test
    @DisplayName("POST /modifyNews.do: 边界值-缺失newsID参数，应抛IllegalStateException")
    void modifyNewsShouldFailWhenMissingNewsId() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(post("/modifyNews.do")
                        .param("title", "t")
                        .param("content", "c"))
        );
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("POST /addNews.do: 语句覆盖-验证News对象title/content/time字段均被赋值后调用create")
    void addNewsShouldSetAllFieldsBeforeCreate() throws Exception {
        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        when(newsService.create(captor.capture())).thenReturn(1);

        mockMvc.perform(post("/addNews.do")
                        .param("title", "白盒标题")
                        .param("content", "白盒内容"))
                .andExpect(status().is3xxRedirection());

        News captured = captor.getValue();
        assertEquals("白盒标题", captured.getTitle());
        assertEquals("白盒内容", captured.getContent());
        assertNotNull(captured.getTime(), "time 字段应由 setTime(LocalDateTime.now()) 语句赋值，不应为 null");
    }

    @Test
    @DisplayName("POST /modifyNews.do: 语句覆盖-验证setTitle/setContent/setTime三条语句均被执行")
    void modifyNewsShouldUpdateAllFieldsBeforeUpdate() throws Exception {
        News existing = new News(7, "原标题", "原内容", LocalDateTime.of(2026, 1, 1, 0, 0));
        when(newsService.findById(7)).thenReturn(existing);
        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        doNothing().when(newsService).update(captor.capture());

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "7")
                        .param("title", "新标题")
                        .param("content", "新内容"))
                .andExpect(status().is3xxRedirection());

        News updated = captor.getValue();
        assertEquals("新标题", updated.getTitle());
        assertEquals("新内容", updated.getContent());
        assertNotNull(updated.getTime(), "modifyNews 中 setTime(LocalDateTime.now()) 语句应已执行");
        assertTrue(updated.getTime().isAfter(LocalDateTime.of(2026, 1, 1, 0, 0)),
                "更新后的时间应晚于原始时间");
    }
}
