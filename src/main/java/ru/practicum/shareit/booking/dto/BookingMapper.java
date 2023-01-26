package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static OutputBookingDto toOutput(Booking booking) {
        OutputBookingDto outputBookingDto = new OutputBookingDto();
        outputBookingDto.setId(booking.getId());
        outputBookingDto.setStart(booking.getStart());
        outputBookingDto.setEnd(booking.getEnd());
        outputBookingDto.setItem(ItemMapper.itemToItemShortDto(booking.getItem()));
        outputBookingDto.setBooker(UserMapper.userToUserShort(booking.getBooker()));
        outputBookingDto.setStatus(booking.getStatus());
        return outputBookingDto;
    }

    public static BookingDtoForUserItemsOutput toBookDtoForItem(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDtoForUserItemsOutput out = new BookingDtoForUserItemsOutput();
        out.setId(booking.getId());
        out.setStart(booking.getStart());
        out.setEnd(booking.getEnd());
        out.setBookerId(booking.getBooker().getId());
        out.setStatus(booking.getStatus());
        return out;
    }

    public static Booking dtoToBooking(InputBookingDto inputBookingDto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(inputBookingDto.getId());
        booking.setStart(inputBookingDto.getStart());
        booking.setEnd(inputBookingDto.getEnd());
        booking.setStatus(inputBookingDto.getStatus());
        booking.setBooker(booker);
        booking.setItem(item);
        return booking;
    }

}
