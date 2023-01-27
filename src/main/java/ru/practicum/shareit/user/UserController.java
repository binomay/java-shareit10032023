package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImp;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserServiceImp userService;

    public UserController(UserServiceImp userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Integer userId) {
        return userService.getUserDtoById(userId);
    }

    @GetMapping
    public List<UserDto> getUserList() {
        return userService.getUserList();
    }

    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        return userService.createUser(userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable Integer userId) {
        userDto.setId(userId);
        return userService.updateUser(userDto);
    }
}

