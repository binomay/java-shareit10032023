package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.UniqueConstraintException;
import ru.practicum.shareit.numerators.UserNumerator;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImp implements UserService {
    private final UserStorage userStorage;

    public UserServiceImp(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto getUserById(Integer userId) {
        User user =  userStorage.getUserById(userId).orElseThrow(
                () -> {
                    String msg = "Не нашел пользователя с Id = " + userId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUserList() {
        return   userStorage.getUserList().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        checkExistingEmail(user);
        user.setId(UserNumerator.getCurrentUserId());
        return UserMapper.toUserDto(userStorage.createUser(user));

    }

    @Override
    public void deleteUser(int userId) {
        UserDto userDto = getUserById(userId);
        userStorage.deleteUser(UserMapper.toUser(userDto));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User oldUser = UserMapper.toUser(getUserById(userDto.getId()));
        User user = UserMapper.toUser(userDto);
        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
        checkExistingEmail(user);
        return UserMapper.toUserDto(userStorage.updateUser(user));
    }

    private void checkExistingEmail(User user) {
        Integer someUserId = userStorage.getUserIdByEmail(user.getEmail()).orElse(-1);
        if (!someUserId.equals(user.getId()) && someUserId != -1) {
            throw new UniqueConstraintException("Не могу поменять e-mail, т.к. пользователь с e-mail: " + user.getEmail() + " уже существует!");
        }
    }

}