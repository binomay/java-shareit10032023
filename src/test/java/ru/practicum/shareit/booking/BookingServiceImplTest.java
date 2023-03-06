package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepositary;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    ItemService itemService;
    @Mock
    UserService userService;
    @Mock
    BookingRepositary bookingRepositary;
    @InjectMocks
    BookingServiceImpl bookingService;
    @Captor
    ArgumentCaptor<User> argumentUserCaptor;
    @Captor
    ArgumentCaptor<Pageable> argumentPageCaptor;
    @Captor
    ArgumentCaptor<LocalDateTime> argumentDateCapture;
    @Captor
    ArgumentCaptor<LocalDateTime> argumentDate1Captor;

    @Test
    void createBooking() {
        User booker = createFirstUser();
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        InputBookingDto inputBookingDto = createInputBookingDto(expectedBooking);
        when(itemService.getItemById(item.getId()))
                .thenReturn(item);
        when(userService.getUserById(expectedBooking.getBooker().getId()))
                .thenReturn(booker);
        when(bookingRepositary.save(any()))
                .thenReturn(expectedBooking);

        OutputBookingDto outputBookingDto = bookingService.createBooking(inputBookingDto);

        verify(bookingRepositary).save(expectedBooking);
        assertEquals(outputBookingDto.getId(), inputBookingDto.getItemId());
    }

    @Test
    void createBooking_whenItemNotAvailable_thenNotSaved() {
        User booker = createFirstUser();
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        item.setAvailable(false);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        InputBookingDto inputBookingDto = createInputBookingDto(expectedBooking);
        when(itemService.getItemById(item.getId()))
                .thenReturn(item);
        when(userService.getUserById(expectedBooking.getBooker().getId()))
                .thenReturn(booker);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(inputBookingDto));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Item недоступен");
    }

    @Test
    void createBooking_whenEndBeforeStart_thenNotSaved() {
        User booker = createFirstUser();
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        expectedBooking.setStart(LocalDateTime.now().plusDays(3));
        expectedBooking.setEnd(LocalDateTime.now().plusDays(1));
        InputBookingDto inputBookingDto = createInputBookingDto(expectedBooking);
        when(itemService.getItemById(item.getId()))
                .thenReturn(item);
        when(userService.getUserById(expectedBooking.getBooker().getId()))
                .thenReturn(booker);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(inputBookingDto));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Дата начала позже даты окончания");
    }

    @Test
    void createBooking_whenStartBeforeNow_thenNotSaved() {
        User booker = createFirstUser();
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        expectedBooking.setStart(LocalDateTime.now().minusDays(1));
        expectedBooking.setEnd(LocalDateTime.now().plusDays(1));
        InputBookingDto inputBookingDto = createInputBookingDto(expectedBooking);
        when(itemService.getItemById(item.getId()))
                .thenReturn(item);
        when(userService.getUserById(expectedBooking.getBooker().getId()))
                .thenReturn(booker);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(inputBookingDto));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Дата начала не может быть в прошлом");
    }

    @Test
    void createBooking_whenBookerEqvOwner_thenNotSaved() {
        User booker = createFirstUser();
        User owner = booker;
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        InputBookingDto inputBookingDto = createInputBookingDto(expectedBooking);
        when(itemService.getItemById(item.getId()))
                .thenReturn(item);
        when(userService.getUserById(expectedBooking.getBooker().getId()))
                .thenReturn(booker);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(inputBookingDto));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Владелец не может сам у себя забронировать. Это глупо");
    }

    @Test
    void createBooking_whenWasBookingWithTheSameDate_thenNotSaved() {
        Booking anotherBooking = new Booking();
        anotherBooking.setId(666);
        List<Booking> anotherBookingList = List.of(anotherBooking);
        User booker = createFirstUser();
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        InputBookingDto inputBookingDto = createInputBookingDto(expectedBooking);
        when(itemService.getItemById(item.getId()))
                .thenReturn(item);
        when(userService.getUserById(expectedBooking.getBooker().getId()))
                .thenReturn(booker);
        when(bookingRepositary.getBookingWithSameDates(anyInt(), any(), any()))
                .thenReturn(anotherBookingList);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(inputBookingDto));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Есть букирование, которое пересекается по времени! " + anotherBooking.getId());
    }

    @Test
    void updateBooking_whenUserNotEqOwner_thenNotSaved() {
        User user = createFirstUser();
        User booker = user;
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(expectedBooking));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.updateBooking(user.getId(), expectedBooking.getId(),true));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Подтвердить/отменить  бронирование может только владелец!");
    }

    @Test
    void updateBooking_whenBookingNotEqOwner_thenNotSaved() {
        User user = createFirstUser();
        User booker = user;
        User owner = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.updateBooking(user.getId(), expectedBooking.getId(),true));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Не нашел бронирование  с Id = " + expectedBooking.getId());
    }

    @Test
    void updateBooking_whenStatusIsNotWaiting_thenNotSaved() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.APPROVED.getName(),
                booker, item);
        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(expectedBooking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.updateBooking(user.getId(), expectedBooking.getId(),true));

        verify(bookingRepositary, never()).save(expectedBooking);
        assertEquals(exception.getMessage(), "Статус можно поменять только для бронирований, ожидаюйщих подтверждения!");
    }

    @Test
    void updateBooking_whenIsApprove_thenSaved() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        Booking bookingTuUpdate = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        Booking expectedBooking = createFirstBooking(BookingStatus.APPROVED.getName(),
                booker, item);
        OutputBookingDto expectedDto = BookingMapper.toOutput(expectedBooking);

        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(bookingTuUpdate));
        when(bookingRepositary.save(any()))
                .thenReturn(expectedBooking);

        OutputBookingDto actualDto = bookingService.updateBooking(user.getId(), bookingTuUpdate.getId(), true);

        verify(bookingRepositary).save(expectedBooking);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void updateBooking_whenReject_thenSaved() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        Booking bookingTuUpdate = createFirstBooking(BookingStatus.WAITING.getName(),
                booker, item);
        Booking expectedBooking = createFirstBooking(BookingStatus.REJECTED.getName(),
                booker, item);
        OutputBookingDto expectedDto = BookingMapper.toOutput(expectedBooking);

        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(bookingTuUpdate));
        when(bookingRepositary.save(any()))
                .thenReturn(expectedBooking);

        OutputBookingDto actualDto = bookingService.updateBooking(user.getId(), bookingTuUpdate.getId(), false);

        verify(bookingRepositary).save(expectedBooking);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getBookingById_whenUserEqOwner_thenReturn() {

        User owner = createFirstUser();
        User user = owner;
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(expectedBooking));

        OutputBookingDto actualOutDto = bookingService.getBookingById(expectedBooking.getId(), user.getId());

        verify(bookingRepositary).findBookingById(expectedBooking.getId());
        assertEquals(actualOutDto.getId(), expectedBooking.getId());
    }

    @Test
    void getBookingById_whenUserEqBooker_thenReturn() {

        Integer bookingId = 1;
        User owner = createFirstUser();
        User booker = createSecondUser();
        User user = booker;
        Item item = createFirstItem(owner);
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(expectedBooking));

        OutputBookingDto actualOutDto = bookingService.getBookingById(expectedBooking.getId(), user.getId());

        verify(bookingRepositary).findBookingById(expectedBooking.getId());
        assertEquals(actualOutDto.getId(), expectedBooking.getId());
    }

    @Test
    void getBookingById_whenUserNotEqOwnerAndBooker_thenReturn() {

        Integer bookingId = 1;
        User owner = createFirstUser();
        User user = new User();
        user.setId(3);
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        when(bookingRepositary.findBookingById(any()))
                .thenReturn(Optional.of(expectedBooking));

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingById(expectedBooking.getId(), user.getId()));
    }

    @Test
    void getUsersBooking_whenAll() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "ALL";
        Integer from = 2;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from - 1, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);
        when(bookingRepositary.findBookingByBooker(any(), any()))
                        .thenReturn(expectedList);

        List<OutputBookingDto> actualList = bookingService.getUsersBooking(owner.getId(), state, from, size);

        assertEquals(actualList.size(), 1);
        assertEquals(actualList.get(0).getId(), expectedBooking.getId());
        verify(bookingRepositary).findBookingByBooker(user, pageable);
    }

    @Test
    void getUsersBooking_whenWaiting() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "WAITING";
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);
        when(bookingRepositary.findBookingByBookerAndStatus(any(), any(), any()))
                .thenReturn(expectedList);

        List<OutputBookingDto> actualList = bookingService.getUsersBooking(owner.getId(), state, from, size);

        assertEquals(actualList.size(), 1);
        assertEquals(actualList.get(0).getId(), expectedBooking.getId());
        verify(bookingRepositary).findBookingByBookerAndStatus(user, state, pageable);
    }

    @Test
    void getUsersBooking_whenRejected() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "REJECTED";
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);
        when(bookingRepositary.findBookingByBookerAndStatus(any(), any(), any()))
                .thenReturn(expectedList);

        List<OutputBookingDto> actualList = bookingService.getUsersBooking(owner.getId(), state, from, size);

        assertEquals(actualList.size(), 1);
        assertEquals(actualList.get(0).getId(), expectedBooking.getId());
        verify(bookingRepositary).findBookingByBookerAndStatus(user, state, pageable);
    }

    @Test
    void getUsersBooking_whenCurrent() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "CURRENT";
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);
        when(bookingRepositary.findBookingByBookerAndStartBeforeAndEndAfter(any(), any(), any(), any()))
                .thenReturn(expectedList);

        List<OutputBookingDto> actualList = bookingService.getUsersBooking(owner.getId(), state, from, size);

        verify(bookingRepositary).findBookingByBookerAndStartBeforeAndEndAfter(argumentUserCaptor.capture(),
                argumentDateCapture.capture(), argumentDate1Captor.capture(), argumentPageCaptor.capture());
        User actualUser = argumentUserCaptor.getValue();
        Pageable actualPage = argumentPageCaptor.getValue();

        assertEquals(actualList.size(), 1);
        assertEquals(actualList.get(0).getId(), expectedBooking.getId());
        assertEquals(user, actualUser);
        assertEquals(pageable, actualPage);
    }

    @Test
    void getUsersBooking_whenPast() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "PAST";
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);
        when(bookingRepositary.findBookingByBookerAndEndBefore(any(), any(), any()))
                .thenReturn(expectedList);

        List<OutputBookingDto> actualList = bookingService.getUsersBooking(owner.getId(), state, from, size);

        verify(bookingRepositary).findBookingByBookerAndEndBefore(argumentUserCaptor.capture(),
                argumentDateCapture.capture(), argumentPageCaptor.capture());
        User actualUser = argumentUserCaptor.getValue();
        Pageable actualPage = argumentPageCaptor.getValue();

        assertEquals(actualList.size(), 1);
        assertEquals(actualList.get(0).getId(), expectedBooking.getId());
        assertEquals(user, actualUser);
        assertEquals(pageable, actualPage);
    }

    @Test
    void getUsersBooking_whenFuture() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "FUTURE";
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);
        when(bookingRepositary.findBookingByBookerAndStartAfter(any(), any(), any()))
                .thenReturn(expectedList);

        List<OutputBookingDto> actualList = bookingService.getUsersBooking(owner.getId(), state, from, size);

        verify(bookingRepositary).findBookingByBookerAndStartAfter(argumentUserCaptor.capture(),
                argumentDateCapture.capture(), argumentPageCaptor.capture());
        User actualUser = argumentUserCaptor.getValue();
        Pageable actualPage = argumentPageCaptor.getValue();

        assertEquals(actualList.size(), 1);
        assertEquals(actualList.get(0).getId(), expectedBooking.getId());
        assertEquals(user, actualUser);
        assertEquals(pageable, actualPage);
    }

    @Test
    void getUsersBooking_whenUnsupported() {
        User user = createFirstUser();
        User owner = user;
        User booker = createSecondUser();
        Item item = createFirstItem(owner);
        String state = "NOONE";
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from, size, Sort.by("start").descending());
        Booking expectedBooking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> expectedList = List.of(expectedBooking);
        when(userService.getUserById(any())).thenReturn(user);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getUsersBooking(owner.getId(), state, from, size));
        assertEquals(exception.getMessage(), "Unknown state: UNSUPPORTED_STATUS");
    }

    @Test
    void getBookingsForOwner_whenAll_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "ALL";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);
        when(bookingRepositary.getAllBookingsForOwner(user.getId(), pageable))
                .thenReturn(bookingList);

        List<OutputBookingDto> expectedDto = bookingService.getBookingsForOwner(user.getId(), state, from, size);

        assertEquals(expectedDto.size(), 1);
        assertEquals(expectedDto.get(0).getId(), booking.getId());
        verify(bookingRepositary).getAllBookingsForOwner(user.getId(), pageable);
    }

    @Test
    void getBookingsForOwner_whenWaiting_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "WAITING";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.WAITING.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);
        when(bookingRepositary.getBookingsForOwnerByStatus(any(), any(), any()))
                .thenReturn(bookingList);

        List<OutputBookingDto> expectedDto = bookingService.getBookingsForOwner(user.getId(), state, from, size);

        assertEquals(expectedDto.size(), 1);
        assertEquals(expectedDto.get(0).getId(), booking.getId());
        verify(bookingRepositary).getBookingsForOwnerByStatus(user.getId(), state, pageable);
    }

    @Test
    void getBookingsForOwner_whenREJECTED_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "REJECTED";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.REJECTED.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);
        when(bookingRepositary.getBookingsForOwnerByStatus(any(), any(), any()))
                .thenReturn(bookingList);

        List<OutputBookingDto> expectedDto = bookingService.getBookingsForOwner(user.getId(), state, from, size);

        assertEquals(expectedDto.size(), 1);
        assertEquals(expectedDto.get(0).getId(), booking.getId());
        verify(bookingRepositary).getBookingsForOwnerByStatus(user.getId(), state, pageable);
    }

    @Test
    void getBookingsForOwner_whenPast_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "PAST";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.REJECTED.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);
        when(bookingRepositary.getPastBookingForOwner(any(), any(), any()))
                .thenReturn(bookingList);

        List<OutputBookingDto> expectedDto = bookingService.getBookingsForOwner(user.getId(), state, from, size);

        assertEquals(expectedDto.size(), 1);
        assertEquals(expectedDto.get(0).getId(), booking.getId());
    }

    @Test
    void getBookingsForOwner_whenFuture_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "FUTURE";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.REJECTED.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);
        when(bookingRepositary.getFutureBookingForOwner(any(), any(), any()))
                .thenReturn(bookingList);

        List<OutputBookingDto> expectedDto = bookingService.getBookingsForOwner(user.getId(), state, from, size);

        assertEquals(expectedDto.size(), 1);
        assertEquals(expectedDto.get(0).getId(), booking.getId());
    }

    @Test
    void getBookingsForOwner_whenCurrent_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "CURRENT";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.REJECTED.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);
        when(bookingRepositary.getCurrentBookingForOwner(any(), any(), any(), any()))
                .thenReturn(bookingList);

        List<OutputBookingDto> expectedDto = bookingService.getBookingsForOwner(user.getId(), state, from, size);

        assertEquals(expectedDto.size(), 1);
        assertEquals(expectedDto.get(0).getId(), booking.getId());
    }

    @Test
    void getBookingsForOwner_whenUnkState_thenReturn() {
        Integer from = 0;
        Integer size = 2;
        Pageable pageable = PageRequest.of(from,size);
        String state = "SOMESTATE";
        User owner = createFirstUser();
        User user = createSecondUser();
        Item item = createFirstItem(owner);
        User booker = createSecondUser();
        Booking booking = createFirstBooking(BookingStatus.REJECTED.getName(), booker, item);
        List<Booking> bookingList = List.of(booking);
        when(userService.getUserById(any()))
                .thenReturn(user);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getBookingsForOwner(user.getId(), state, from, size));

        assertEquals(exception.getMessage(), "Unknown state: UNSUPPORTED_STATUS");
    }

    private Booking createFirstBooking(String status, User booker, Item item) {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(status);
        booking.setBooker(booker);
        booking.setItem(item);
        return booking;
    }

    private User createFirstUser() {
        User user = new User();
        user.setId(1);
        user.setName("user1");
        user.setEmail("user1@mail.ru");
        return user;
    }

    private User createSecondUser() {
        User user = new User();
        user.setId(2);
        user.setName("user2");
        user.setEmail("user2@mail.ru");
        return user;
    }

    private Item createFirstItem(User owner) {
        Item item = new Item();
        item.setId(1);
        item.setAvailable(true);
        item.setName("Вещь 1");
        item.setDescription("Первая вещь");
        item.setAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private  InputBookingDto createInputBookingDto(Booking booking) {
        InputBookingDto dto = new InputBookingDto();
        dto.setId(booking.getId());
        dto.setItemId(booking.getItem().getId());
        dto.setStatus(booking.getStatus());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}