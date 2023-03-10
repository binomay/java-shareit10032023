package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.dto.MagicBookings;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositary.BookingRepositary;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositary.CommentRepository;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.repositary.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepositary bookingRepositary;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ItemRequestService itemRequestService;


    public ItemServiceImpl(ItemRepository itemRepository, UserService userService,
                           BookingRepositary bookingRepositary, CommentRepository commentRepository,
                           ItemRequestService itemRequestService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepositary = bookingRepositary;
        this.commentRepository = commentRepository;
        this.itemRequestService = itemRequestService;
    }

    @Override
    public ItemOutDtoWithDate getItemDtoById(Integer itemId, Integer userId) {
        Item item = getItemById(itemId);
        List<Comment> commentList = commentRepository.findAllByItemInOrderByCreatedDesc(List.of(item));
        List<Booking> bookingList = bookingRepositary.getBookingsByItemOwner(itemId, userId);
        MagicBookings magicBookings = getMagicBookings(bookingList);
        return ItemMapper.toItemOutDtoWithDate(item, commentList, magicBookings);
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
    public List<ItemOutDtoWithDate> getUsersItems(Integer ownerId, Integer from, Integer size) {
        List<ItemOutDtoWithDate> outList = new ArrayList<>();
        Sort sortById = Sort.by("id");
        Pageable pageable = PageRequest.of(from / size, size, sortById);
        List<Item> itemList = itemRepository.findAllByOwner(userService.getUserById(ownerId), pageable);
        Map<Integer, List<Comment>> commentMap = getCommentsMap(itemList);
        Map<Integer, List<Booking>> bookingMap = getBookingMap(itemList);
        for (Item item : itemList) {
            List<Comment> commentList = commentMap.get(item.getId());
            if (commentList == null) {
                commentList = new ArrayList<>();
            }
            List<Booking> bookingList = bookingMap.get(item.getId());
            if (bookingList == null) {
                bookingList = new ArrayList<>();
            }
            MagicBookings magicBookings = getMagicBookings(bookingList);
            outList.add(ItemMapper.toItemOutDtoWithDate(item, commentList, magicBookings));
        }
        return outList;
    }

    private MagicBookings getMagicBookings(List<Booking> bookingList) {
        //букинги отсортированы в порядке возрастания даты начала....
        //Последнее бронирование таково, что началось в самом ближайшем прошлом. А следующее, которое начнется в самом ближайшем будущем.
        MagicBookings magicBookings = new MagicBookings();
        LocalDateTime now = LocalDateTime.now();
        if (bookingList.size() == 0) {
            magicBookings.setLastBooking(null);
            magicBookings.setNextBooking(null);
        } else {
            for (Booking booking : bookingList) {
                if (booking.getStart().isBefore(now)) {
                    magicBookings.setLastBooking(booking);
                }
                if (booking.getStart().isAfter(now)) {
                    magicBookings.setNextBooking(booking);
                    break;
                }
            }
        }
        return magicBookings;
    }

    private Map<Integer, List<Booking>> getBookingMap(List<Item> itemList) {
        List<Booking> bookingList = bookingRepositary.findAllByItemInAndStatusOrderByStart(itemList,
                BookingStatus.APPROVED.getName());
        Map<Integer, List<Booking>> outMap = new HashMap<>();
        List<Booking> currList;
        Integer currItemId;
        for (Booking booking : bookingList) {
            currItemId = booking.getItem().getId();
            if (outMap.containsKey(currItemId)) {
                currList = outMap.get(currItemId);
            } else {
                currList = new ArrayList<>();
            }
            currList.add(booking);
            outMap.put(currItemId, currList);
        }
        return outMap;
    }

    private Map<Integer, List<Comment>>  getCommentsMap(List<Item> itemList) {
        List<Comment> commentList = commentRepository.findAllByItemInOrderByCreatedDesc(itemList);
        Map<Integer, List<Comment>> outMap = new HashMap<>();
        List<Comment> currList;
        Integer currItemId;
        for (Comment comment: commentList) {
            currItemId = comment.getItem().getId();
            if (outMap.containsKey(currItemId)) {
                currList = outMap.get(currItemId);
            } else {
                currList = new ArrayList<>();
            }
            currList.add(comment);
            outMap.put(currItemId, currList);
        }
        return outMap;
    }

    @Override
    public List<ItemDto> getItemsByContextSearch(String context, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        String upperContext = context.toUpperCase();
        if (context.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<Item> itemList = itemRepository.contextSearch(upperContext, pageable);
            return itemList.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
    }

    @Override
    public OutputCommentDto addCommentToItem(InputCommentDto commentDto) {
        Item item = getItemById(commentDto.getItemId());
        User author = userService.getUserById(commentDto.getAuthorId());
        Comment comment = ItemMapper.inputDtoToComment(commentDto, item, author);
        checkComment(comment);
        return ItemMapper.commentToOutputDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        ItemRequest itemRequest;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestService.getItemRequestById(itemDto.getRequestId());
        } else {
            itemRequest = null;
        }
        Item item = ItemMapper.toItem(itemDto, userService.getUserById(itemDto.getOwner()), itemRequest);
        checkAvailable(item);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto, userService.getUserById(itemDto.getOwner()), null);
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

    private void checkAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("При создании Item не может быть недоступным!");
        }
    }

    private void checkOwner(User newOwner, User oldOwner) {
    if (!newOwner.equals(oldOwner)) {
            throw new RightsException("Редактировать вещь может только ее вдладелец");
        }
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