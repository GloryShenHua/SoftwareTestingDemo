package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.exception.LoginException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class MessageControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldRequireLoginForMessageListPage() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/message_list")))
                .hasCauseInstanceOf(LoginException.class);
    }

    @Test
    void shouldLoadMessageListPageWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/message_list").session(userSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("message_list"));
    }

    @Test
    void shouldReturnOnlyPassStateMessagesForPublicList() throws Exception {
        mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].messageID").value(passMessage.getMessageID()))
                .andExpect(jsonPath("$[0].state").value(2));
    }

    @Test
    void shouldFailWhenPageIsOutOfBoundary() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/message/getMessageList").param("page", "0")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnUserMessagesOnlyForCurrentSessionUser() throws Exception {
        mockMvc.perform(get("/message/findUserList")
                        .param("page", "1")
                        .session(userSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldCreateNewMessageAndRedirect() throws Exception {
        int before = messageDao.findAll().size();

        mockMvc.perform(post("/sendMessage")
                        .param("userID", normalUser.getUserID())
                        .param("content", "新增留言"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        assertThat(messageDao.findAll().size()).isEqualTo(before + 1);
        Message created = messageDao.findAll().stream()
                .filter(m -> "新增留言".equals(m.getContent()))
                .findFirst()
                .orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getState()).isEqualTo(1);
    }

    @Test
    void shouldModifyExistingMessage() throws Exception {
        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", String.valueOf(passMessage.getMessageID()))
                        .param("content", "更新后内容"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Message updated = messageDao.findByMessageID(passMessage.getMessageID());
        assertThat(updated.getContent()).isEqualTo("更新后内容");
        assertThat(updated.getState()).isEqualTo(1);
    }

    @Test
    void shouldDeleteMessageById() throws Exception {
        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", String.valueOf(pendingMessage.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertThat(messageDao.findByMessageID(pendingMessage.getMessageID())).isNull();
    }
}
