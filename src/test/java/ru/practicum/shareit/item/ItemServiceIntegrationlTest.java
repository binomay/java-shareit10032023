package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)

class ItemServiceIntegrationlTest {
    private final ItemService itemService;
    private final UserService userService;
    private final EntityManager em;

    @Test
    void createItem() {
        UserDto userDto = createOneUserDto();
        userDto = userService.createUser(userDto);
        ItemDto itemDto = createOneItemDto(userDto.getId());
        itemDto = itemService.createItem(itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getOwner().getId(), itemDto.getOwner());
    }

    @Test
    void updateItem() {
        UserDto userDto = createOneUserDto();
        userDto = userService.createUser(userDto);
        ItemDto itemDto = createOneItemDto(userDto.getId());
        itemDto = itemService.createItem(itemDto);
        Integer itemId = itemDto.getId();
        itemDto.setName("Измененное имя");
        itemDto.setDescription("Измененное описание");
        itemDto.setAvailable(false);
        itemDto = itemService.updateItem(itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemId).getSingleResult();

        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getOwner().getId(), itemDto.getOwner());
    }

    private UserDto createOneUserDto() {
        UserDto user = new UserDto();
        user.setName("Тестовый юзер");
        user.setEmail("test@mail.ru");
        return user;
    }

    private ItemDto createOneItemDto(Integer owneId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("какая-то вещь");
        itemDto.setOwner(owneId);
        itemDto.setAvailable(true);
        itemDto.setDescription("описание какой-то вещи");
        return itemDto;
    }
}