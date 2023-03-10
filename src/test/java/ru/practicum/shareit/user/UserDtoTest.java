package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserDto() throws IOException {
        UserDto userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Пользователь");
        userDto.setEmail("user@mail.ru");

        JsonContent<UserDto> userJson = json.write(userDto);

        assertThat(userJson).extractingJsonPathStringValue("$.name").isEqualTo("Пользователь");
        assertThat(userJson).extractingJsonPathStringValue("$.email").isEqualTo("user@mail.ru");
        assertThat(userJson).extractingJsonPathNumberValue("$.id").isEqualTo(1);
    }

}