package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Item item = itemService.getItemById(inputBookingDto.getItemId());
        User booker = userService.getUserById(inputBookingDto.getBookerId());
        Booking booking = BookingMapper.dtoToBooking(inputBookingDto, item, booker);
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
    public List<OutputBookingDto> getUsersBooking(Integer userId, String state, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        User user = userService.getUserById(userId);
        List<Booking> bookList;
        switch (state) {
            case "ALL":
                bookList = bookingRepositary.findBookingByBooker(user, pageable);
                break;
            case "WAITING":
            case "REJECTED":
                bookList = bookingRepositary.findBookingByBookerAndStatus(user, state, pageable);
                break;
            case "CURRENT":
                LocalDateTime dateTime = LocalDateTime.now();
                bookList = bookingRepositary.findBookingByBookerAndStartBeforeAndEndAfter(user, dateTime, dateTime, pageable);
                break;
            case "PAST":
                LocalDateTime dateTime1 = LocalDateTime.now();
                bookList = bookingRepositary.findBookingByBookerAndEndBefore(user, dateTime1, pageable);
                break;
            case "FUTURE":
                LocalDateTime dateTime2 = LocalDateTime.now();
                bookList = bookingRepositary.findBookingByBookerAndStartAfter(user, dateTime2, pageable);
                break;
            default:
                String msg = "Unknown state: UNSUPPORTED_STATUS";
                log.info(msg);
                throw new ValidationException(msg);
        }
        return bookList.stream().map(BookingMapper::toOutput).collect(Collectors.toList());
    }

    @Override
    public List<OutputBookingDto> getBookingsForOwner(Integer userId, String state, Integer from, Integer size) {
        User user = userService.getUserById(userId);
        List<Booking> bookList;
        Pageable pageable = PageRequest.of(from / size, size);
        switch (state) {
            case "ALL":
                bookList = bookingRepositary.getAllBookingsForOwner(userId, pageable);
                break;
            case "WAITING":
            case "REJECTED":
                bookList = bookingRepositary.getBookingsForOwnerByStatus(userId, state, pageable);
                break;
            case "CURRENT":
                LocalDateTime dateTime = LocalDateTime.now();
                bookList = bookingRepositary.getCurrentBookingForOwner(userId, dateTime, dateTime, pageable);
                break;
            case "PAST":
                LocalDateTime dateTime1 = LocalDateTime.now();
                bookList = bookingRepositary.getPastBookingForOwner(userId, dateTime1, pageable);
                break;
            case "FUTURE":
                LocalDateTime dateTime2 = LocalDateTime.now();
                bookList = bookingRepositary.getFutureBookingForOwner(userId, dateTime2, pageable);
                break;
            default:
                String msg = "Unknown state: UNSUPPORTED_STATUS";
                log.info(msg);
                throw new ValidationException(msg);
        }
        return bookList.stream().map(BookingMapper::toOutput).collect(Collectors.toList());
    }

    private void checkBooking(Booking booking) {
        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("Item недоступен");
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Дата начала позже даты окончания");
        } else if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new ResourceNotFoundException("Владелец не может сам у себя забронировать. Это глупо");
        }
        checkDates(booking);
    }

    private void checkDates(Booking booking) {
        List<Booking> bookingList = bookingRepositary.getBookingWithSameDates(booking.getItem().getId(), booking.getStart(), booking.getEnd());
        if (bookingList.size() != 0) {
            throw new ResourceNotFoundException("Есть букирование, которое пересекается по времени! " + bookingList.get(0).getId());
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
            String msg = "Подтвердить/отменить  бронирование может только владелец!";
            log.warn(msg);
            throw new ResourceNotFoundException(msg);
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING.getName())) {
            String msg = "Статус можно поменять только для бронирований, ожидаюйщих подтверждения!";
            log.warn(msg);
            throw new ValidationException(msg);
        }
    }

}
