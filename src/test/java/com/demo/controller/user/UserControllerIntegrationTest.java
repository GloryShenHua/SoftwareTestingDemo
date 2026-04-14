package com.demo.controller.user;

import com.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class UserControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldOpenSignupAndLoginPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void shouldLoginAsNormalUserAndSetSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", normalUser.getUserID())
                        .param("password", normalUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"))
                .andReturn();

        assertThat(result.getRequest().getSession().getAttribute("user")).isNotNull();
    }

    @Test
    void shouldLoginAsAdminAndSetSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", adminUser.getUserID())
                        .param("password", adminUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"))
                .andReturn();

        assertThat(result.getRequest().getSession().getAttribute("admin")).isNotNull();
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {
        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", normalUser.getUserID())
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldRegisterUserAndRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register.do")
                        .param("userID", "newUser01")
                        .param("userName", "新用户")
                        .param("password", "newPass123")
                        .param("email", "newUser@test.com")
                        .param("phone", "13900000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        User created = userDao.findByUserID("newUser01");
        assertThat(created).isNotNull();
        assertThat(created.getUserName()).isEqualTo("新用户");
    }

    @Test
    void shouldUpdateUserWithoutChangingPasswordWhenPasswordNewIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "更新后用户名")
                        .param("userID", normalUser.getUserID())
                        .param("passwordNew", "")
                        .param("email", "updated@test.com")
                        .param("phone", "13700000000")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updated = userDao.findByUserID(normalUser.getUserID());
        assertThat(updated.getUserName()).isEqualTo("更新后用户名");
        assertThat(updated.getPassword()).isEqualTo("Pass123");
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void shouldCheckPasswordByEquivalenceClass() throws Exception {
        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", normalUser.getUserID())
                        .param("password", normalUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", normalUser.getUserID())
                        .param("password", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldLogoutAndQuitByClearingDifferentSessionFlags() throws Exception {
        mockMvc.perform(get("/logout.do").session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        mockMvc.perform(get("/quit.do").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }
}
