package ru.practicum.shareit.booking.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class InputBookingDto {
    private Integer id;
    @NotNull(message = "Не указана дата начала бронирования")
    LocalDateTime start;
    @NotNull(message = "Не указана дата окончания бронирования")
    LocalDateTime end;
    @NotNull(message = "Не указано, что бронируется")
    Integer itemId;
    Integer bookerId;
    String status;
}
