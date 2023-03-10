package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositary.ItemRequestRepositary;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRequestRepositaryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRequestRepositary repositary;

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(0, 2);
        User user = new User();
        user.setName("тестовый пользак");
        user.setEmail("test@mail.ru");
        em.persist(user);
        em.flush();
        Item item1 = new Item();
        item1.setAvailable(true);
        item1.setOwner(user);
        item1.setName("какая-то вещь");
        item1.setDescription("какая-то претокая-от вещь");
        em.persist(item1);
        em.flush();
        Booking booking = new Booking();
        booking.setItem(item1);
        booking.setStatus(BookingStatus.APPROVED.getName());
        LocalDateTime startDate  = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(7);
        booking.setStart(startDate);
        booking.setEnd(endDate);
        booking.setBooker(user);
        em.persist(booking);
        em.flush();
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now().minusDays(1));
        itemRequest.setDescription("какой-то отзыв");
        em.persist(itemRequest);
        em.flush();

        List<ItemRequest> list = repositary.findAll(2, pageable);

        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getDescription(), itemRequest.getDescription());
    }
}