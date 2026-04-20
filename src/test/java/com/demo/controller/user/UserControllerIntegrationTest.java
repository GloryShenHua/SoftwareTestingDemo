package com.demo.controller.user;

import com.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class UserControllerIntegrationTest extends BaseUserControllerIntegrationTest {

    @Test
    void shouldOpenSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void shouldOpenLoginPage() throws Exception {
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

        assertThat(result.getRequest().getSession().getAttribute("user")).isInstanceOf(User.class);
        assertThat(result.getRequest().getSession().getAttribute("admin")).isNull();
        User sessionUser = (User) result.getRequest().getSession().getAttribute("user");
        assertThat(sessionUser.getUserID()).isEqualTo(normalUser.getUserID());
    }

    @Test
    void shouldLoginAsAdminAndSetSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", adminUser.getUserID())
                        .param("password", adminUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"))
                .andReturn();

        assertThat(result.getRequest().getSession().getAttribute("admin")).isInstanceOf(User.class);
        assertThat(result.getRequest().getSession().getAttribute("user")).isNull();
        User sessionAdmin = (User) result.getRequest().getSession().getAttribute("admin");
        assertThat(sessionAdmin.getUserID()).isEqualTo(adminUser.getUserID());
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {
        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", normalUser.getUserID())
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andReturn();

        assertThat(result.getRequest().getSession().getAttribute("user")).isNull();
        assertThat(result.getRequest().getSession().getAttribute("admin")).isNull();
    }

    @Test
    void shouldRejectLoginWhenUserIdNotExistsOrEmpty() throws Exception {
        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "not-exists")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "")
                        .param("password", normalUser.getPassword()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldRejectLoginWhenPasswordEmptyOrBothEmpty() throws Exception {
        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", normalUser.getUserID())
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "")
                        .param("password", ""))
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
        assertThat(created.getPicture()).isEqualTo("");
        assertThat(created.getIsadmin()).isEqualTo(0);
    }

    @Test
    void shouldAllowDuplicateUserIdRegistrationAsCurrentBehavior() throws Exception {
        int before = userDao.findAll().size();

        mockMvc.perform(post("/register.do")
                        .param("userID", normalUser.getUserID())
                        .param("userName", "重复用户")
                        .param("password", "dup-pass")
                        .param("email", "dup@test.com")
                        .param("phone", "13600000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        assertThat(userDao.findAll().size()).isEqualTo(before + 1);
    }

    @Test
    void shouldAllowMissingOrEmptyRegisterFieldsAsCurrentBehavior() throws Exception {
        int before = userDao.findAll().size();

        mockMvc.perform(post("/register.do")
                        .param("userID", "no-password")
                        .param("userName", "缺少密码")
                        .param("email", "e2@test.com")
                        .param("phone", "13000000002"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        mockMvc.perform(post("/register.do")
                        .param("userID", "no-email")
                        .param("userName", "缺少邮箱")
                        .param("password", "p3")
                        .param("phone", "13000000003"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        mockMvc.perform(post("/register.do")
                        .param("userID", "")
                        .param("userName", "")
                        .param("password", "")
                        .param("email", "")
                        .param("phone", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        assertThat(userDao.findAll().size()).isEqualTo(before + 3);
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
    void shouldUpdatePasswordWhenPasswordNewIsNotEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "改密用户")
                        .param("userID", normalUser.getUserID())
                        .param("passwordNew", "new-pass-999")
                        .param("email", "changepwd@test.com")
                        .param("phone", "13700000001")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updated = userDao.findByUserID(normalUser.getUserID());
        assertThat(updated.getPassword()).isEqualTo("new-pass-999");
    }

    @Test
    void shouldUpdatePictureWhenUploadIsNotEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("picture", "avatar.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/updateUser.do")
                        .file(file)
                        .param("userName", "改头像用户")
                        .param("userID", normalUser.getUserID())
                        .param("passwordNew", "")
                        .param("email", "avatar@test.com")
                        .param("phone", "13700000002")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updated = userDao.findByUserID(normalUser.getUserID());
        assertThat(updated.getPicture()).startsWith("file/user/");
    }

    @Test
    void shouldExposeUnauthorizedUpdateRiskWithoutSessionOrWithForgedUserId() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "无会话也能改")
                        .param("userID", normalUser.getUserID())
                        .param("passwordNew", "")
                        .param("email", "nosession@test.com")
                        .param("phone", "13700000003"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updatedByNoSession = userDao.findByUserID(normalUser.getUserID());
        assertThat(updatedByNoSession.getUserName()).isEqualTo("无会话也能改");

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "伪造ID更新")
                        .param("userID", adminUser.getUserID())
                        .param("passwordNew", "")
                        .param("email", "forged@test.com")
                        .param("phone", "13700000004")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User forgedUpdated = userDao.findByUserID(adminUser.getUserID());
        assertThat(forgedUpdated.getUserName()).isEqualTo("伪造ID更新");
    }

    @Test
    void shouldFailForInvalidUpdateUserInputs() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        assertThatThrownBy(() -> mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "不存在用户")
                        .param("userID", "no-such-user")
                        .param("passwordNew", "")
                        .param("email", "x@test.com")
                        .param("phone", "13700000005")
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "缺失userID")
                        .param("passwordNew", "")
                        .param("email", "x@test.com")
                        .param("phone", "13700000006")
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(multipart("/updateUser.do")
                        .param("userName", "缺失图片字段")
                        .param("userID", normalUser.getUserID())
                        .param("passwordNew", "")
                        .param("email", "x@test.com")
                        .param("phone", "13700000007")
                        .session(userSession())))
                .hasRootCauseInstanceOf(NullPointerException.class);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(emptyFile)
                        .param("userName", "空邮箱手机")
                        .param("userID", normalUser.getUserID())
                        .param("passwordNew", "")
                        .param("email", "")
                        .param("phone", "")
                        .session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        User updated = userDao.findByUserID(normalUser.getUserID());
        assertThat(updated.getEmail()).isEqualTo("");
        assertThat(updated.getPhone()).isEqualTo("");
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
    void shouldCheckPasswordForInvalidInputs() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "no-such-user")
                        .param("password", "any")))
                .hasCauseInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "")
                        .param("password", "any")))
                .hasCauseInstanceOf(NullPointerException.class);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", normalUser.getUserID()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldOpenUserInfoPageWhenSessionUserExists() throws Exception {
        mockMvc.perform(get("/user_info").session(userSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }

    @Test
    void shouldFailUserInfoPageWhenSessionUserMissing() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/user_info")))
                .hasCauseInstanceOf(org.thymeleaf.exceptions.TemplateInputException.class);
    }

    @Test
    void shouldLogoutAndQuitByClearingDifferentSessionFlags() throws Exception {
        MvcResult logout = mockMvc.perform(get("/logout.do").session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn();

        assertThat(logout.getRequest().getSession().getAttribute("user")).isNull();

        MvcResult quit = mockMvc.perform(get("/quit.do").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn();

        assertThat(quit.getRequest().getSession().getAttribute("admin")).isNull();
    }

    @Test
    void shouldNotClearOppositeRoleSessionWhenCallingQuitOrLogout() throws Exception {
        MvcResult userCallQuit = mockMvc.perform(get("/quit.do").session(userSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn();
        assertThat(userCallQuit.getRequest().getSession().getAttribute("user")).isNotNull();

        MvcResult adminCallLogout = mockMvc.perform(get("/logout.do").session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andReturn();
        assertThat(adminCallLogout.getRequest().getSession().getAttribute("admin")).isNotNull();
    }
}
