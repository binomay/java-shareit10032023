package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositary.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void contextSearch_whenOneItemFound() {
        Pageable pageable = PageRequest.of(0, 2);
        User user = new User();
        user.setName("тестовый пользак");
        user.setEmail("test@mail.ru");
        //userRepository.save(user);
        em.persist(user);
        em.flush();
        Item item1 = new Item();
        item1.setAvailable(true);
        item1.setOwner(user);
        item1.setName("какая-то вещь");
        item1.setDescription("какая-то претокая-от вещь");
        em.persist(item1);
        em.flush();
        Item item2 = new Item();
        item2.setAvailable(true);
        item2.setOwner(user);
        item2.setName("просто вещь");
        item2.setDescription("простецкая вещь");
        em.persist(item2);
        em.flush();

        List<Item> itemList = itemRepository.contextSearch("КАК", pageable);

        assertEquals(itemList.size(), 1);
        assertEquals(itemList.get(0).getName(), "какая-то вещь");
    }

    @Test
    void contextSearch_whenNoOneItemFound() {
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
        Item item2 = new Item();
        item2.setAvailable(true);
        item2.setOwner(user);
        item2.setName("просто вещь");
        item2.setDescription("простецкая вещь");
        em.persist(item2);
        em.flush();

        List<Item> itemList = itemRepository.contextSearch("НЕНАШЕЛ", pageable);

        assertEquals(itemList.size(), 0);
    }

    @Test
    void getItemsWasCompleteBookingByUser() {
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
        Item item2 = new Item();
        item2.setAvailable(true);
        item2.setOwner(user);
        item2.setName("просто вещь");
        item2.setDescription("простецкая вещь");
        em.persist(item2);
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

        Booking booking1 = new Booking();
        booking1.setItem(item1);
        booking1.setStatus(BookingStatus.APPROVED.getName());
        LocalDateTime startDate1  = LocalDateTime.now().minusDays(3);
        LocalDateTime endDate1 = LocalDateTime.now().plusDays(10);
        booking1.setStart(startDate1);
        booking1.setEnd(endDate1);
        booking1.setBooker(user);
        em.persist(booking1);
        em.flush();


        List<Item> itemList = itemRepository.getItemsWasCompleteBookingByUser(item1.getId(), LocalDateTime.now());

        assertEquals(itemList.size(), 1);
        assertEquals(itemList.get(0).getName(), item1.getName());
    }
}