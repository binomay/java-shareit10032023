package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final UserService userService;
    private final ItemRequestService itemRequestService;
    private final EntityManager em;

    @Test
    void createRequest() {
        ItemRequestCreateDto itemRequestCreateDto = createReqDto();
        UserDto userDto = createOneUserDto();
        userDto = userService.createUser(userDto);
        ItemRequestOutDto outDto = itemRequestService.createRequest(userDto.getId(), itemRequestCreateDto);
        Integer reqId = outDto.getId();

        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.id = :id", ItemRequest.class);
        ItemRequest actualItemRequest = query.setParameter("id", reqId).getSingleResult();

        assertEquals(actualItemRequest.getDescription(), itemRequestCreateDto.getDescription());
    }

    @Test
    void getItemRequestById() {
        ItemRequestCreateDto itemRequestCreateDto = createReqDto();
        UserDto userDto = createOneUserDto();
        userDto = userService.createUser(userDto);
        ItemRequestOutDto outDto = itemRequestService.createRequest(userDto.getId(), itemRequestCreateDto);
        Integer reqId = outDto.getId();
        ItemRequest actualRequest = itemRequestService.getItemRequestById(reqId);

        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.id = :id", ItemRequest.class);
        ItemRequest expectedItemRequest = query.setParameter("id", reqId).getSingleResult();

        assertEquals(actualRequest, expectedItemRequest);
    }

    private ItemRequestCreateDto createReqDto() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Какой-то запрос на что-то....");
        return dto;
    }

    private UserDto createOneUserDto() {
        UserDto user = new UserDto();
        user.setName("Тестовый юзер");
        user.setEmail("test@mail.ru");
        return user;
    }


}