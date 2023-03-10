package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.dto.ItemReqDtoForResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {

    ItemRequestOutDto createRequest(Integer userId, ItemRequestCreateDto itemRequestDto);

    ItemRequest getItemRequestById(Integer itemRequestId);

    List<ItemReqDtoForResponse> getRequestsByUser(Integer userId);

    List<ItemReqDtoForResponse> getAll(Integer userId, Integer from, Integer size);

    ItemReqDtoForResponse getItemRequestDtoById(Integer requestId, Integer userId);
}