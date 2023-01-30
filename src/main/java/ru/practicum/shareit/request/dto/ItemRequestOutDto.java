package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequestOutDto {
    private Integer id;
    private String description;
    private LocalDateTime created;
}
