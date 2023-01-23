package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public OutputBookingDto createBooking(@RequestBody InputBookingDto inputBookingDto, @RequestHeader(value = "X-Sharer-User-Id") Integer bookerId) {
        inputBookingDto.setBookerId(bookerId);
        return bookingService.createBooking(inputBookingDto);
    }

    //Эндпоинт — PATCH /bookings/{bookingId}?approved={approved}, параметр approved может принимать значения true или false.
    @PatchMapping("/{bookingId}")
    public OutputBookingDto approveBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId, @PathVariable("bookingId") Integer bookingId, @RequestParam(name = "approved") Boolean isApprove) {
        return bookingService.updateBooking(userId, bookingId, isApprove);
    }

    @GetMapping("/{bookingId}")
    public OutputBookingDto getBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId, @PathVariable("bookingId") Integer bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<OutputBookingDto> getBookingsForUser(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                  @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        return bookingService.getUsersBooking(userId, state);
    }

    @GetMapping("/owner")
    public List<OutputBookingDto> getBookingsForOwner(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                  @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        return bookingService.getBookingsForOwner(userId, state);
    }
}
