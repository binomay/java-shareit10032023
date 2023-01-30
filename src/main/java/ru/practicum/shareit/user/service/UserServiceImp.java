package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositary.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;

    public UserServiceImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto getUserDtoById(Integer userId) {
        return UserMapper.toUserDto(getUserById(userId));
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findUserById(userId).orElseThrow(
                () -> {
                    String msg = "Не нашел пользователя с Id = " + userId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
    }

    @Override
    public List<UserDto> getUserList() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(int userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User oldUser = getUserById(userDto.getId());
        User user = UserMapper.toUser(userDto);
        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

}