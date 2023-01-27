package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.InputCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDate;
import ru.practicum.shareit.item.dto.OutputCommentDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemOutDtoWithDate getItemById(@PathVariable Integer itemId,
                                          @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        return itemService.getItemDtoById(itemId, userId);
    }

    @GetMapping
    public List<ItemOutDtoWithDate> getUsersItems(@RequestHeader(value = "X-Sharer-User-Id") Integer ownerId) {
        return itemService.getUsersItems(ownerId);

    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByContextSearch(@RequestParam("text") String context) {
        return itemService.getItemsByContextSearch(context);
    }

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        itemDto.setOwner(userId);
        return itemService.createItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @PathVariable Integer itemId,
                              @RequestHeader(value = "X-Sharer-User-Id") Integer ownerId) {
        itemDto.setOwner(ownerId);
        itemDto.setId(itemId);
        return itemService.updateItem(itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public OutputCommentDto addComment(@RequestBody @Valid InputCommentDto commentDto,
                                       @PathVariable Integer itemId,
                                       @RequestHeader(value = "X-Sharer-User-Id") Integer authorId) {
        commentDto.setItemId(itemId);
        commentDto.setAuthorId(authorId);
        return itemService.addCommentToItem(commentDto);
    }

}

