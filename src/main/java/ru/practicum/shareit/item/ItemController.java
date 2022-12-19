package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Integer itemId) {
        Item item = itemService.getItemById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @GetMapping
    public List<ItemDto> getUsersItems(@RequestHeader(value = "X-Sharer-User-Id") Integer ownerId) {
        List<Item> usersItems = itemService.getUsersItems(ownerId);
        return usersItems.stream().map(x -> ItemMapper.toItemDto(x)).collect(Collectors.toList());
    }

    @GetMapping("/search")
    //?text=аккУМУляторная
    public List<ItemDto> getItemsByContextSearch(@RequestParam("text") String context) {
        List<Item> itemsList = itemService.getItemsByContextSearch(context);
        return itemsList.stream().map(x -> ItemMapper.toItemDto(x)).collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        itemDto.setOwner(userId);
        Item item = itemService.createItem(ItemMapper.toItem(itemDto));
        return ItemMapper.toItemDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto, @PathVariable Integer itemId, @RequestHeader(value = "X-Sharer-User-Id") Integer ownerId) {
        itemDto.setOwner(ownerId);
        itemDto.setId(itemId);
        Item item = itemService.updateItem(ItemMapper.toItem(itemDto));
        return ItemMapper.toItemDto(item);
    }


}

