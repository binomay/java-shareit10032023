package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositary.ItemRepository;
import ru.practicum.shareit.request.dto.ItemReqDtoForResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositary.ItemRequestRepositary;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRepository itemRepository;

    private final UserService userService;
    private final ItemRequestRepositary itemRequestRepositary;

    public ItemRequestServiceImpl(UserService userService, ItemRequestRepositary itemRequestRepositary,
                                  ItemRepository itemRepository) {
        this.userService = userService;
        this.itemRequestRepositary = itemRequestRepositary;
        this.itemRepository = itemRepository;
    }

    public ItemRequestOutDto createRequest(Integer userId, ItemRequestCreateDto itemRequestDto) {
        User requester = userService.getUserById(userId);
        ItemRequest itemRequest = ItemRequestMapper.createDtoToRequest(itemRequestDto, requester);
        return ItemRequestMapper.itemRequestToOutDto(itemRequestRepositary.save(itemRequest));
    }

    public ItemRequest getItemRequestById(Integer itemRequestId) {
        return itemRequestRepositary.findById(itemRequestId).orElseThrow(
                () -> {
                    String msg = "Не нашел request с Id = " + itemRequestId;
                    log.warn(msg);
                    throw new ResourceNotFoundException(msg);
                }
        );
    }

    @Override
    public List<ItemReqDtoForResponse> getRequestsByUser(Integer userId) {
        User user = userService.getUserById(userId);
        List<ItemRequest> itemReqList = itemRequestRepositary.findAllByRequesterOrderById(user);
        List<Item> itemsList = itemRepository.findByRequestInOrderById(itemReqList);
        return getSmartRequestDtoList(itemReqList, itemsList);
    }

    @Override
    public List<ItemReqDtoForResponse> getAll(Integer userId, Integer from, Integer size) {
        Sort sortByCreatedDesc = Sort.by("created").descending();
        List<ItemRequest> itemRequestList = itemRequestRepositary.findAll(userId, PageRequest.of(from, size, sortByCreatedDesc));
        List<Item> itemsList = itemRepository.findByRequestInOrderById(itemRequestList);
        return getSmartRequestDtoList(itemRequestList, itemsList);
    }

    @Override
    public ItemReqDtoForResponse getItemRequestDtoById(Integer requestId, Integer userId) {
        User user = userService.getUserById(userId);
        ItemRequest itemRequest = getItemRequestById(requestId);
        List<Item> itemsList = itemRepository.findByRequestInOrderById(List.of(itemRequest));
        return getSmartRequestDtoList(List.of(itemRequest), itemsList).get(0);
    }

    private List<ItemReqDtoForResponse> getSmartRequestDtoList(List<ItemRequest> itemReqList, List<Item>  itemsList) {
        Map<Integer, List<Item>> itemByItemReqId = getMapOfItems(itemsList);
        List<ItemReqDtoForResponse> outList = new ArrayList<>();
        for (ItemRequest itemRequest : itemReqList) {
            List<Item> tmpItemsList = itemByItemReqId.get(itemRequest.getId());
            if (tmpItemsList == null) {
                tmpItemsList = new ArrayList<>();
            }
            outList.add(ItemRequestMapper.toItemReqDtoForResponse(itemRequest, tmpItemsList));
        }
        return outList;
    }

    private Map<Integer, List<Item>> getMapOfItems(List<Item> itemList) {
        Map<Integer, List<Item>> itemByItemReqId = new HashMap<>();
        Integer reqId;
        List<Item> currItemList;
        for (Item item : itemList) {
            reqId = item.getRequest().getId();
            if (itemByItemReqId.containsKey(reqId)) {
                currItemList = itemByItemReqId.get(reqId);
            } else {
                currItemList = new ArrayList<>();
            }
            currItemList.add(item);
            itemByItemReqId.put(reqId, currItemList);
        }
        return itemByItemReqId;
    }
}

