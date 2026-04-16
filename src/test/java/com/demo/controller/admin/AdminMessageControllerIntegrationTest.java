package com.demo.controller.admin;

import com.demo.entity.Message;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.NestedServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class AdminMessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    @Test
    @DisplayName("GET /message_manage: 应返回管理页面并携带总页数字段")
    void messageManageShouldReturnViewAndTotalPages() throws Exception {
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 31);
        when(messageService.findWaitState(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attribute("total", 1));

        verify(messageService).findWaitState(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /messageList.do?page=1: 等价类-有效页码，应返回留言列表")
    void messageListShouldReturnVoListForValidPage() throws Exception {
        List<Message> messages = Arrays.asList(
                new Message(1, "u1", "content-1", LocalDateTime.of(2026, 4, 1, 10, 0), 1),
                new Message(2, "u2", "content-2", LocalDateTime.of(2026, 4, 1, 11, 0), 1)
        );
        List<MessageVo> vos = Arrays.asList(
                new MessageVo(1, "u1", "content-1", LocalDateTime.of(2026, 4, 1, 10, 0), "Tom", "p1.png", 1),
                new MessageVo(2, "u2", "content-2", LocalDateTime.of(2026, 4, 1, 11, 0), "Jerry", "p2.png", 1)
        );
        when(messageService.findWaitState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(messages, Pageable.unpaged(), messages.size()));
        when(messageVoService.returnVo(messages)).thenReturn(vos);

        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].messageID").value(1))
                .andExpect(jsonPath("$[0].userName").value("Tom"))
                .andExpect(jsonPath("$[1].messageID").value(2));

        verify(messageVoService).returnVo(messages);
    }

    @Test
    @DisplayName("GET /messageList.do: 边界值-缺省页码，默认按第1页处理")
    void messageListShouldUseDefaultPageWhenPageIsMissing() throws Exception {
        when(messageService.findWaitState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0));
        when(messageVoService.returnVo(Collections.emptyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/messageList.do"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(messageVoService).returnVo(Collections.emptyList());
    }

        @Test
        @DisplayName("GET /messageList.do?page=0: 边界值-非法页码，应抛IllegalArgumentException")
    void messageListShouldFailWhenPageIsZero() throws Exception {
                NestedServletException exception = assertThrows(
                                NestedServletException.class,
                                () -> mockMvc.perform(get("/messageList.do").param("page", "0"))
                );
                assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("POST /passMessage.do: 有效messageID时应返回true并调用确认服务")
    void passMessageShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(messageService).confirmMessage(100);

        mockMvc.perform(post("/passMessage.do").param("messageID", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).confirmMessage(100);
    }

    @Test
    @DisplayName("POST /rejectMessage.do: 边界值-负数messageID也会透传服务并返回true")
    void rejectMessageShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(messageService).rejectMessage(-1);

        mockMvc.perform(post("/rejectMessage.do").param("messageID", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).rejectMessage(-1);
    }

    @Test
    @DisplayName("REQUEST /delMessage.do: 支持POST删除并返回true")
    void delMessageShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(messageService).delById(200);

        mockMvc.perform(post("/delMessage.do").param("messageID", "200"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).delById(eq(200));
    }
}
