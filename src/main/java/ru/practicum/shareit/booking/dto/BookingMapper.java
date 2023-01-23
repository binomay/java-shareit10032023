package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

public class BookingMapper {
    public static OutputBookingDto toOutput(Booking booking) {
        OutputBookingDto outputBookingDto = new OutputBookingDto();
        outputBookingDto.setId(booking.getId());
        outputBookingDto.setStart(booking.getStart());
        outputBookingDto.setEnd(booking.getEnd());
        outputBookingDto.setItem(ItemMapper.itemToItemShortDto(booking.getItem()));
        outputBookingDto.setBooker(UserMapper.UserToUserShort(booking.getBooker()));
        outputBookingDto.setStatus(booking.getStatus());
        return outputBookingDto;
    }

    public static BookingDtoForUserItemsOutput toBookDtoForItem(Booking booking) {
        BookingDtoForUserItemsOutput out = new BookingDtoForUserItemsOutput();
        out.setId(booking.getId());
        out.setStart(booking.getStart());
        out.setEnd(booking.getEnd());
        out.setBookerId(booking.getBooker().getId());
        out.setStatus(booking.getStatus());
        return out;
    }

}
