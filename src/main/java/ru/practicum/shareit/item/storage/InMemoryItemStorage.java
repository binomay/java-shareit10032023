package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {

    private HashMap<Integer, Item> itemList = new HashMap<>();
    private HashMap<Integer, HashSet<Integer>> usersItems = new HashMap<>();

    @Override
    public Item createItem(Item item) {
        itemList.put(item.getId(), item);
        if (usersItems.containsKey(item.getOwner())) {
            usersItems.get(item.getOwner()).add(item.getId());
        } else {
            HashSet<Integer> tmpSet = new HashSet<>();
            tmpSet.add(item.getId());
            usersItems.put(item.getOwner(), tmpSet);
        }
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        itemList.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteItem(Item item) {
        itemList.remove(item.getId());
    }

    @Override
    public Optional<Item> getItemById(int itemId) {
        return Optional.ofNullable(itemList.get(itemId));
    }

    @Override
    public List<Item> getItemList() {
        return new ArrayList<>(itemList.values());
    }

    @Override
    public List<Item> getUsersItems(Integer ownerId) {
        return usersItems.get(ownerId).stream().map(x -> itemList.get(x)).collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsByContextSearch(String context) {
        ArrayList<Item> itemsList = new ArrayList<>();
        String upperContext = context.toUpperCase();
        for (Item item: itemList.values()) {
            if ((item.getDescription().toUpperCase().contains(upperContext) ||
                    item.getName().toUpperCase().contains(upperContext)) && (item.getAvailable())) {
                itemsList.add(item);
            }
        }
        return itemsList;
    }

}

