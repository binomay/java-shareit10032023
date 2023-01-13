package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    void deleteItem(Item item);

    Optional<Item> getItemById(int itemId);

    List<Item> getItemList();

    List<Item> getUsersItems(Integer ownerId);

    List<Item> getItemsByContextSearch(String context);
}