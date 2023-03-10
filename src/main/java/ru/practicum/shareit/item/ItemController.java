package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.InputCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDate;
import ru.practicum.shareit.item.dto.OutputCommentDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
    public List<ItemOutDtoWithDate> getUsersItems(@RequestHeader(value = "X-Sharer-User-Id") Integer ownerId,
                                                  @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                  @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemService.getUsersItems(ownerId, from, size);

    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByContextSearch(@RequestParam("text") String context,
                                                 @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemService.getItemsByContextSearch(context, from, size);
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

