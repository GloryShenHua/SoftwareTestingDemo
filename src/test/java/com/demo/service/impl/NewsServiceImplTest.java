package com.demo.service.impl;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    void testFindAll_WhenNewsExist_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        News news = new News(1, "Title", "Content", LocalDateTime.of(2026, 3, 29, 12, 0));
        Page<News> expectedPage = new PageImpl<>(Collections.singletonList(news), pageable, 1);
        when(newsDao.findAll(pageable)).thenReturn(expectedPage);

        Page<News> result = newsService.findAll(pageable);

        assertSame(expectedPage, result);
        verify(newsDao).findAll(pageable);
    }

    @Test
    void testFindAll_WhenNoNewsExist_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> emptyPage = Page.empty(pageable);
        when(newsDao.findAll(pageable)).thenReturn(emptyPage);

        Page<News> result = newsService.findAll(pageable);

        assertSame(emptyPage, result);
        verify(newsDao).findAll(pageable);
    }

    @Test
    void testFindById_WhenDaoReturnsNews_ShouldReturnNews() {
        News news = new News(1, "Title", "Content", LocalDateTime.of(2026, 3, 29, 12, 0));
        when(newsDao.getOne(1)).thenReturn(news);

        News result = newsService.findById(1);

        assertSame(news, result);
        verify(newsDao).getOne(1);
    }

    @Test
    void testCreate_WhenSaveSucceeds_ShouldReturnGeneratedNewsId() {
        News toCreate = new News(0, "Title", "Content", LocalDateTime.of(2026, 3, 29, 12, 0));
        News saved = new News(88, "Title", "Content", LocalDateTime.of(2026, 3, 29, 12, 0));
        when(newsDao.save(toCreate)).thenReturn(saved);

        int generatedId = newsService.create(toCreate);

        assertEquals(88, generatedId);
        verify(newsDao).save(toCreate);
    }

    @Test
    void testDelById_ShouldInvokeDeleteById() {
        newsService.delById(1);

        verify(newsDao).deleteById(1);
    }

    @Test
    void testUpdate_ShouldInvokeSave() {
        News news = new News(1, "T2", "C2", LocalDateTime.of(2026, 3, 29, 13, 0));

        newsService.update(news);

        verify(newsDao).save(news);
    }
}