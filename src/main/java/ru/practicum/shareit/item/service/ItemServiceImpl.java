package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.numerators.ItemNumerator;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserServiceImp userService;

    public ItemServiceImpl(ItemStorage itemStorage, UserServiceImp userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    @Override
    public ItemDto getItemById(Integer itemId) {
        Item item = itemStorage.getItemById(itemId).orElseThrow(
                () -> {
                    String msg = "Не нашел item с Id = " + itemId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getUsersItems(Integer ownerId) {
        return itemStorage.getUsersItems(ownerId).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByContextSearch(String context) {
        if (context.isEmpty()) {
            return new ArrayList<>();
        } else {
            return itemStorage.getItemsByContextSearch(context).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
    }

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        checkAvialabel(item);
        //проверить, что owner есть на самом деле
        UserDto ownerDto = userService.getUserById(item.getOwner());
        item.setId(ItemNumerator.getCurrenItemId());
        return ItemMapper.toItemDto(itemStorage.createItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item oldItem = ItemMapper.toItem(getItemById(item.getId()));
        //отредактировать вещь может только ее владелец
        checkOwner(item.getOwner(), oldItem.getOwner());
        //отредактировать можно только название, комментарий и доступность...
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        return ItemMapper.toItemDto(itemStorage.updateItem(oldItem));
    }

    private void checkAvialabel(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("При создании Item не может быть недоступным!");
        }
    }

    private void checkOwner(Integer newOwner, Integer oldOwner) {
        if (newOwner == null) {
            throw new RightsException("Не указан owner");
        } else if (!newOwner.equals(oldOwner)) {
            throw new RightsException("Редактировать вещь может только ее вдладелец");
        }
    }

}