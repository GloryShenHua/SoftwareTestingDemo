package com.demo.service.impl;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testFindByUserID_WhenUserExists() {
        User user = new User(1, "test_user", "nickname", "12", "mail@qq.com", "123", 0, "pic");
        when(userDao.findByUserID("test_user")).thenReturn(user);

        User result = userService.findByUserID("test_user");
        assertSame(user, result);
        verify(userDao).findByUserID("test_user");
    }

    @Test
    void testFindById_WhenUserExists() {
        User user = new User(1, "test_user", "nickname", "12", "mail@qq.com", "123", 0, "pic");
        when(userDao.findById(1)).thenReturn(user);

        User result = userService.findById(1);
        assertSame(user, result);
        verify(userDao).findById(1);
    }

    @Test
    void testFindByUserIDPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(page);

        Page<User> result = userService.findByUserID(pageable);
        assertSame(page, result);
        verify(userDao).findAllByIsadmin(0, pageable);
    }

    @Test
    void testCheckLogin_Success() {
        User user = new User(1, "user", "nickname", "pass", "mail@qq.com", "123", 0, "pic");
        when(userDao.findByUserIDAndPassword("user", "pass")).thenReturn(user);

        User result = userService.checkLogin("user", "pass");
        assertSame(user, result);
        verify(userDao).findByUserIDAndPassword("user", "pass");
    }

    @Test
    void testCheckLogin_Fail() {
        when(userDao.findByUserIDAndPassword("user", "wrong")).thenReturn(null);

        User result = userService.checkLogin("user", "wrong");
        assertNull(result);
        verify(userDao).findByUserIDAndPassword("user", "wrong");
    }

    @Test
    void testCreate() {
        User user = new User(1, "name", "name", "pass", "mail", "phone", 0, "pic");
        when(userDao.findAll()).thenReturn(Arrays.asList(user));

        int total = userService.create(user);
        assertEquals(1, total);
        verify(userDao).save(user);
        verify(userDao).findAll();
    }

    @Test
    void testDelByID() {
        userService.delByID(1);
        verify(userDao).deleteById(1);
    }

    @Test
    void testUpdateUser() {
        User user = new User(1, "name", "name", "pass", "mail", "phone", 0, "pic");
        userService.updateUser(user);
        verify(userDao).save(user);
    }

    @Test
    void testCountUserID() {
        when(userDao.countByUserID("user1")).thenReturn(5);
        int count = userService.countUserID("user1");
        assertEquals(5, count);
        verify(userDao).countByUserID("user1");
    }
}
