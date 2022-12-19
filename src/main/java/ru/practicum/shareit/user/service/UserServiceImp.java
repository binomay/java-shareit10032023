package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.UniqueConstraintException;
import ru.practicum.shareit.numerators.UserNumerator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserServiceImp implements UserService {
    private final UserStorage userStorage;

    public UserServiceImp(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User getUserById(Integer userId) {
        return userStorage.getUserById(userId).orElseThrow(
                () -> {
                    String msg = "Не нашел пользователя с Id = " + userId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
    }

    @Override
    public List<User> getUserList() {
        return userStorage.getUserList();
    }

    @Override
    public User createUser(User user) {
        checkExistingEmail(user);
        user.setId(UserNumerator.getCurrentUserId());
        return userStorage.createUser(user);

    }

    @Override
    public void deleteUser(int userId) {
        User user = getUserById(userId);
        userStorage.deleteUser(user);
    }

    @Override
    public User updateUser(User user) {
        User oldUser = getUserById(user.getId());
        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
        checkExistingEmail(user);
        return userStorage.updateUser(user);

    }

    private void checkExistingEmail(User user) {
        Integer someUserId = userStorage.getUserIdByEmail(user.getEmail()).orElse(-1);
        if (!someUserId.equals(user.getId()) && someUserId != -1) {
            throw new UniqueConstraintException("Не могу поменять e-mail, т.к. пользователь с e-mail: " + user.getEmail() + " уже существует!");
        }
    }

}