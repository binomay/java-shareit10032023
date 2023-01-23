package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class InputCommentDto {
    private final Integer id;
    @Size(max = 50)
    @NotEmpty(message = "комментарий не может быть пустым")
    private final String text;
    private Integer itemId;
    private Integer authorId;
}