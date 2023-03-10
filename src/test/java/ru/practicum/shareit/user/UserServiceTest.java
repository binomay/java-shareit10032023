package ru.practicum.shareit.user;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositary.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserServiceImp userService;
    @Captor
    ArgumentCaptor<User> argumentUserCaptor;
    @Captor
    ArgumentCaptor<Integer> argumentUserIdCaptor;

    @Test
    void getUserById_whenUserFound_thenReturnUser() {
        Integer userId = 1;
        User expectedUser = createOneUser(userId);
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserById(userId);
        verify(userRepository).findUserById(argumentUserIdCaptor.capture());
        Integer userIdForDelete = argumentUserIdCaptor.getValue();

        assertEquals(userId, userIdForDelete);
        assertEquals(expectedUser, actualUser, "Пользователи не совпали!");
        verify(userRepository).findUserById(userId);
    }

    @Test
    void getUserById_whenUserNotFound_thenResourceNotFoundException() {
        Integer userId = 1;
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));

        assertEquals("Не нашел пользователя с Id = 1", exception.getMessage(),
                "Не совпали сообщения об ошибке");
    }

    @Test
    void getUserDtoById_whenUserFound_thenReturnUser() {
        Integer userId = 1;
        User expectedUser = createOneUser(userId);
        UserDto expectedUserDto = UserMapper.toUserDto(expectedUser);
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(expectedUser));

        UserDto actualUserDto = userService.getUserDtoById(userId);

        assertEquals(expectedUserDto, actualUserDto);
        verify(userRepository).findUserById(userId);
    }

    @Test
    void getUserDtoById_whenUserFound_thenReturnUserDto() {
        Integer userId = 1;
        User expectedUser = createOneUser(userId);
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserById(userId);
        verify(userRepository).findUserById(argumentUserIdCaptor.capture());
        Integer userIdForDelete = argumentUserIdCaptor.getValue();

        assertEquals(userId, userIdForDelete);
        assertEquals(expectedUser, actualUser, "Пользователи не совпали!");
        verify(userRepository).findUserById(userId);
    }

    @Test
    void createUser_whenValidationIsOk_thenSave() {
        User userToSave = createOneUser(1);
        when(userRepository.save(userToSave)).thenReturn(userToSave);

        UserDto expectedUserDto = userService.createUser(UserMapper.toUserDto(userToSave));

        assertEquals(expectedUserDto, UserMapper.toUserDto(userToSave),"создал другого юзера");
        verify(userRepository).save(userToSave);
    }

    @Test
    void createUser_whenNotValid_thenNotSaved() {
        User userToSave = createOneUser(1);
        when(userRepository.save(userToSave)).thenThrow(ConstraintViolationException.class);

        assertThrows(ConstraintViolationException.class,
                () -> userService.createUser(UserMapper.toUserDto(userToSave)));
    }

    @Test
    void updateUser_whenUserNotFound_thenNotSaved() {
        Integer userId = 999;
        User user = createOneUser(userId);
        UserDto userDto = createFirstUserDto(userId);

        when(userRepository.findUserById(userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userDto));
        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUser_whenUserFound_Saved() {
        Integer userId = 1;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("oldname");
        oldUser.setEmail("old@mail.ru");

        UserDto newUserDto = new UserDto();
        newUserDto.setId(userId);
        newUserDto.setName("new");
        newUserDto.setEmail("new@mail.ru");
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(oldUser));
        when(userRepository.save(UserMapper.toUser(newUserDto)))
                .thenReturn(UserMapper.toUser(newUserDto));

        UserDto expectedUser = userService.updateUser(newUserDto);

        verify(userRepository).save(argumentUserCaptor.capture());
        User savedUser = argumentUserCaptor.getValue();

        assertEquals("new", expectedUser.getName());
        assertEquals("new@mail.ru", expectedUser.getEmail());
    }

    @Test
    void updateUser_whenSomeFiedNullFound_Saved() {
        Integer userId = 1;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("oldname");
        oldUser.setEmail("old@mail.ru");

        UserDto newUserDto = new UserDto();
        newUserDto.setId(userId);
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(oldUser));
        when(userRepository.save(oldUser))
                .thenReturn(oldUser);

        UserDto expectedUser = userService.updateUser(newUserDto);

        verify(userRepository).save(argumentUserCaptor.capture());
        User savedUser = argumentUserCaptor.getValue();

        assertEquals(savedUser, oldUser);
        assertEquals(expectedUser, UserMapper.toUserDto(oldUser));
    }

    @Test
    void updateUser_whenConstraintEmailValidation_NotUpdated() {
        Integer userId = 1;
        User oldUser = createOneUser(userId);
        UserDto newUserDto = createSecondUserDto(userId);

        doThrow(ConstraintViolationException.class)
                .when(userRepository).save(UserMapper.toUser(newUserDto));

        assertThrows(ConstraintViolationException.class,
                () -> userService.createUser(newUserDto));
    }

    @Test
    void getUserList_whenTwoUsers() {
        User user1 = createOneUser(1);
        User user2 = createSecondUser(2);
        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserDto> actualList = userService.getUserList();

        assertEquals(List.of(user1, user2),
                actualList.stream().map(UserMapper::toUser).collect(Collectors.toList()));
    }

    @Test
    void getUserList_whenNoOneUsers() {
        List<User> actualList = new ArrayList<>();
        when(userRepository.findAll())
                .thenReturn(actualList);

        List<UserDto> expectedList = userService.getUserList();

        assertEquals(actualList, expectedList.stream().map(UserMapper::toUser).collect(Collectors.toList()));
    }

    @Test
    void deleteUser_whenUserNotFound_notDeleted() {
        Integer userId = 999;
        User user = createOneUser(userId);

        when(userRepository.findUserById(userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));
        verify(userRepository, never()).delete(user);
    }

    @Test
    void deleteUser_whenConstraintValidation_notDeleted() {
        Integer userId = 1;
        User user = createOneUser(userId);
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(user));
        doThrow(ConstraintViolationException.class)
                    .when(userRepository).delete(user);


        assertThrows(ConstraintViolationException.class,
                    () -> userService.deleteUser(userId));
    }

    @Test
    void deleteUser_whenUserFound_Deleted() {
        Integer userId = 1;
        User user = createOneUser(userId);
        when(userRepository.findUserById(userId))
                .thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(userId);
        verify(userRepository).delete(argumentUserCaptor.capture());
        User userForDelete = argumentUserCaptor.getValue();


        assertEquals(user, userForDelete);
        verify(userRepository).delete(user);
    }

    private User createOneUser(Integer userId) {
        User user = new User();
        user.setId(userId);
        user.setName("юзер 1");
        user.setEmail("firstuser@mail.ru");
        return user;
    }

    private UserDto createFirstUserDto(Integer userId) {
        UserDto user = new UserDto();
        user.setId(userId);
        user.setName("юзер 1");
        user.setEmail("firstuser@mail.ru");
        return user;
    }

    private UserDto createSecondUserDto(Integer userId) {
        UserDto user = new UserDto();
        user.setId(userId);
        user.setName("юзер 2");
        return user;
    }

    private User createSecondUser(Integer userId) {
        User user = new User();
        user.setId(userId);
        user.setName("юзер 2");
        user.setEmail("seconduser@mail.ru");
        return user;
    }
}