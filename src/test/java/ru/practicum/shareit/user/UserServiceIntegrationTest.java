package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shareit1",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {
    private final UserService service;
    private final EntityManager em;

    @Test
    void createUser() {
        UserDto userDto = createOneUserDto();
        UserDto actualUser = service.createUser(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", actualUser.getId()).getSingleResult();
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void updateUser() {
        UserDto userDto = createOneUserDto();
        UserDto actualUser = service.createUser(userDto);
        Integer userId = actualUser.getId();
        userDto.setId(actualUser.getId());
        userDto.setName("Новое имя");
        userDto.setEmail("newUser@mail.ru");

        actualUser = service.updateUser(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", userId).getSingleResult();

        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());

    }

    private UserDto createOneUserDto() {
        UserDto user = new UserDto();
        user.setName("Тестовый юзер");
        user.setEmail("test@mail.ru");
        return user;
    }
}