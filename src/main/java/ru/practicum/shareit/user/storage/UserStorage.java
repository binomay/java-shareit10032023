package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    void deleteUser(User user);

    Optional<User> getUserById(int userId);

    Optional<Integer> getUserIdByEmail(String email);

    List<User> getUserList();

}
