package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto getUserById(Integer userId);

    List<UserDto> getUserList();

    UserDto createUser(UserDto userDto);

    void deleteUser(int userId);

    UserDto updateUser(UserDto userDto);
}

