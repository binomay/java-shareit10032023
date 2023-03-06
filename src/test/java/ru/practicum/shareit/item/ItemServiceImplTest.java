package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.BookingStatus;
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
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemRequestService itemRequestService;
    @Mock
    UserService userService;
    @Mock
    CommentRepository commentRepository;
    @Mock
    BookingRepositary bookingRepositary;
    @InjectMocks
    ItemServiceImpl itemService;
    @Captor
    ArgumentCaptor<Item> argumentItemCaptor;
    @Captor
    ArgumentCaptor<Integer> argumentItemIdCaptor;

    @Test
    void getItemDtoById_whenItemNotFound_thenResourceNotFoundException() {
        Integer itemId = 1;
        when(itemRepository.getItemById(itemId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(itemId));

        assertEquals("Не нашел item с Id = 1", exception.getMessage(), "Не совпали сообщения об ошибке");
    }

    @Test
    void getItemDtoById_whenItemFound_thenReturnItemDto() {
        Integer ownerId = 1;
        Item item = createOneItem(1, ownerId);
        List<Item> itemList = List.of(item);
        User booker = createSecondUser();
        Booking lastBooking = createLastBooking(item, booker);
        Booking nextBooking = createNextBooking(item, booker);
        List<Booking> bookingList = List.of(lastBooking, nextBooking);
        Comment commentForItem = createOneComment(item, booker);
        List<Comment> commentList = List.of(commentForItem);
        when(itemRepository.getItemById(item.getId()))
                .thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemInOrderByCreatedDesc(itemList))
                .thenReturn(commentList);
        when(commentRepository.findAllByItemInOrderByCreatedDesc(itemList))
                .thenReturn(List.of(commentForItem));
        when(bookingRepositary.getBookingsByItemOwner(item.getId(), ownerId))
                .thenReturn(bookingList);

        ItemOutDtoWithDate actualItemDto = itemService.getItemDtoById(item.getId(), ownerId);

        verify(itemRepository).getItemById(item.getId());
        verify(commentRepository).findAllByItemInOrderByCreatedDesc(itemList);
        verify(bookingRepositary).getBookingsByItemOwner(item.getId(), ownerId);

        assertEquals(actualItemDto.getId(), item.getId());
        assertEquals(actualItemDto.getName(), item.getName());
        assertEquals(actualItemDto.getLastBooking().getId(), lastBooking.getId());
        assertEquals(actualItemDto.getNextBooking().getId(), nextBooking.getId());
    }


    @Test
    void getItemById_whenItemNotFound_thenResourceNotFoundException() {
        Integer itemId = 1;
        when(itemRepository.getItemById(itemId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(itemId));

        assertEquals("Не нашел item с Id = 1", exception.getMessage(), "Не совпали сообщения об ошибке");
    }

    @Test
    void getItemById_whenItemFound_thenReturnItem() {
        Integer itemId = 1;
        Integer userId = 1;
        Item expectedItem = createOneItem(itemId, userId);
        doReturn(Optional.of(expectedItem)).when(itemRepository).getItemById(itemId);

        Item actualItem = itemService.getItemById(itemId);
        verify(itemRepository).getItemById(argumentItemIdCaptor.capture());
        Integer itemIdForDelete = argumentItemIdCaptor.getValue();

        assertEquals(itemId, itemIdForDelete);
        verify(itemRepository). getItemById(itemId);
        assertEquals(expectedItem, actualItem, "вещи не совпали!");
    }

    @Test
    void getUsersItems_whenTwoBooking_thanReturn() {
        Integer ownerId = 1;
        Item item1 = createOneItem(1, ownerId);
        Item item2 = createOneItem(2, ownerId);
        User owner = createOneUser(ownerId);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("id"));
        List<Item> itemList = List.of(item1, item2);
        User booker = createSecondUser();
        Booking book1 = createLastBooking(item1, booker);
        Booking book2 = createNextBooking(item2, booker);
        List<Booking> bookingList = List.of(book1, book2);
        Comment commentForItem1 = createOneComment(item1, booker);
        when(userService.getUserById(ownerId)).thenReturn(owner);
        when(itemRepository.findAllByOwner(owner, pageable))
                .thenReturn(itemList);
        when(commentRepository.findAllByItemInOrderByCreatedDesc(itemList))
                .thenReturn(List.of(commentForItem1));
        when(bookingRepositary.findAllByItemInAndStatusOrderByStart(itemList,
                BookingStatus.APPROVED.getName()))
                .thenReturn(bookingList);

        List<ItemOutDtoWithDate> actualList = itemService.getUsersItems(ownerId, 0, 1);

        verify(itemRepository).findAllByOwner(owner, pageable);
        verify(commentRepository).findAllByItemInOrderByCreatedDesc(itemList);
        verify(bookingRepositary).findAllByItemInAndStatusOrderByStart(itemList,
                BookingStatus.APPROVED.getName());
        assertEquals(actualList.size(), 2);
        assertEquals((actualList.get(0).getId()), item1.getId());
        assertEquals(actualList.get(0).getName(), item1.getName());
        assertEquals(actualList.get(0).getLastBooking().getId(), book1.getId());
    }

    @Test
    void getUsersItems_whenNoOneBooking_thanReturn() {
        Integer ownerId = 1;
        Item item1 = createOneItem(1, ownerId);
        Item item2 = createOneItem(2, ownerId);
        User owner = createOneUser(ownerId);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("id"));
        List<Item> itemList = List.of(item1, item2);
        User booker = createSecondUser();
        List<Booking> bookingList = new ArrayList<>();
        Comment commentForItem1 = createOneComment(item1, booker);
        when(userService.getUserById(ownerId)).thenReturn(owner);
        when(itemRepository.findAllByOwner(owner, pageable))
                .thenReturn(itemList);
        when(commentRepository.findAllByItemInOrderByCreatedDesc(itemList))
                .thenReturn(List.of(commentForItem1));
        when(bookingRepositary.findAllByItemInAndStatusOrderByStart(itemList,
                BookingStatus.APPROVED.getName()))
                .thenReturn(bookingList);

        List<ItemOutDtoWithDate> actualList = itemService.getUsersItems(ownerId, 0, 1);

        verify(itemRepository).findAllByOwner(owner, pageable);
        verify(commentRepository).findAllByItemInOrderByCreatedDesc(itemList);
        verify(bookingRepositary).findAllByItemInAndStatusOrderByStart(itemList,
                BookingStatus.APPROVED.getName());
        assertEquals(actualList.size(), 2);
        assertEquals((actualList.get(0).getId()), item1.getId());
        assertEquals(actualList.get(0).getName(), item1.getName());
        assertNull(actualList.get(0).getLastBooking());
    }

    @Test
    void getItemsByContextSearch_whenEmptyContext() {
        String context = "";
        List<ItemDto> itemDtoList = itemService.getItemsByContextSearch(context, 0, 2);
        assertEquals(itemDtoList.size(), 0);
    }

    @Test
    void getItemsByContextSearch_whenNotEmptyContext_Return() {
        Pageable pageable = PageRequest.of(0,2);
        Item item1 = createOneItem(1, 1);
        Item item2 = createSecondItem(2,2);
        List<Item> itemList = List.of(item1, item2);
        String context = "Какой-то контекст";
        when(itemRepository.contextSearch(context.toUpperCase(), pageable))
                .thenReturn(itemList);

        List<ItemDto> actualItemDtoList = itemService.getItemsByContextSearch(context,
                pageable.getPageNumber(),
                pageable.getPageSize());

        verify(itemRepository).contextSearch(context.toUpperCase(), pageable);
        assertEquals(actualItemDtoList.size(), 2);
        assertEquals(actualItemDtoList.get(0).getId(), 1);
        assertEquals(actualItemDtoList.get(1).getId(), 2);
    }

    @Test
    void addCommentToItem_whenItemNotFound_thanNotSaved() {
        InputCommentDto dto = new InputCommentDto();
        Integer itemId = 1;
        Integer authorId = 1;
        dto.setItemId(itemId);
        dto.setAuthorId(authorId);
        dto.setText("какой-то комметарий");
        when(itemRepository.getItemById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.addCommentToItem(dto));
    }

    //TODO
    @Test
    void addCommentToItem_whenOkSaved() {
        Integer commentId = 1;
        Integer itemId = 1;
        Integer authorId = 1;
        User author = createOneUser(authorId);
        InputCommentDto dto = new InputCommentDto();
        dto.setId(commentId);
        dto.setItemId(itemId);
        dto.setAuthorId(authorId);
        dto.setText("какой-то комментарий");
        Integer ownerId = 2;
        User owner = createSecondUser();
        owner.setId(ownerId);
        Item item = createOneItem(itemId, owner.getId());
        List<Item> itemList = List.of(item);
        item.setId(itemId);
        Comment comment = ItemMapper.inputDtoToComment(dto, item, author);

        when(itemRepository.getItemById(itemId))
                .thenReturn(Optional.of(item));
        when(itemRepository.getItemsWasCompleteBookingByUser(any(), any()))
                .thenReturn(itemList);
        when(commentRepository.save(comment))
                .thenReturn(comment);

        OutputCommentDto out = itemService.addCommentToItem(dto);

        verify(commentRepository).save(comment);
        assertEquals(out.getItemId(), dto.getItemId());
        assertEquals(out.getText(), dto.getText());
    }

    @Test
    void addCommentToItem_whenNotValidated_thanNotSaved() {
        InputCommentDto dto = new InputCommentDto();
        Integer commentId = 1;
        Integer itemId = 1;
        Integer authorId = 1;
        User author = createOneUser(authorId);
        dto.setId(commentId);
        dto.setItemId(itemId);
        dto.setAuthorId(authorId);
        dto.setText("какой-то комментарий");
        Item item = new Item();
        item.setId(itemId);
        Comment comment = new Comment();
        when(itemRepository.getItemById(itemId))
                .thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class, () -> itemService.addCommentToItem(dto));
        assertEquals(exception.getMessage(), "Пользователь не брал вещь в аренду или аренда не закончена");
    }

    @Test
    void createItem_whenRequestNotFound_thenNotSaved() {
        Integer itemId = 1;
        Integer ownerId = 1;
        Integer itemRequestId = 1;
        ItemDto expectedItemDto = createOneItemDto(itemId, ownerId);
        expectedItemDto.setRequestId(itemRequestId);
        when(itemRequestService.getItemRequestById(itemRequestId))
                .thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class,
                () -> itemService.createItem(expectedItemDto));
    }

    @Test
    void createItem_whenNotAvialabel_thanValidationException() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        ItemDto expectedItemDto = createOneItemDto(itemId, ownerId);
        expectedItemDto.setRequestId(null);
        expectedItemDto.setAvailable(false);
        when(userService.getUserById(ownerId))
                .thenReturn(new User());

        assertThrows(ValidationException.class,
                () -> itemService.createItem(expectedItemDto));
    }

    @Test
    void createItem_whenAvialabel_thanSave() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        Item expectedItem = createOneItem(itemId, ownerId);
        ItemDto expectedItemDto = createOneItemDto(itemId, ownerId);
        expectedItemDto.setRequestId(null);
        doReturn(createOneUser(ownerId))
                .when(userService).getUserById(ownerId);
        when(itemRepository.save(expectedItem))
                .thenReturn(expectedItem);

        ItemDto actualItemDto = itemService.createItem(expectedItemDto);
        verify(itemRepository).save(argumentItemCaptor.capture());
        Item actualItem = argumentItemCaptor.getValue();

        assertEquals(expectedItem, actualItem);
        verify(itemRepository).save(expectedItem);
        assertEquals(expectedItemDto, actualItemDto);
    }

    @Test
    void createItem_whenItemReqvNotNullAndAllOk_thanSave() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        Integer requesterId = 2;
        User requester = createSecondUser();
        Item expectedItem = createOneItem(itemId, ownerId);
        ItemDto expectedItemDto = createOneItemDto(itemId, ownerId);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now().minusDays(2));
        itemRequest.setDescription("Хочу сдать спринт 15 побыстрее. Задолбали тесты...");
        expectedItemDto.setRequestId(itemRequest.getId());
        doReturn(itemRequest).when(itemRequestService).getItemRequestById(itemRequest.getId());
        doReturn(createOneUser(ownerId))
                .when(userService).getUserById(ownerId);
        when(itemRepository.save(expectedItem))
                .thenReturn(expectedItem);

        ItemDto actualItemDto = itemService.createItem(expectedItemDto);
        verify(itemRepository).save(argumentItemCaptor.capture());
        Item actualItem = argumentItemCaptor.getValue();

        assertEquals(expectedItem, actualItem);
        verify(itemRepository).save(expectedItem);
        assertEquals(expectedItemDto.getId(), actualItemDto.getId());
    }

    @Test
    void updateItem_whenItemNotFound_thenNotSaved() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        ItemDto expectedItemDto = createOneItemDto(itemId, ownerId);
        expectedItemDto.setRequestId(null);
        when(itemRepository.getItemById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> itemService.updateItem(expectedItemDto));
    }

    @Test
    void updateItem_whenNewOwnerNotEqOldOwner_thanNotSaved() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        Integer newOwnerId = 2;
        User owner = createOneUser(ownerId);
        User newOwner = createSecondUser();
        ItemDto oldItemDto = createOneItemDto(itemId, ownerId);
        oldItemDto.setRequestId(null);
        Item oldItem = createOneItem(itemId, ownerId);
        oldItem.setRequest(null);
        ItemDto newItemDto = createSecondItemDto(itemId, newOwnerId);
        newItemDto.setRequestId(null);
        Item newItem = createOneItem(itemId, ownerId);
        newItem.setRequest(null);
        doReturn(Optional.of(oldItem)).when(itemRepository).getItemById(any());
        doReturn(newOwner).when(userService).getUserById(any());
        RightsException exception = assertThrows(RightsException.class,
                () -> itemService.updateItem(newItemDto));

        assertEquals(exception.getMessage(), "Редактировать вещь может только ее вдладелец");
    }

    @Test
    void updateItem_whenAllOk_thenSave() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        User owner = createOneUser(ownerId);
        ItemDto oldItemDto = createOneItemDto(itemId, ownerId);
        oldItemDto.setRequestId(null);
        Item oldItem = createOneItem(itemId, ownerId);
        oldItem.setRequest(null);
        ItemDto newItemDto = createSecondItemDto(itemId, ownerId);
        newItemDto.setRequestId(null);
        Item newItem = createOneItem(itemId, ownerId);
        newItem.setAvailable(false);
        newItem.setRequest(null);
        doReturn(Optional.of(oldItem)).when(itemRepository).getItemById(itemId);
        doReturn(owner).when(userService).getUserById(ownerId);
        doReturn(newItem).when(itemRepository).save(newItem);

        ItemDto actualItemDto = itemService.updateItem(newItemDto);
        verify(itemRepository).save(argumentItemCaptor.capture());
        Item actualItem = argumentItemCaptor.getValue();

        assertEquals(actualItemDto.getName(), newItem.getName());
        assertEquals(actualItemDto.getDescription(), newItem.getDescription());
        assertEquals(actualItem.getAvailable(), newItem.getAvailable());
        verify(itemRepository).save(newItem);
    }

    @Test
    void updateItem_whenAllOkAndSomeFieldIsNull_thenSave() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        User owner = createOneUser(ownerId);
        Integer newOwnerId = 2;
        Item oldItem = createOneItem(itemId, ownerId);
        oldItem.setRequest(null);
        ItemDto newItemDto = new ItemDto();
        newItemDto.setId(oldItem.getId());
        newItemDto.setRequestId(null);
        newItemDto.setOwner(newOwnerId);
        newItemDto.setName(null);
        newItemDto.setDescription(null);
        newItemDto.setAvailable(null);
        Item newItem = createOneItem(itemId, ownerId);
        newItem.setRequest(null);
        doReturn(Optional.of(oldItem)).when(itemRepository).getItemById(newItemDto.getId());
        doReturn(owner).when(userService).getUserById(newOwnerId);
        doReturn(oldItem).when(itemRepository).save(any());

        ItemDto actualItemDto = itemService.updateItem(newItemDto);

        assertEquals(actualItemDto.getName(), oldItem.getName());
        assertEquals(actualItemDto.getDescription(), oldItem.getDescription());
        assertEquals(actualItemDto.getAvailable(), oldItem.getAvailable());
        assertEquals(actualItemDto.getOwner(), oldItem.getOwner().getId());
        verify(itemRepository).save(oldItem);
    }

    private Item createOneItem(Integer itemId, Integer ownerId) {
        Item item = new Item();
        item.setId(itemId);
        item.setName("Первая вещь");
        item.setDescription("Вещь номер один");
        item.setAvailable(true);
        User owner = new User();
        owner.setId(ownerId);
        item.setOwner(owner);
        return item;
    }

    private Item createSecondItem(Integer itemId, Integer ownerId) {
        Item item = new Item();
        item.setId(itemId);
        item.setName("Вторая вещь");
        item.setDescription("Вещь номер два");
        item.setAvailable(false);
        User owner = new User();
        owner.setId(ownerId);
        item.setOwner(owner);
        return item;
    }

    private ItemDto createOneItemDto(Integer itemId, Integer ownerId) {
        ItemDto item = new ItemDto();
        item.setId(itemId);
        item.setName("Первая вещь");
        item.setDescription("Вещь номер один");
        item.setAvailable(true);
        item.setOwner(ownerId);
        return item;
    }

    private ItemDto createSecondItemDto(Integer itemId, Integer ownerId) {
        ItemDto item = new ItemDto();
        item.setId(itemId);
        item.setName("Вторая вещь");
        item.setDescription("Вещь номер два");
        item.setAvailable(false);
        item.setOwner(ownerId);
        return item;
    }

    private User createOneUser(Integer userId) {
        User user = new User();
        user.setId(userId);
        user.setName("юзер 1");
        user.setEmail("firstuser@mail.ru");
        return user;
    }

    private User createSecondUser() {
        User user = new User();
        user.setId(2);
        user.setName("юзер 2");
        user.setEmail("seconduser@mail.ru");
        return user;
    }

    private Booking createLastBooking(Item item, User booker) {
        Booking book = new Booking();
        book.setId(1);
        book.setItem(item);
        book.setBooker(booker);
        book.setStatus(BookingStatus.APPROVED.getName());
        book.setStart(LocalDateTime.now().minusDays(2));
        book.setEnd(LocalDateTime.now().minusDays(1));
        return book;
    }

    private Booking createNextBooking(Item item, User booker) {
        Booking book = new Booking();
        book.setId(2);
        book.setItem(item);
        book.setBooker(booker);
        book.setStatus(BookingStatus.APPROVED.getName());
        book.setStart(LocalDateTime.now().plusDays(1));
        book.setEnd(LocalDateTime.now().plusDays(3));
        return book;
    }

    private Comment createOneComment(Item item, User author) {
        Comment comment = new Comment();
        comment.setId(1);
        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(author);
        comment.setText("какой-то текст");
        comment.setItem(item);
        return comment;
    }
}