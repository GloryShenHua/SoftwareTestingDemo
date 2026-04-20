package com.demo.controller.admin;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /user_manage: 应返回用户管理页并携带总页数")
    void userManageShouldReturnViewAndTotalPages() throws Exception {
        Page<User> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 22);
        when(userService.findByUserID(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attribute("total", 3));

        verify(userService).findByUserID(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /user_add: 应返回用户新增页")
    void userAddShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    @Test
    @DisplayName("GET /userList.do?page=1: 等价类-有效页码，应返回用户列表")
    void userListShouldReturnUsersForValidPage() throws Exception {
        List<User> users = Arrays.asList(
                new User(1, "u001", "Tom", "p1", "t@demo.com", "18811112222", 0, ""),
                new User(2, "u002", "Jerry", "p2", "j@demo.com", "18833334444", 0, "")
        );
        when(userService.findByUserID(any(Pageable.class))).thenReturn(new PageImpl<>(users, PageRequest.of(0, 10), users.size()));

        mockMvc.perform(get("/userList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userID").value("u001"))
                .andExpect(jsonPath("$[1].userName").value("Jerry"));
    }

    @Test
    @DisplayName("GET /userList.do: 边界值-缺省页码，默认按第1页处理")
    void userListShouldUseDefaultPageWhenMissing() throws Exception {
        when(userService.findByUserID(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/userList.do"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /userList.do?page=0: 边界值-非法页码，应抛IllegalArgumentException")
    void userListShouldReturnBadRequestWhenPageIsZero() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(get("/userList.do").param("page", "0"))
        );
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("GET /user_edit: 有效id时应返回编辑页并携带user")
    void userEditShouldReturnEditViewAndUser() throws Exception {
        User user = new User(8, "u008", "Alice", "pwd", "a@demo.com", "18866667777", 0, "avatar.png");
        when(userService.findById(8)).thenReturn(user);

        mockMvc.perform(get("/user_edit").param("id", "8"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attribute("user", user));

        verify(userService).findById(8);
    }

    @Test
    @DisplayName("POST /modifyUser.do: 等价类-有效参数，应更新并重定向")
    void modifyUserShouldUpdateAndRedirect() throws Exception {
        User oldUser = new User(9, "old01", "Old", "pwd", "old@demo.com", "13300001111", 0, "");
        when(userService.findByUserID("old01")).thenReturn(oldUser);
        doNothing().when(userService).updateUser(any(User.class));

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "new01")
                        .param("oldUserID", "old01")
                        .param("userName", "New Name")
                        .param("password", "new-pwd")
                        .param("email", "new@demo.com")
                        .param("phone", "13399998888"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "user_manage"));

        verify(userService).findByUserID("old01");
        verify(userService).updateUser(any(User.class));
    }

    @Test
    @DisplayName("POST /addUser.do: 等价类-有效参数，应创建并重定向")
    void addUserShouldCreateAndRedirect() throws Exception {
        when(userService.create(any(User.class))).thenReturn(1);

        mockMvc.perform(post("/addUser.do")
                        .param("userID", "u100")
                        .param("userName", "Bob")
                        .param("password", "pwd100")
                        .param("email", "bob@demo.com")
                        .param("phone", "18899990000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "user_manage"));

        verify(userService).create(any(User.class));
    }

    @Test
    @DisplayName("POST /checkUserID.do: 等价类-用户ID存在时应返回false")
    void checkUserIdShouldReturnFalseWhenDuplicated() throws Exception {
        when(userService.countUserID("u001")).thenReturn(1);

        mockMvc.perform(post("/checkUserID.do").param("userID", "u001"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /checkUserID.do: 等价类-用户ID不存在时应返回true")
    void checkUserIdShouldReturnTrueWhenNotExists() throws Exception {
        when(userService.countUserID("u999")).thenReturn(0);

        mockMvc.perform(post("/checkUserID.do").param("userID", "u999"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /delUser.do: 等价类-有效id应返回true并删除")
    void delUserShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(userService).delByID(10);

        mockMvc.perform(post("/delUser.do").param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).delByID(10);
    }

    @Test
    @DisplayName("POST /delUser.do: 边界值-缺失id，应抛IllegalStateException")
    void delUserShouldReturnBadRequestWhenIdMissing() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(post("/delUser.do"))
        );
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("POST /addUser.do: 语句覆盖-验证user.setPicture(\"\")语句已执行（picture字段初始化为空字符串）")
    void addUserShouldSetEmptyPictureField() throws Exception {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userService.create(captor.capture())).thenReturn(1);

        mockMvc.perform(post("/addUser.do")
                        .param("userID", "wb001")
                        .param("userName", "WhiteBox")
                        .param("password", "pwd001")
                        .param("email", "wb@demo.com")
                        .param("phone", "13300001111"))
                .andExpect(status().is3xxRedirection());

        assertEquals("", captor.getValue().getPicture(),
                "addUser 中 user.setPicture(\"\") 语句应将图片字段初始化为空字符串");
    }

    @Test
    @DisplayName("POST /modifyUser.do: 语句覆盖-验证五条setXXX语句均对User对象正确赋值")
    void modifyUserShouldSetAllFieldsFromRequest() throws Exception {
        User existing = new User(9, "old01", "OldName", "oldpwd", "old@demo.com", "13300001111", 0, "");
        when(userService.findByUserID("old01")).thenReturn(existing);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        doNothing().when(userService).updateUser(captor.capture());

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "new01")
                        .param("oldUserID", "old01")
                        .param("userName", "NewName")
                        .param("password", "newpwd")
                        .param("email", "new@demo.com")
                        .param("phone", "13399991111"))
                .andExpect(status().is3xxRedirection());

        User updated = captor.getValue();
        assertEquals("new01", updated.getUserID());
        assertEquals("NewName", updated.getUserName());
        assertEquals("newpwd", updated.getPassword());
        assertEquals("new@demo.com", updated.getEmail());
        assertEquals("13399991111", updated.getPhone());
    }

}
