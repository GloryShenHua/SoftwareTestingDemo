package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageVoServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageVoServiceImpl messageVoService;

    @Test
    void testReturnMessageVoByMessageID_WhenMessageAndUserExist_ShouldAssembleVo() {
        LocalDateTime time = LocalDateTime.of(2026, 3, 29, 12, 0);
        Message message = new Message(1, "u001", "hello", time, 2);
        User user = new User(100, "u001", "Alice", "pwd", "a@demo.com", "123456", 0, "p1.jpg");
        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("u001")).thenReturn(user);

        MessageVo vo = messageVoService.returnMessageVoByMessageID(1);

        assertEquals(1, vo.getMessageID());
        assertEquals("u001", vo.getUserID());
        assertEquals("hello", vo.getContent());
        assertEquals(time, vo.getTime());
        assertEquals("Alice", vo.getUserName());
        assertEquals("p1.jpg", vo.getPicture());
        assertEquals(2, vo.getState());
        verify(messageDao).findByMessageID(1);
        verify(userDao).findByUserID("u001");
    }

    @Test
    void testReturnMessageVoByMessageID_WhenMessageNotExist_ShouldThrowNullPointerException() {
        when(messageDao.findByMessageID(404)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> messageVoService.returnMessageVoByMessageID(404));
        verify(messageDao).findByMessageID(404);
        verify(userDao, never()).findByUserID(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testReturnMessageVoByMessageID_WhenUserNotExist_ShouldThrowNullPointerException() {
        Message message = new Message(1, "u001", "hello",
                LocalDateTime.of(2026, 3, 29, 12, 0), 1);
        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("u001")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> messageVoService.returnMessageVoByMessageID(1));
        verify(messageDao).findByMessageID(1);
        verify(userDao).findByUserID("u001");
    }

    @Test
    void testReturnVo_WhenInputIsEmpty_ShouldReturnEmptyList() {
        List<MessageVo> result = messageVoService.returnVo(Collections.emptyList());

        assertTrue(result.isEmpty());
        verifyNoInteractions(messageDao, userDao);
    }

    @Test
    void testReturnVo_WhenInputContainsOneMessage_ShouldReturnSingleVo() {
        Message input = new Message(11, "ignored", "ignored",
                LocalDateTime.of(2026, 3, 29, 10, 0), 1);
        Message found = new Message(11, "u011", "content11",
                LocalDateTime.of(2026, 3, 29, 10, 0), 2);
        User user = new User(11, "u011", "Bob", "pwd", "b@demo.com", "111", 0, "u11.jpg");
        when(messageDao.findByMessageID(11)).thenReturn(found);
        when(userDao.findByUserID("u011")).thenReturn(user);

        List<MessageVo> result = messageVoService.returnVo(Collections.singletonList(input));

        assertEquals(1, result.size());
        assertEquals(11, result.get(0).getMessageID());
        assertEquals("Bob", result.get(0).getUserName());
        verify(messageDao).findByMessageID(11);
        verify(userDao).findByUserID("u011");
    }

    @Test
    void testReturnVo_WhenInputContainsMultipleMessages_ShouldReturnVoListInOrder() {
        Message input1 = new Message(1, "ignored", "ignored",
                LocalDateTime.of(2026, 3, 29, 9, 0), 1);
        Message input2 = new Message(2, "ignored", "ignored",
                LocalDateTime.of(2026, 3, 29, 10, 0), 1);

        Message found1 = new Message(1, "u001", "c1",
                LocalDateTime.of(2026, 3, 29, 9, 0), 2);
        Message found2 = new Message(2, "u002", "c2",
                LocalDateTime.of(2026, 3, 29, 10, 0), 3);

        User user1 = new User(1, "u001", "Alice", "pwd", "a@demo.com", "111", 0, "a.jpg");
        User user2 = new User(2, "u002", "Carol", "pwd", "c@demo.com", "222", 0, "c.jpg");

        when(messageDao.findByMessageID(1)).thenReturn(found1);
        when(messageDao.findByMessageID(2)).thenReturn(found2);
        when(userDao.findByUserID("u001")).thenReturn(user1);
        when(userDao.findByUserID("u002")).thenReturn(user2);

        List<MessageVo> result = messageVoService.returnVo(Arrays.asList(input1, input2));

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getMessageID());
        assertEquals("Alice", result.get(0).getUserName());
        assertEquals(2, result.get(1).getMessageID());
        assertEquals("Carol", result.get(1).getUserName());
        verify(messageDao).findByMessageID(1);
        verify(messageDao).findByMessageID(2);
        verify(userDao).findByUserID("u001");
        verify(userDao).findByUserID("u002");
    }
}