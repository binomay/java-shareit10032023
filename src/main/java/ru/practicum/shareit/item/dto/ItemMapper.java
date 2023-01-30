package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.MagicBookings;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setOwner(item.getOwner().getId());
        itemDto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static ItemShortDto itemToItemShortDto(Item item) {
        ItemShortDto itemShortDto = new ItemShortDto();
        itemShortDto.setId(item.getId());
        itemShortDto.setName(item.getName());
        return itemShortDto;
    }

    public static ItemOutDtoWithDate toItemOutDtoWithDate(Item item,
                                                    List<Comment> commentList,
                                                    MagicBookings magicBookings) {
        ItemOutDtoWithDate outItemDto = new ItemOutDtoWithDate();
        outItemDto.setId(item.getId());
        outItemDto.setName(item.getName());
        outItemDto.setDescription(item.getDescription());
        outItemDto.setAvailable(item.getAvailable());
        List<OutputCommentDto> commentDtoList = commentList.stream().map(ItemMapper::commentToOutputDto).collect(Collectors.toList());
        outItemDto.setComments(commentDtoList);
        outItemDto.setLastBooking(BookingMapper.toBookDtoForItem(magicBookings.getLastBooking()));
        outItemDto.setNextBooking(BookingMapper.toBookDtoForItem(magicBookings.getNextBooking()));
        return outItemDto;
    }

    public static OutputCommentDto commentToOutputDto(Comment comment) {
        OutputCommentDto outDto = new OutputCommentDto();
        outDto.setId(comment.getId());
        outDto.setText(comment.getText());
        outDto.setItemId(comment.getItem().getId());
        outDto.setAuthorName(comment.getAuthor().getName());
        outDto.setCreated(comment.getCreated());
        return outDto;
    }

    public static  Item toItem(ItemDto itemDto, User owner, ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwner(owner);
        item.setAvailable(itemDto.getAvailable());
        item.setRequest(itemRequest);
        return item;
    }

    public static Comment inputDtoToComment(InputCommentDto commentDto, Item item, User author) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public static ItemDtoForReq itemToItemDtoForReq(Item item) {
        ItemDtoForReq outDto = new ItemDtoForReq();
        outDto.setId(item.getId());
        outDto.setName(item.getName());
        outDto.setDescription(item.getDescription());
        outDto.setAvailable(item.getAvailable());
        outDto.setRequestId(item.getRequest().getId());
        return outDto;
    }
}
