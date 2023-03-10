package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDtoForReq;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemReqDtoForResponse {
    private Integer id;
    private String description;
    private LocalDateTime created;
    private List<ItemDtoForReq> items;
}
