package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepositary;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositaryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepositary repositary;
    private User user1 = new User();
    private Item item1 = new Item();
    private Booking booking1 = new Booking();
    private LocalDateTime startDate1;
    private LocalDateTime endDate1;
    private Pageable pageable = PageRequest.of(0, 2);

    @BeforeEach
    void setUp() {
        user1.setName("тестовый пользак");
        user1.setEmail("test@mail.ru");
        item1.setAvailable(true);
        item1.setOwner(user1);
        item1.setName("какая-то вещь");
        item1.setDescription("какая-то претокая-от вещь");
        booking1.setItem(item1);
        booking1.setStatus(BookingStatus.APPROVED.getName());
        startDate1  = LocalDateTime.now().minusDays(10);
        endDate1 = LocalDateTime.now().minusDays(7);
        booking1.setStart(startDate1);
        booking1.setEnd(endDate1);
        booking1.setBooker(user1);
    }

    @Test
    void getBookingsForOwnerByStatus_whenOneFound() {
        em.persist(user1);
        em.flush();
        em.persist(item1);
        em.flush();
        em.persist(booking1);
        em.flush();
        Booking booking2 = new Booking();
        booking2.setItem(item1);
        booking2.setStatus(BookingStatus.WAITING.getName());
        LocalDateTime startDate  = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);
        booking2.setStart(startDate);
        booking2.setEnd(endDate);
        booking2.setBooker(user1);
        em.persist(booking2);
        em.flush();

        List<Booking> bookingList = repositary.getBookingsForOwnerByStatus(user1.getId(), BookingStatus.APPROVED.getName(), pageable);

        assertEquals(bookingList.size(), 1);
        assertEquals(bookingList.get(0).getItem().getName(), item1.getName());
    }

    @Test
    void getAllBookingsForOwner_whenAllFound() {
        em.persist(user1);
        em.flush();
        em.persist(item1);
        em.flush();
        em.persist(booking1);
        em.flush();
        Item item2 = new Item();
        item2.setOwner(user1);
        item2.setName("вторая вещь");
        item2.setDescription("какая-то вторая вещь");
        item2.setAvailable(true);
        em.persist(item2);
        Booking booking2 = new Booking();
        booking2.setItem(item2);
        booking2.setStatus(BookingStatus.WAITING.getName());
        LocalDateTime startDate  = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);
        booking2.setStart(startDate);
        booking2.setEnd(endDate);
        booking2.setBooker(user1);
        em.persist(booking2);
        em.flush();

        List<Booking> bookingList = repositary.getAllBookingsForOwner(user1.getId(), pageable);

        assertEquals(bookingList.size(), 2);
        assertEquals(bookingList.get(0).getItem().getName(), item2.getName());
        assertEquals(bookingList.get(1).getItem().getName(), item1.getName());
    }
}