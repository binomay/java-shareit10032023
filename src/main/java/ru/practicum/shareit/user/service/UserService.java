package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto getUserDtoById(Integer userId);

    User getUserById(Integer userId);

    List<UserDto> getUserList();

    UserDto createUser(UserDto userDto);

    void deleteUser(int userId);

    UserDto updateUser(UserDto userDto);
}

