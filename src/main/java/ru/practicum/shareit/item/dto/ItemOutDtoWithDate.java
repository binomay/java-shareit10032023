package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoForUserItemsOutput;

import java.util.List;

@Data
public class ItemOutDtoWithDate {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoForUserItemsOutput lastBooking;
    private BookingDtoForUserItemsOutput nextBooking;
    private List<OutputCommentDto> comments;
}
