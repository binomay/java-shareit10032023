package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> userMainMap = new LinkedHashMap<>();
    private final HashMap<String, Integer> userMap = new HashMap<>();

    @Override
    public Optional<User> getUserById(int userId) {
        return Optional.ofNullable(userMainMap.get(userId));
    }

    @Override
    public List<User> getUserList() {
        return new ArrayList<>(userMainMap.values());
    }

    @Override
    public User createUser(User user) {
        userMainMap.put(user.getId(), user);
        userMap.put(user.getEmail(), user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        //удалить старого юзера
        User oldUser = getUserById(user.getId()).get();
        deleteUser(oldUser);
        return createUser(user);
    }

    @Override
    public void deleteUser(User user) {
        userMainMap.remove(user.getId());
        userMap.remove(user.getEmail());
    }

    public Optional<Integer> getUserIdByEmail(String email) {
        return Optional.ofNullable(userMap.get(email));
    }

}

