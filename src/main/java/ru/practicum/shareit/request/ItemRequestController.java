package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemReqDtoForResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestOutDto createRequest(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                           @Valid @RequestBody ItemRequestCreateDto itemRequestDto) {
        return itemRequestService.createRequest(userId, itemRequestDto);
    }

    //получить список своих запросов вместе с данными об ответах на них.
    @GetMapping
    public List<ItemReqDtoForResponse> getRequestsByUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemRequestService.getRequestsByUser(userId);
    }

    @GetMapping("/{requestId}")
    public ItemReqDtoForResponse getById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                         @PathVariable Integer requestId) {
        return itemRequestService.getItemRequestDtoById(requestId, userId);
    }

    @GetMapping("/all")
    public List<ItemReqDtoForResponse> getAll(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                               @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemRequestService.getAll(userId, from, size);
    }

}
