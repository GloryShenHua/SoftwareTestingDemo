package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import com.demo.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void testFindById_WhenDaoReturnsMessage_ShouldReturnMessage() {
        Message message = new Message(1, "u001", "hello",
                LocalDateTime.of(2026, 3, 29, 12, 0), MessageService.STATE_NO_AUDIT);
        when(messageDao.getOne(1)).thenReturn(message);

        Message result = messageService.findById(1);

        assertSame(message, result);
        verify(messageDao).getOne(1);
    }

    @Test
    void testFindByUser_WhenUserHasMessages_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> expectedPage = new PageImpl<>(Collections.singletonList(
                new Message(1, "u001", "hello",
                        LocalDateTime.of(2026, 3, 29, 12, 0), MessageService.STATE_NO_AUDIT)
        ), pageable, 1);
        when(messageDao.findAllByUserID("u001", pageable)).thenReturn(expectedPage);

        Page<Message> result = messageService.findByUser("u001", pageable);

        assertSame(expectedPage, result);
        verify(messageDao).findAllByUserID("u001", pageable);
    }

    @Test
    void testFindByUser_WhenUserHasNoMessages_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> emptyPage = Page.empty(pageable);
        when(messageDao.findAllByUserID("u001", pageable)).thenReturn(emptyPage);

        Page<Message> result = messageService.findByUser("u001", pageable);

        assertSame(emptyPage, result);
        verify(messageDao).findAllByUserID("u001", pageable);
    }

    @Test
    void testCreate_WhenSaveSucceeds_ShouldReturnGeneratedId() {
        Message toCreate = new Message(0, "u001", "hello",
                LocalDateTime.of(2026, 3, 29, 12, 0), MessageService.STATE_NO_AUDIT);
        Message saved = new Message(101, "u001", "hello",
                LocalDateTime.of(2026, 3, 29, 12, 0), MessageService.STATE_NO_AUDIT);
        when(messageDao.save(toCreate)).thenReturn(saved);

        int generatedId = messageService.create(toCreate);

        org.junit.jupiter.api.Assertions.assertEquals(101, generatedId);
        verify(messageDao).save(toCreate);
    }

    @Test
    void testDelById_ShouldInvokeDeleteById() {
        messageService.delById(1);

        verify(messageDao).deleteById(1);
    }

    @Test
    void testUpdate_ShouldInvokeSave() {
        Message message = new Message(1, "u001", "updated",
                LocalDateTime.of(2026, 3, 29, 13, 0), MessageService.STATE_NO_AUDIT);

        messageService.update(message);

        verify(messageDao).save(message);
    }

    @Test
    void testConfirmMessage_WhenMessageExists_ShouldUpdatePassState() {
        Message message = new Message(1, "u001", "hello",
                LocalDateTime.of(2026, 3, 29, 12, 0), MessageService.STATE_NO_AUDIT);
        when(messageDao.findByMessageID(1)).thenReturn(message);

        messageService.confirmMessage(1);

        verify(messageDao).updateState(MessageService.STATE_PASS, 1);
    }

    @Test
    void testConfirmMessage_WhenMessageNotExists_ShouldThrowException() {
        when(messageDao.findByMessageID(404)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> messageService.confirmMessage(404));
        verify(messageDao, never()).updateState(MessageService.STATE_PASS, 404);
    }

    @Test
    void testRejectMessage_WhenMessageExists_ShouldUpdateRejectState() {
        Message message = new Message(1, "u001", "hello",
                LocalDateTime.of(2026, 3, 29, 12, 0), MessageService.STATE_NO_AUDIT);
        when(messageDao.findByMessageID(1)).thenReturn(message);

        messageService.rejectMessage(1);

        verify(messageDao).updateState(MessageService.STATE_REJECT, 1);
    }

    @Test
    void testRejectMessage_WhenMessageNotExists_ShouldThrowException() {
        when(messageDao.findByMessageID(404)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> messageService.rejectMessage(404));
        verify(messageDao, never()).updateState(MessageService.STATE_REJECT, 404);
    }

    @Test
    void testFindWaitState_ShouldQueryStateNoAudit() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> expectedPage = Page.empty(pageable);
        when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, pageable)).thenReturn(expectedPage);

        Page<Message> result = messageService.findWaitState(pageable);

        assertSame(expectedPage, result);
        verify(messageDao).findAllByState(MessageService.STATE_NO_AUDIT, pageable);
    }

    @Test
    void testFindPassState_ShouldQueryStatePass() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> expectedPage = Page.empty(pageable);
        when(messageDao.findAllByState(MessageService.STATE_PASS, pageable)).thenReturn(expectedPage);

        Page<Message> result = messageService.findPassState(pageable);

        assertSame(expectedPage, result);
        verify(messageDao).findAllByState(MessageService.STATE_PASS, pageable);
    }
}
