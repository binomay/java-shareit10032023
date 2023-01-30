package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositary.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemRequestService itemRequestService;
    @Mock
    UserService userService;
    @InjectMocks
    ItemServiceImpl itemService;
    @Captor
    ArgumentCaptor<Item> argumentItemCaptor;
    @Captor
    ArgumentCaptor<Integer> argumentItemIdCaptor;

    @Test
    void getItemDtoById_whenItemNotFound_thenResourceNotFoundException() {
        Integer itemId = 1;
        when(itemRepository.getItemById(itemId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () ->itemService.getItemById(itemId));

        assertEquals("Не нашел item с Id = 1", exception.getMessage()
                ,"Не совпали сообщения об ошибке");
    }

    @Test
    void getItemDtoById_whenItemFound_thenReturnItemDto() {
        Integer itemId = 1;
        Integer userId = 1;
        ItemDto expectedItemDto = createOneItemDto(itemId, userId);
        Item expectedItem = createOneItem(itemId, userId);
        doReturn(Optional.of(expectedItem)).when(itemRepository).getItemById(itemId);

        Item actualItemDto = itemService.getItemById(itemId);
        verify(itemRepository).getItemById(argumentItemIdCaptor.capture());
        Integer itemIdForDelete = argumentItemIdCaptor.getValue();

        assertEquals(itemId, itemIdForDelete);
        verify(itemRepository). getItemById(itemId);
        assertEquals(expectedItemDto.getId(), actualItemDto.getId(), "вещи не совпали!");
    }

    @Test
    void getItemById_whenItemNotFound_thenResourceNotFoundException() {
        Integer itemId = 1;
        when(itemRepository.getItemById(itemId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () ->itemService.getItemById(itemId));

        assertEquals("Не нашел item с Id = 1", exception.getMessage()
                ,"Не совпали сообщения об ошибке");
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
    void getUsersItems() {
    }

    @Test
    void getItemsByContextSearch() {
    }

    @Test
    void addCommentToItem() {
    }

    @Test
    void createItem_whenRequestNotFound_thenNotSaved() {
        Integer itemId = 1;
        Integer ownerId  = 1;
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
        Integer itemRequestId = 1;
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
    void updateItem_whenItemRequestNotFound_thenNotSaved() {
        Integer itemId = 1;
        Integer ownerId  = 1;
        Integer itemRequestId = 1;
        ItemDto expectedItemDto = createOneItemDto(itemId, ownerId);
        expectedItemDto.setRequestId(itemRequestId);
        when(itemRequestService.getItemRequestById(itemRequestId))
                .thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class,
                () -> itemService.updateItem(expectedItemDto));
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

    //TODO
    @Test
    void updateItem_whenNotOwnerEdit_thanNotSaved() {
            
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

    private ItemDto createOneItemDto(Integer itemId, Integer ownerId) {
        ItemDto item = new ItemDto();
        item.setId(itemId);
        item.setName("Первая вещь");
        item.setDescription("Вещь номер один");
        item.setAvailable(true);
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
}