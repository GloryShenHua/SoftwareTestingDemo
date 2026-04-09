package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AdminVenueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    @Test
    @DisplayName("GET /venue_manage: 应返回场馆管理页并携带总页数")
    void venueManageShouldReturnViewAndTotalPages() throws Exception {
        Page<Venue> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 25);
        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", 3));
    }

    @Test
    @DisplayName("GET /venue_edit: 有效venueID时应返回编辑页并携带venue")
    void editVenueShouldReturnViewAndVenue() throws Exception {
        Venue venue = new Venue(7, "羽毛球馆", "desc", 120, "p.png", "addr", "08:00", "22:00");
        when(venueService.findByVenueID(7)).thenReturn(venue);

        mockMvc.perform(get("/venue_edit").param("venueID", "7"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_edit"))
                .andExpect(model().attribute("venue", venue));

        verify(venueService).findByVenueID(7);
    }

    @Test
    @DisplayName("GET /venue_add: 应返回新增页")
    void venueAddShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    @DisplayName("GET /venueList.do?page=1: 等价类-有效页码，应返回场馆列表")
    void getVenueListShouldReturnListForValidPage() throws Exception {
        List<Venue> venues = Arrays.asList(
                new Venue(1, "A馆", "d1", 80, "", "a1", "09:00", "21:00"),
                new Venue(2, "B馆", "d2", 100, "", "a2", "10:00", "22:00")
        );
        when(venueService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(venues, PageRequest.of(0, 10), venues.size()));

        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].venueID").value(1))
                .andExpect(jsonPath("$[1].venueName").value("B馆"));
    }

    @Test
    @DisplayName("GET /venueList.do: 边界值-缺省页码，默认按第1页处理")
    void getVenueListShouldUseDefaultPageWhenMissing() throws Exception {
        when(venueService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/venueList.do"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /venueList.do?page=0: 边界值-非法页码，应抛IllegalArgumentException")
    void getVenueListShouldReturnBadRequestWhenPageIsZero() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(get("/venueList.do").param("page", "0"))
        );
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("POST /addVenue.do: 等价类-有效参数且无图片，应创建并重定向")
    void addVenueShouldCreateAndRedirectWhenNoPicture() throws Exception {
        when(venueService.create(any(Venue.class))).thenReturn(1);
        MockMultipartFile picture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", "新馆")
                        .param("address", "测试地址")
                        .param("description", "测试描述")
                        .param("price", "120")
                        .param("open_time", "08:00")
                        .param("close_time", "22:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "venue_manage"));

        verify(venueService).create(any(Venue.class));
    }

    @Test
    @DisplayName("POST /addVenue.do: 边界值-创建失败应重定向回新增页")
    void addVenueShouldRedirectToAddWhenCreateFailed() throws Exception {
        when(venueService.create(any(Venue.class))).thenReturn(0);
        MockMultipartFile picture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", "新馆")
                        .param("address", "测试地址")
                        .param("description", "测试描述")
                        .param("price", "120")
                        .param("open_time", "08:00")
                        .param("close_time", "22:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "venue_add"));
    }

    @Test
    @DisplayName("POST /modifyVenue.do: 等价类-有效参数且无新图片，应更新并重定向")
    void modifyVenueShouldUpdateAndRedirectWhenNoPicture() throws Exception {
        Venue old = new Venue(5, "旧馆", "旧描述", 100, "old.png", "旧地址", "09:00", "21:00");
        when(venueService.findByVenueID(5)).thenReturn(old);
        doNothing().when(venueService).update(any(Venue.class));
        MockMultipartFile picture = new MockMultipartFile("picture", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(picture)
                        .param("venueID", "5")
                        .param("venueName", "新馆名")
                        .param("address", "新地址")
                        .param("description", "新描述")
                        .param("price", "150")
                        .param("open_time", "08:00")
                        .param("close_time", "23:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "venue_manage"));

        verify(venueService).findByVenueID(5);
        verify(venueService).update(any(Venue.class));
    }

    @Test
    @DisplayName("POST /delVenue.do: 等价类-有效venueID时返回true并调用删除")
    void delVenueShouldReturnTrueAndInvokeService() throws Exception {
        doNothing().when(venueService).delById(9);

        mockMvc.perform(post("/delVenue.do").param("venueID", "9"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(venueService).delById(9);
    }

    @Test
    @DisplayName("POST /delVenue.do: 边界值-缺失venueID，应抛IllegalStateException")
    void delVenueShouldReturnBadRequestWhenVenueIdMissing() throws Exception {
        NestedServletException exception = assertThrows(
                NestedServletException.class,
                () -> mockMvc.perform(post("/delVenue.do"))
        );
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("POST /checkVenueName.do: 等价类-名称已存在返回false")
    void checkVenueNameShouldReturnFalseWhenExists() throws Exception {
        when(venueService.countVenueName("A馆")).thenReturn(2);

        mockMvc.perform(post("/checkVenueName.do").param("venueName", "A馆"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /checkVenueName.do: 等价类-名称不存在返回true")
    void checkVenueNameShouldReturnTrueWhenNotExists() throws Exception {
        when(venueService.countVenueName("Z馆")).thenReturn(0);

        mockMvc.perform(post("/checkVenueName.do").param("venueName", "Z馆"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
