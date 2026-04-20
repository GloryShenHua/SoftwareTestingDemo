package com.demo.controller;

import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class IndexControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    @MockBean
    private VenueService venueService;

    @MockBean
    private MessageVoService messageVoService;

    @MockBean
    private MessageService messageService;

    @Test
    @DisplayName("GET /index: 等价类-正常数据，应加载首页并组装四类模型数据")
    void indexShouldRenderWithNormalData() throws Exception {
        List<Venue> venues = Arrays.asList(
                new Venue(1, "A馆", "d1", 80, "", "addr1", "08:00", "22:00"),
                new Venue(2, "B馆", "d2", 100, "", "addr2", "09:00", "21:00")
        );
        List<News> news = Arrays.asList(
                new News(1, "n1", "c1", LocalDateTime.of(2026, 4, 8, 10, 0)),
                new News(2, "n2", "c2", LocalDateTime.of(2026, 4, 8, 11, 0))
        );
        List<Message> messages = Arrays.asList(
                new Message(1, "u1", "m1", LocalDateTime.of(2026, 4, 8, 12, 0), 2),
                new Message(2, "u2", "m2", LocalDateTime.of(2026, 4, 8, 13, 0), 2)
        );
        List<MessageVo> messageVos = Arrays.asList(
                new MessageVo(1, "u1", "m1", LocalDateTime.of(2026, 4, 8, 12, 0), "Tom", "p1", 2),
                new MessageVo(2, "u2", "m2", LocalDateTime.of(2026, 4, 8, 13, 0), "Jerry", "p2", 2)
        );

        when(venueService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(venues));
        when(newsService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(news));
        when(messageService.findPassState(any(Pageable.class))).thenReturn(new PageImpl<>(messages));
        when(messageVoService.returnVo(messages)).thenReturn(messageVos);

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("user", (Object) null))
                .andExpect(model().attribute("venue_list", venues))
                .andExpect(model().attribute("news_list", news))
                .andExpect(model().attribute("message_list", messageVos));

        verify(venueService).findAll(any(Pageable.class));
        verify(newsService).findAll(any(Pageable.class));
        verify(messageService).findPassState(any(Pageable.class));
        verify(messageVoService).returnVo(messages);
    }

    @Test
    @DisplayName("GET /index: 边界值-空数据集合，应正常返回首页")
    void indexShouldRenderWithEmptyData() throws Exception {
        when(venueService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(newsService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(messageService.findPassState(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(messageVoService.returnVo(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("venue_list", Collections.emptyList()))
                .andExpect(model().attribute("news_list", Collections.emptyList()))
                .andExpect(model().attribute("message_list", Collections.emptyList()));
    }

    @Test
    @DisplayName("GET /admin_index: 应返回管理员首页")
    void adminIndexShouldReturnAdminHomeView() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));
    }

    @Test
    @DisplayName("GET /index: 语句覆盖-验证四个服务方法按代码顺序依次调用且model.user=null")
    void indexShouldInvokeAllServicesInOrderAndSetNullUser() throws Exception {
        when(venueService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(newsService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(messageService.findPassState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(messageVoService.returnVo(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", (Object) null));

        InOrder inOrder = inOrder(venueService, newsService, messageService, messageVoService);
        inOrder.verify(venueService).findAll(any(Pageable.class));
        inOrder.verify(newsService).findAll(any(Pageable.class));
        inOrder.verify(messageService).findPassState(any(Pageable.class));
        inOrder.verify(messageVoService).returnVo(Collections.emptyList());
    }
}
