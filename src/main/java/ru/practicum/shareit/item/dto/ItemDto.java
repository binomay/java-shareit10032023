package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ItemDto {
    private Integer id;
    @NotEmpty(message = "Имя не может быть пустым!")
    private String name;
    @NotEmpty(message = "Описание не может быть пустым!")
    private String description;
    @NotNull(message = "Доступность должна быть указана")
    private Boolean available;
    private Integer owner;
    private Integer requestId;
}
