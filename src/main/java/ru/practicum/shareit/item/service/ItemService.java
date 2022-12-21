package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto item);

    ItemDto updateItem(ItemDto item);

    ItemDto getItemById(Integer itemId);

    List<ItemDto> getUsersItems(Integer ownerId);

    List<ItemDto> getItemsByContextSearch(String context);
}
