package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setOwner(item.getOwner().getId());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setRequest(item.getRequest());
        return itemDto;
    }

    public static ItemShortDto itemToItemShortDto(Item item) {
        ItemShortDto itemShortDto = new ItemShortDto();
        itemShortDto.setId(item.getId());
        itemShortDto.setName(item.getName());
        return itemShortDto;
    }
}
