package ru.practicum.shareit;

public enum BookingStatus {

    WAITING("WAITING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");
    private final String  name;

    BookingStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
