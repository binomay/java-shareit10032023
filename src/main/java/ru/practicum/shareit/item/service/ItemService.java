package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.InputCommentDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDate;
import ru.practicum.shareit.item.dto.OutputCommentDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto item);

    ItemDto updateItem(ItemDto item);

    ItemOutDtoWithDate getItemDtoById(Integer itemId, Integer userId);

    Item getItemById(Integer itemId);

    List<ItemOutDtoWithDate> getUsersItems(Integer ownerId, Integer from, Integer size);

    List<ItemDto> getItemsByContextSearch(String context, Integer from, Integer size);

    OutputCommentDto addCommentToItem(InputCommentDto commentDto);
}
