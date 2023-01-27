package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class OutputBookingDto {
        private Integer id;
        private LocalDateTime start;
        private LocalDateTime end;
        private ItemShortDto item;
        private UserShortDto booker;
        private String status;
}
