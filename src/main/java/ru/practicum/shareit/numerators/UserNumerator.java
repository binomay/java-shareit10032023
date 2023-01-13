package ru.practicum.shareit.numerators;

public class UserNumerator {
    private static int currentUserId;

    public static int getCurrentUserId() {
        return ++ currentUserId;
    }
}
