package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;


public interface ItemService {
    Item createItem(Item item);

    Item updateItem(Item item);

    Item getItemById(Integer itemId);

    List<Item> getUsersItems(Integer ownerId);

    List<Item> getItemsByContextSearch(String context);
}
