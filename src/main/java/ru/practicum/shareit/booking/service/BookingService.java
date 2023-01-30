package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;

import java.util.List;

public interface BookingService {
    OutputBookingDto createBooking(InputBookingDto inputBookingDto);

    OutputBookingDto updateBooking(Integer userId, Integer bookingId, Boolean isApprove);

    OutputBookingDto getBookingById(Integer bookingId, Integer userId);

    List<OutputBookingDto> getUsersBooking(Integer userId, String state, Integer from, Integer size);

    List<OutputBookingDto> getBookingsForOwner(Integer userId, String state, Integer from, Integer size);
}
