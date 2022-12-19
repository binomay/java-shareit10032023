package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private Integer owner;
    /*
    request — если вещь была создана по запросу другого пользователя, то в этом
    поле будет храниться ссылка на соответствующий запрос.
     */
    private Integer request;
}

