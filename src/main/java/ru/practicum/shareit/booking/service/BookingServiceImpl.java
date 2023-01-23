package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepositary;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepositary bookingRepositary;
    private final UserService userService;
    private final ItemService itemService;

    public BookingServiceImpl(BookingRepositary bookingRepositary,
                              UserService userService,
                              ItemService itemService) {
        this.bookingRepositary = bookingRepositary;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public OutputBookingDto createBooking(InputBookingDto inputBookingDto) {
        Booking booking = dtoToBooking(inputBookingDto);
        checkBooking(booking);
        booking.setStatus(BookingStatus.WAITING.getName());
        return BookingMapper.toOutput(bookingRepositary.save(booking));
    }

    @Override
    public OutputBookingDto updateBooking(Integer userId, Integer bookingId, Boolean isApprove) {
        Booking booking = getBooking(bookingId);
        checkApproving(booking, userId);
        if (isApprove) {
            booking.setStatus(BookingStatus.APPROVED.getName());
        } else {
            booking.setStatus(BookingStatus.REJECTED.getName());
        }
        return BookingMapper.toOutput(bookingRepositary.save(booking));
    }

    @Override
    public OutputBookingDto getBookingById(Integer bookingId, Integer userId) {
        Booking booking = getBooking(bookingId);
        if (!userId.equals(booking.getBooker().getId()) &&
                !userId.equals(booking.getItem().getOwner().getId())) {
            String msg = "Получить данные по бронированию может либо автор, либо владедец!";
            log.warn(msg);
            throw new ResourceNotFoundException(msg);
        }
        return BookingMapper.toOutput(booking);
    }

    @Override
    public List<OutputBookingDto> getUsersBooking(Integer userId, String state) {
        User user = userService.getUserById(userId);
        List<Booking> bookList;
        switch (state) {
            case "ALL":
                bookList = bookingRepositary.findBookingByBookerOrderByStartDesc(user);
                break;
            case "WAITING":
            case "REJECTED":
                bookList = bookingRepositary.findBookingByBookerAndStatusOrderByStartDesc(user, state);
                break;
            case "CURRENT":
                LocalDateTime dateTime = LocalDateTime.now();
                bookList = bookingRepositary.findBookingByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user, dateTime, dateTime);
                break;
            case "PAST":
                LocalDateTime dateTime1 = LocalDateTime.now();
                bookList = bookingRepositary.findBookingByBookerAndEndBeforeOrderByStartDesc(user, dateTime1);
                break;
            case "FUTURE":
                LocalDateTime dateTime2 = LocalDateTime.now();
                bookList = bookingRepositary.findBookingByBookerAndStartAfterOrderByStartDesc(user, dateTime2);
                break;
            default:
                String msg = "Unknown state: UNSUPPORTED_STATUS";
                log.info(msg);
                throw new ValidationException(msg);
        }
        return bookList.stream().map(BookingMapper::toOutput).collect(Collectors.toList());
    }

    @Override
    public List<OutputBookingDto> getBookingsForOwner(Integer userId, String state) {
        User user = userService.getUserById(userId);
        List<Booking> bookList;
        switch (state) {
            case "ALL":
                bookList = bookingRepositary.getAllBookingsForOwner(userId);
                break;
            case "WAITING":
            case "REJECTED":
                bookList = bookingRepositary.getBookingsForOwnerByStatus(userId, state);
                break;
            case "CURRENT":
                LocalDateTime dateTime = LocalDateTime.now();
                bookList = bookingRepositary.getCurrenBookingForOwner(userId, dateTime, dateTime);
                break;
            case "PAST":
                LocalDateTime dateTime1 = LocalDateTime.now();
                bookList = bookingRepositary.getPastBookingForOwner(userId, dateTime1);
                break;
            case "FUTURE":
                LocalDateTime dateTime2 = LocalDateTime.now();
                bookList = bookingRepositary.getFutureBookingForOwner(userId, dateTime2);
                break;
            default:
                String msg = "Unknown state: UNSUPPORTED_STATUS";
                log.info(msg);
                throw new ValidationException(msg);
        }
        return bookList.stream().map(BookingMapper::toOutput).collect(Collectors.toList());
    }

    private Booking dtoToBooking(InputBookingDto inputBookingDto) {
        Booking booking = new Booking();
        booking.setId(inputBookingDto.getId());
        booking.setStart(inputBookingDto.getStart());
        booking.setEnd(inputBookingDto.getEnd());
        booking.setStatus(inputBookingDto.getStatus());
        User booker = userService.getUserById(inputBookingDto.getBookerId());
        booking.setBooker(booker);
        Item item = itemService.getItemById(inputBookingDto.getItemId());
        booking.setItem(item);
        return booking;
    }

    private void checkBooking(Booking booking) {
        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("Item недоступен");
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Дата начала позже даты окончания");
        } else if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        } else if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата окончания не может быть в прошлом");
        }
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new ResourceNotFoundException("Владелец не может сам у себя забронировать. Это глупо");
        }
    }

    private Booking getBooking(Integer bookingId) {
        return bookingRepositary.findBookingById(bookingId).orElseThrow(
                () -> {
                    String msg = "Не нашел бронирование  с Id = " + bookingId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
    }

    private void checkApproving(Booking booking, Integer userId) {
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            String msg = "Подтвердить бронирование может только владелец!";
            log.warn(msg);
            throw new ResourceNotFoundException(msg);
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED.getName())) {
            String msg = "Нельзя менять статус потвержденного бронирования";
            log.warn(msg);
            throw new ValidationException(msg);
        }
    }

}
