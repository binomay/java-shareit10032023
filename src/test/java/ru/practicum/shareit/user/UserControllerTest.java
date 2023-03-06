package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private  ObjectMapper objectMapper;
    private UserDto userDto;
    private User user;

    @BeforeEach
    public void beforeAll() {
        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Тестовый пользователь");
        userDto.setEmail("user@mail.ru");

        user = new User();
        user.setId(1);
        user.setName("Тестовый пользователь");
        user.setEmail("user@mail.ru");
    }

    @SneakyThrows
    @Test
    void getUserById()  {
        Integer userId = 1;
        when(userService.getUserDtoById(any())).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()));

        verify(userService).getUserDtoById(userId);
    }

    @SneakyThrows
    @Test
    void getUserList() {
        List<UserDto> userList = List.of(userDto);

        when(userService.getUserList()).thenReturn(userList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userDto.getId()));
    }

    @SneakyThrows
    @Test
    void createUser() {
        when(userService.createUser(userDto)).thenReturn(userDto);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()));

        verify(userService).createUser(userDto);
    }

    @SneakyThrows
    @Test
    void deleteUser() {

        mockMvc.perform(delete("/users/{userId}", userDto.getId()))
                .andExpect(status().isOk());

        verify(userService).deleteUser(userDto.getId());
    }

    @SneakyThrows
    @Test
    void updateUser() {
        when(userService.updateUser(userDto)).thenReturn(userDto);

        mockMvc.perform(patch("/users/{userId}", userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()));

        verify(userService).updateUser(userDto);
    }
}