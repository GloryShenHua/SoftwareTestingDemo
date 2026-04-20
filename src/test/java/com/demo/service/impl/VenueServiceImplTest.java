package com.demo.service.impl;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    void testFindByVenueID_WhenDaoReturnsVenue_ShouldReturnVenue() {
        Venue venue = new Venue(1, "Gym", "Indoor", 100, "gym.jpg", "addr", "08:00", "22:00");
        when(venueDao.getOne(1)).thenReturn(venue);

        Venue result = venueService.findByVenueID(1);

        assertSame(venue, result);
        verify(venueDao).getOne(1);
    }

    @Test
    void testFindByVenueName_WhenVenueExists_ShouldReturnVenue() {
        Venue venue = new Venue(1, "Gym", "Indoor", 100, "gym.jpg", "addr", "08:00", "22:00");
        when(venueDao.findByVenueName("Gym")).thenReturn(venue);

        Venue result = venueService.findByVenueName("Gym");

        assertSame(venue, result);
        verify(venueDao).findByVenueName("Gym");
    }

    @Test
    void testFindByVenueName_WhenVenueNotExists_ShouldReturnNull() {
        when(venueDao.findByVenueName("NotExists")).thenReturn(null);

        Venue result = venueService.findByVenueName("NotExists");

        assertNull(result);
        verify(venueDao).findByVenueName("NotExists");
    }

    @Test
    void testFindAllPageable_WhenVenuesExist_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Venue venue = new Venue(1, "Gym", "Indoor", 100, "gym.jpg", "addr", "08:00", "22:00");
        Page<Venue> expectedPage = new PageImpl<>(Collections.singletonList(venue), pageable, 1);
        when(venueDao.findAll(pageable)).thenReturn(expectedPage);

        Page<Venue> result = venueService.findAll(pageable);

        assertSame(expectedPage, result);
        verify(venueDao).findAll(pageable);
    }

    @Test
    void testFindAll_WhenVenuesExist_ShouldReturnList() {
        List<Venue> venues = Arrays.asList(
                new Venue(1, "Gym", "Indoor", 100, "gym.jpg", "addr", "08:00", "22:00"),
                new Venue(2, "Court", "Outdoor", 80, "court.jpg", "addr2", "09:00", "21:00")
        );
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();

        assertSame(venues, result);
        verify(venueDao).findAll();
    }

    @Test
    void testCreate_WhenSaveSucceeds_ShouldReturnGeneratedVenueId() {
        Venue toCreate = new Venue(0, "Gym", "Indoor", 100, "gym.jpg", "addr", "08:00", "22:00");
        Venue saved = new Venue(10, "Gym", "Indoor", 100, "gym.jpg", "addr", "08:00", "22:00");
        when(venueDao.save(toCreate)).thenReturn(saved);

        int generatedId = venueService.create(toCreate);

        assertEquals(10, generatedId);
        verify(venueDao).save(toCreate);
    }

    @Test
    void testUpdate_ShouldInvokeSave() {
        Venue venue = new Venue(1, "Gym", "Updated", 120, "gym2.jpg", "new addr", "07:00", "23:00");

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    void testDelById_ShouldInvokeDeleteById() {
        venueService.delById(1);

        verify(venueDao).deleteById(1);
    }

    @Test
    void testCountVenueName_WhenNoDuplicate_ShouldReturnZero() {
        when(venueDao.countByVenueName("Gym")).thenReturn(0);

        int count = venueService.countVenueName("Gym");

        assertEquals(0, count);
        verify(venueDao).countByVenueName("Gym");
    }

    @Test
    void testCountVenueName_WhenDuplicateExists_ShouldReturnPositiveCount() {
        when(venueDao.countByVenueName("Gym")).thenReturn(2);

        int count = venueService.countVenueName("Gym");

        assertEquals(2, count);
        verify(venueDao).countByVenueName("Gym");
    }
}