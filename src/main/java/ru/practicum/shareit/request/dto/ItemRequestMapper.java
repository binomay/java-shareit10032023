package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest createDtoToRequest(ItemRequestCreateDto reqDto, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(reqDto.getDescription());
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    public static ItemRequestOutDto itemRequestToOutDto(ItemRequest itemRequest) {
        ItemRequestOutDto outDto = new ItemRequestOutDto();
        outDto.setId(itemRequest.getId());
        outDto.setDescription(itemRequest.getDescription());
        outDto.setCreated(itemRequest.getCreated());
        return outDto;
    }

    public static ItemReqDtoForResponse toItemReqDtoForResponse(ItemRequest itemRequest, List<Item> itemList) {
        ItemReqDtoForResponse itemReqDto = new ItemReqDtoForResponse();
        itemReqDto.setId(itemRequest.getId());
        itemReqDto.setDescription(itemRequest.getDescription());
        itemReqDto.setCreated(itemRequest.getCreated());
        itemReqDto.setItems(itemList.stream().map(ItemMapper:: itemToItemDtoForReq).collect(Collectors.toList()));
        return itemReqDto;
    }
}
