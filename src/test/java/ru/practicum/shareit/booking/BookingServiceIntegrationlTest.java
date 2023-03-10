package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationlTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;
    private final EntityManager em;

    @Test
    void createBooking() {
        UserDto userDto = createOneUserDto();
        userDto = userService.createUser(userDto);
        ItemDto itemDto = createOneItemDto(userDto.getId());
        itemDto = itemService.createItem(itemDto);
        UserDto bookerDto = createAnotherUserDto();
        bookerDto = userService.createUser(bookerDto);
        InputBookingDto inputBookingDto = createOneInputBookingDto(itemDto.getId(), bookerDto.getId());
        OutputBookingDto outputBookingDto = bookingService.createBooking(inputBookingDto);
        Integer bookingId = outputBookingDto.getId();

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking actualBooking = query.setParameter("id", bookingId).getSingleResult();

        assertEquals(actualBooking.getItem().getId(), inputBookingDto.getItemId());
        assertEquals(actualBooking.getStatus(), inputBookingDto.getStatus());
        assertEquals(actualBooking.getStart(), inputBookingDto.getStart());
        assertEquals(actualBooking.getEnd(), inputBookingDto.getEnd());
        assertEquals(actualBooking.getBooker().getId(), inputBookingDto.getBookerId());
    }

    @Test
    void updateBooking() {
        UserDto userDto = createOneUserDto();
        userDto = userService.createUser(userDto);
        Integer userId = userDto.getId();
        ItemDto itemDto = createOneItemDto(userDto.getId());
        itemDto = itemService.createItem(itemDto);
        UserDto bookerDto = createAnotherUserDto();
        bookerDto = userService.createUser(bookerDto);
        bookerDto = userService.createUser(bookerDto);
        InputBookingDto inputBookingDto = createOneInputBookingDto(itemDto.getId(), bookerDto.getId());
        OutputBookingDto outputBookingDto = bookingService.createBooking(inputBookingDto);
        Integer bookingId = outputBookingDto.getId();
        outputBookingDto  = bookingService.updateBooking(userId, bookingId, true);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking actualBooking = query.setParameter("id", bookingId).getSingleResult();

        assertEquals(actualBooking.getItem().getId(), inputBookingDto.getItemId());
        assertEquals(actualBooking.getStatus(), BookingStatus.APPROVED.getName());
        assertEquals(actualBooking.getStart(), inputBookingDto.getStart());
        assertEquals(actualBooking.getEnd(), inputBookingDto.getEnd());
        assertEquals(actualBooking.getBooker().getId(), inputBookingDto.getBookerId());
    }

    private InputBookingDto createOneInputBookingDto(Integer itemId, Integer bookerId) {
        InputBookingDto dto = new InputBookingDto();
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setStatus(BookingStatus.WAITING.getName());
        dto.setItemId(itemId);
        dto.setBookerId(bookerId);
        return dto;
    }

    private UserDto createOneUserDto() {
        UserDto user = new UserDto();
        user.setName("Тестовый юзер");
        user.setEmail("test@mail.ru");
        return user;
    }

    private UserDto createAnotherUserDto() {
        UserDto user = new UserDto();
        user.setName("Тестовый букер");
        user.setEmail("booker@mail.ru");
        return user;
    }

    private ItemDto createOneItemDto(Integer owneId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("какая-то вещь");
        itemDto.setOwner(owneId);
        itemDto.setAvailable(true);
        itemDto.setDescription("описание какой-то вещи");
        return itemDto;
    }
}