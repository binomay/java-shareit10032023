package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDtoForUserItemsOutput {
    private Integer id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer bookerId;
    private String status;
}
