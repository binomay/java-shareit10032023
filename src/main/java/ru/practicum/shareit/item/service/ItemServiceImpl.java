package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepositary;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositary.CommentRepository;
import ru.practicum.shareit.item.repositary.ItemRepository;
import ru.practicum.shareit.numerators.ItemNumerator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepositary bookingRepositary;
    private final UserService userService;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserService userService,
                           BookingRepositary bookingRepositary, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepositary = bookingRepositary;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemOutDtoWithDate getItemDtoById(Integer itemId, Integer userId) {
        Item item = getItemById(itemId);
        return toItemOutDtoWithDate(item, userId);
    }

    private List<Booking> getLastAndNextBookList(Integer itemId, Integer userId) {
        List<Booking> bookingList = bookingRepositary.getBookingsByItemOwner(itemId, userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> outList = new ArrayList<>();
        if (bookingList.size() == 0) {
            return outList;
        }
        Booking prevBooking = bookingList.get(0);
        if (prevBooking.getStart().isAfter(now)) {
            return outList;
        }
        if (bookingList.get(bookingList.size() - 1).getStart().isBefore(now)) {
            outList.add(bookingList.get(bookingList.size() - 1));
            return outList;
        }
        Booking currBooking;
        for (int i = 1; i < bookingList.size(); i++) {
            currBooking = bookingList.get(i);
            if (currBooking.getStart().isAfter(now)) {
                outList.add(prevBooking);
                outList.add(currBooking);
                return outList;
            }
        }
        return null;
    }

    @Override
    public Item getItemById(Integer itemId) {
        return itemRepository.getItemById(itemId).orElseThrow(
                () -> {
                    String msg = "Не нашел item с Id = " + itemId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
    }

    @Override
    public List<ItemOutDtoWithDate> getUsersItems(Integer ownerId) {
        List<Item> itemList = itemRepository.findAllByOwnerOrderById(userService.getUserById(ownerId));
        return itemList.stream().map(x -> toItemOutDtoWithDate(x, ownerId)).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByContextSearch(String context) {
        String upperContext = context.toUpperCase();
        if (context.isEmpty()) {
            return new ArrayList<>();
        } else {
            return itemRepository.contextSearch(upperContext).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
    }

    @Override
    public OutputCommentDto addCommentToItem(InputCommentDto commentDto) {
        Comment comment = InputDtoToComment(commentDto);
        checkComment(comment);
        return commentToOutputDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        Item item = toItem(itemDto);
        checkAvailable(item);
        item.setId(ItemNumerator.getCurrenItemId());
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = toItem(itemDto);
        Item oldItem = getItemById(item.getId());
        //отредактировать вещь может только ее владелец
        checkOwner(item.getOwner(), oldItem.getOwner());
        //отредактировать можно только название, комментарий и доступность...
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(oldItem));
    }

    private Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        User owner = userService.getUserById(itemDto.getOwner());
        item.setOwner(owner);
        item.setAvailable(itemDto.getAvailable());
        item.setRequest(itemDto.getRequest());
        return item;
    }

    private void checkAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("При создании Item не может быть недоступным!");
        }
    }

    private void checkOwner(User newOwner, User oldOwner) {
        if (newOwner == null) {
            throw new RightsException("Не указан owner");
        } else if (!newOwner.equals(oldOwner)) {
            throw new RightsException("Редактировать вещь может только ее вдладелец");
        }
    }

    private ItemOutDtoWithDate toItemOutDtoWithDate(Item item, Integer userId) {
        ItemOutDtoWithDate outItemDto = new ItemOutDtoWithDate();
        outItemDto.setId(item.getId());
        outItemDto.setName(item.getName());
        outItemDto.setDescription(item.getDescription());
        outItemDto.setAvailable(item.getAvailable());
        List<OutputCommentDto> commentDtoList = commentRepository.findCommentsByItemOrderByCreatedDesc(item)
                .stream().map(this::commentToOutputDto).collect(Collectors.toList());
        outItemDto.setComments(commentDtoList);
        List<Booking> bookingList = getLastAndNextBookList(item.getId(), userId);
        if (bookingList.size() == 0) {
            outItemDto.setNextBooking(null);
            outItemDto.setLastBooking(null);
        } else if (bookingList.size() == 1) {
            outItemDto.setLastBooking(BookingMapper.toBookDtoForItem(bookingList.get(0)));
            outItemDto.setNextBooking(null);
        } else {
            outItemDto.setLastBooking(BookingMapper.toBookDtoForItem(bookingList.get(0)));
            outItemDto.setNextBooking(BookingMapper.toBookDtoForItem(bookingList.get(1)));
        }
        return outItemDto;
    }

    private Comment InputDtoToComment(InputCommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        Item item = getItemById(commentDto.getItemId());
        comment.setItem(item);
        User author = userService.getUserById(commentDto.getAuthorId());
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private OutputCommentDto commentToOutputDto(Comment comment) {
        OutputCommentDto outDto = new OutputCommentDto();
        outDto.setId(comment.getId());
        outDto.setText(comment.getText());
        outDto.setItemId(comment.getItem().getId());
        outDto.setAuthorName(comment.getAuthor().getName());
        outDto.setCreated(comment.getCreated());
        return outDto;
    }

    private void checkComment(Comment comment) {
        List<Item> tmpList = itemRepository.getItemsWasCompleteBookingByUser(comment.getItem().getId(), comment.getCreated());
        if (tmpList.size() == 0) {
            String msg = "Пользователь не брал вещь в аренду или аренда не закончена";
            log.warn(msg);
            throw new ValidationException(msg);
        }
    }
}