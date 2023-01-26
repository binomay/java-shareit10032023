package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;

@Data
public class MagicBookings {
    private Booking lastBooking;
    private Booking nextBooking;
}
