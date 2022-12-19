package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User getUserById(Integer userId);

    List<User> getUserList();

    User createUser(User user);

    void deleteUser(int userId);

    User updateUser(User user);
}

