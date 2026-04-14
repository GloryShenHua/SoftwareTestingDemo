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
    void shouldReturnDefaultFirstPageWhenPageParameterMissing() throws Exception {
        mockMvc.perform(get("/message/getMessageList"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].messageID").value(passMessage.getMessageID()));
    }

    @Test
    void shouldFailWhenPageParameterIsNegative() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/message/getMessageList").param("page", "-1")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnEmptyListWhenPageParameterIsTooLarge() throws Exception {
        mockMvc.perform(get("/message/getMessageList").param("page", "9999"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenPageParameterIsNotNumber() throws Exception {
        mockMvc.perform(get("/message/getMessageList").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUserMessagesOnlyForCurrentSessionUser() throws Exception {
        mockMvc.perform(get("/message/findUserList")
                        .param("page", "1")
                        .session(userSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userID").value(normalUser.getUserID()))
                .andExpect(jsonPath("$[1].userID").value(normalUser.getUserID()))
                .andExpect(jsonPath("$[0].state").value(1))
                .andExpect(jsonPath("$[1].state").value(2));
    }

    @Test
    void shouldRequireLoginForFindUserList() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/message/findUserList").param("page", "1")))
                .hasCauseInstanceOf(LoginException.class);
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
    void shouldAllowEmptyContentAndPersistMessageAsCurrentBehavior() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", normalUser.getUserID())
                        .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        Message created = messageDao.findAll().stream()
                .filter(m -> normalUser.getUserID().equals(m.getUserID()) && "".equals(m.getContent()))
                .findFirst()
                .orElse(null);
        assertThat(created).isNotNull();
    }

    @Test
    void shouldAllowMissingContentAndPersistNullAsCurrentBehavior() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", normalUser.getUserID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        Message created = messageDao.findAll().stream()
                .filter(m -> normalUser.getUserID().equals(m.getUserID()) && m.getContent() == null)
                .findFirst()
                .orElse(null);
        assertThat(created).isNotNull();
    }

    @Test
    void shouldAllowUnknownUserIdAndPersistAsCurrentBehavior() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "unknown-user")
                        .param("content", "unknown user content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        Message created = messageDao.findAll().stream()
                .filter(m -> "unknown-user".equals(m.getUserID()))
                .findFirst()
                .orElse(null);
        assertThat(created).isNotNull();
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
    void shouldHandleNonExistentMessageIdWhenModifying() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "999999")
                        .param("content", "不存在的留言")))
                .hasRootCauseInstanceOf(javax.persistence.EntityNotFoundException.class);
    }

    @Test
    void shouldAllowEmptyAndMissingContentWhenModifyingAsCurrentBehavior() throws Exception {
        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", String.valueOf(passMessage.getMessageID()))
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Message updatedEmpty = messageDao.findByMessageID(passMessage.getMessageID());
        assertThat(updatedEmpty.getContent()).isEqualTo("");

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", String.valueOf(passMessage.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Message updatedNull = messageDao.findByMessageID(passMessage.getMessageID());
        assertThat(updatedNull.getContent()).isNull();
    }

    @Test
    void shouldDeleteMessageById() throws Exception {
        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", String.valueOf(pendingMessage.getMessageID())))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertThat(messageDao.findByMessageID(pendingMessage.getMessageID())).isNull();
    }

    @Test
    void shouldThrowWhenDeletingNonExistentMessage() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/delMessage.do")
                        .param("messageID", "999999")))
                .hasRootCauseInstanceOf(org.springframework.dao.EmptyResultDataAccessException.class);
    }
}
