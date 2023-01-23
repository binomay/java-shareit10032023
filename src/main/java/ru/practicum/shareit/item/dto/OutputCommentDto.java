package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutputCommentDto {
    private Integer id;
    private String text;
    private Integer itemId;
    private String authorName;
    private LocalDateTime created;
}
