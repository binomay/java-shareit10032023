package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositary.ItemRepository;
import ru.practicum.shareit.request.dto.ItemReqDtoForResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositary.ItemRequestRepositary;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;
    @Mock
    UserServiceImp userService;
    @Mock
    ItemRequestRepositary itemRequestRepositary;
    @Mock
    ItemRepository itemRepository;
    @Captor
    ArgumentCaptor<ItemRequest> argumentItemRequestCaptor;

    @Test
    void createRequest() {
        User user = createFirstUser();
        Integer requestId = 1;
        ItemRequest itemRequest = createFirstItemRequest(user);
        itemRequest.setId(null);
        ItemRequestCreateDto inputDto = createCreationDto(itemRequest);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemRequestRepositary.save(any())).thenReturn(itemRequest);

        ItemRequestOutDto expectedDto = itemRequestService.createRequest(user.getId(), inputDto);

        verify(itemRequestRepositary).save(argumentItemRequestCaptor.capture());
        ItemRequest actualItemRequest = argumentItemRequestCaptor.getValue();

        assertEquals(actualItemRequest.getId(), itemRequest.getId());
        assertEquals(actualItemRequest.getDescription(), itemRequest.getDescription());
        assertEquals(actualItemRequest.getRequester(), itemRequest.getRequester());
        assertEquals(expectedDto.getId(), itemRequest.getId());
        assertEquals(expectedDto.getDescription(), itemRequest.getDescription());
    }

    @Test
    void getItemRequestById_whenFound() {
        User user = createFirstUser();
        ItemRequest itemRequest = createFirstItemRequest(user);
        Integer requestId = itemRequest.getId();
        when(itemRequestRepositary.findById(any())).thenReturn(Optional.of(itemRequest));

        ItemRequest actualItemRequest = itemRequestService.getItemRequestById(requestId);

        assertEquals(itemRequest, actualItemRequest);
        verify(itemRequestRepositary).findById(requestId);
    }

    @Test
    void getItemRequestById_whenNotFound() {
        User user = createFirstUser();
        Integer requestId = 1;
        when(itemRequestRepositary.findById(any())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> itemRequestService.getItemRequestById(requestId));
        assertEquals(exception.getMessage(), "Не нашел request с Id = " + requestId);
        verify(itemRequestRepositary).findById(requestId);
    }

    @Test
    void getRequestsByUser() {
        User requesterOne = createFirstUser();
        ItemRequest itemRequestOne = createFirstItemRequest(requesterOne);
        ItemRequest itemRequestTwo = createSecondItemRequest(requesterOne);
        List<ItemRequest> itemRequestList = List.of(itemRequestTwo, itemRequestOne);
        User user = requesterOne;
        User ownerOne = createThirdUser();
        Item item1 = createFirstItem(ownerOne, itemRequestOne);
        List<Item> itemList = List.of(item1);
        when(itemRequestRepositary.findAllByRequesterOrderById(any()))
                .thenReturn(itemRequestList);
        when(itemRepository.findByRequestInOrderById(any()))
                .thenReturn(itemList);

        List<ItemReqDtoForResponse> actualOut = itemRequestService.getRequestsByUser(user.getId());

        assertEquals(actualOut.size(), 2);
        assertEquals(actualOut.get(0).getId(), 2);
        assertEquals(actualOut.get(1).getId(), 1);
        assertEquals(actualOut.get(0).getItems().size(), 0);
        assertEquals(actualOut.get(1).getItems().size(), 1);
        assertEquals(actualOut.get(1).getItems().get(0).getId(), 1);
        assertEquals(actualOut.get(1).getItems().get(0).getRequestId(), 1);
    }

    @Test
    void getAll() {
        User requesterOne = createFirstUser();
        User requesterTwo = createSecondUser();
        ItemRequest itemRequestOne = createFirstItemRequest(requesterOne);
        ItemRequest itemRequestTwo = createSecondItemRequest(requesterTwo);
        List<ItemRequest> itemRequestList = List.of(itemRequestTwo, itemRequestOne);
        Integer from = 0;
        Integer size = 2;
        User user = createFourthUser();
        User ownerOne = createThirdUser();
        Item item1 = createFirstItem(ownerOne, itemRequestOne);
        List<Item> itemList = List.of(item1);
        when(itemRequestRepositary.findAll(any(), any()))
                .thenReturn(itemRequestList);
        when(itemRepository.findByRequestInOrderById(any()))
                .thenReturn(itemList);

        List<ItemReqDtoForResponse> actualOut = itemRequestService.getAll(user.getId(), from, size);

        assertEquals(actualOut.size(), 2);
        assertEquals(actualOut.get(0).getId(), 2);
        assertEquals(actualOut.get(1).getId(), 1);
        assertEquals(actualOut.get(0).getItems().size(), 0);
        assertEquals(actualOut.get(1).getItems().size(), 1);
        assertEquals(actualOut.get(1).getItems().get(0).getId(), 1);
        assertEquals(actualOut.get(1).getItems().get(0).getRequestId(), 1);
    }

    @Test
    void getItemRequestDtoById() {
        User requesterOne = createFirstUser();
        ItemRequest itemRequestOne = createFirstItemRequest(requesterOne);
        User user = createFourthUser();
        User ownerOne = createThirdUser();
        User ownerTwo = createFourthUser();
        Item item1 = createFirstItem(ownerOne, itemRequestOne);
        Item item2 = createSecondItem(ownerTwo, itemRequestOne);
        List<Item> itemList = List.of(item1, item2);
        doReturn(user).when(userService).getUserById(any());
        when(itemRequestRepositary.findById(any())).thenReturn(Optional.of(itemRequestOne));
        when(itemRepository.findByRequestInOrderById(any()))
                .thenReturn(itemList);

        ItemReqDtoForResponse actualOut = itemRequestService.getItemRequestDtoById(itemRequestOne.getId(), user.getId());

        assertEquals(actualOut.getId(), 1);
        assertEquals(actualOut.getItems().get(0).getId(), 1);
        assertEquals(actualOut.getItems().get(1).getId(), 2);
    }

    private User createFirstUser() {
        return createUser(1, "Пользователь1", "user1@mail.ru");
    }

    private User createSecondUser() {
        return createUser(2, "Пользователь2", "user1@mail.ru");
    }

    private User createThirdUser() {
        return createUser(3, "Пользователь3", "user3@mail.ru");
    }

    private User createFourthUser() {
        return createUser(4, "Пользователь4", "user4@mail.ru");
    }

    private User createUser(Integer userId, String name, String email) {
        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemRequest createItemRequest(Integer requestId, User requester, String description) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription(description);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(requester);
        return itemRequest;
    }

    private ItemRequest createFirstItemRequest(User user) {
        return createItemRequest(1, user, "Первый запрос вещи");
    }

    private ItemRequest createSecondItemRequest(User user) {
        return createItemRequest(2, user, "второй запрос вещи");
    }


    private ItemRequestCreateDto createCreationDto(ItemRequest itemRequest) {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription(itemRequest.getDescription());
        return dto;
    }

    private Item createItem(Integer id, User owner, String name, ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(id);
        item.setAvailable(true);
        item.setName(name);
        item.setDescription(name);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(itemRequest);
        return item;
    }

    private Item createFirstItem(User owner, ItemRequest itemRequest) {
        return createItem(1, owner, "Первая вещь", itemRequest);
    }

    private Item createSecondItem(User owner, ItemRequest itemRequest) {
        return createItem(2, owner, "вторая вещь", itemRequest);
    }
}