package ru.practicum.shareit.numerators;


public class ItemNumerator {
    private static int currentItemId;

    public static int getCurrenItemId() {
        return ++currentItemId;
    }
}

