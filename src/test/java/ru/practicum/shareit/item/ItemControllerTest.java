package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.InputCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDate;
import ru.practicum.shareit.item.dto.OutputCommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private ItemOutDtoWithDate outItemDto;
    private ItemDto itemDto;

    @BeforeEach
    public void beforeEach() {
        outItemDto = new ItemOutDtoWithDate();
        outItemDto.setId(1);
        outItemDto.setName("Тестовая вещь");
        outItemDto.setAvailable(true);
        Item item = new Item();
        item.setId(1);
        item.setName("Тестовая вещь");
        item.setAvailable(true);
        itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Тестовая вещь");
        itemDto.setAvailable(true);
        itemDto.setDescription("какое-то описание");
        itemDto.setOwner(1);
    }

    @SneakyThrows
    @Test
    void getItemById() {
        Integer itemId = 1;
        Integer userId = 1;

        when(itemService.getItemDtoById(any(), any()))
                .thenReturn(outItemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));

        verify(itemService).getItemDtoById(itemId, userId);
    }

    @SneakyThrows
    @Test
    void getUsersItems() {
        Integer ownerId = 1;
        Integer from = 0;
        String fromStr = Integer.toString(from);
        Integer size = 2;
        String sizeStr = Integer.toString(size);
        List<ItemOutDtoWithDate> listDdo = List.of(outItemDto);
        when(itemService.getUsersItems(ownerId, from, size)).thenReturn(listDdo);

        mockMvc.perform(get("/items")
                        .param("from", fromStr)
                        .param("size", sizeStr)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(itemService).getUsersItems(ownerId, from, size);
    }

    @SneakyThrows
    @Test
    void getItemsByContextSearch() {
        ItemDto item1 = new ItemDto();
        outItemDto.setId(1);
        outItemDto.setName("Тестовая вещь 1");
        outItemDto.setAvailable(true);
        ItemDto item2 = new ItemDto();
        outItemDto.setId(2);
        outItemDto.setName("Тестовая вещь 2");
        outItemDto.setAvailable(true);

        List<ItemDto> itemDtoList = List.of(item1, item2);

        when(itemService.getItemsByContextSearch(any(), any(), any()))
                .thenReturn(itemDtoList);

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(itemService).getItemsByContextSearch("test", 0, 10);
    }

    @SneakyThrows
    @Test
    void createItem() {
        when(itemService.createItem(any())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", itemDto.getOwner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));

        verify(itemService).createItem(itemDto);
    }

    @SneakyThrows
    @Test
    void updateItem() {
        when(itemService.updateItem(any())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", itemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", itemDto.getOwner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));

        verify(itemService).updateItem(itemDto);
    }

    @SneakyThrows
    @Test
    void addComment() {
        InputCommentDto inputCommentDto = new InputCommentDto();
        inputCommentDto.setItemId(1);
        inputCommentDto.setText("какой-то комментарий");
        inputCommentDto.setAuthorId(2);
        OutputCommentDto outputCommentDto = new OutputCommentDto();
        outputCommentDto.setText(inputCommentDto.getText());
        outputCommentDto.setItemId(inputCommentDto.getItemId());

        when(itemService.addCommentToItem(any())).thenReturn(outputCommentDto);

        mockMvc.perform(post("/items/{itemId}/comment", inputCommentDto.getItemId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCommentDto))
                        .header("X-Sharer-User-Id", inputCommentDto.getAuthorId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(inputCommentDto.getItemId()));

        verify(itemService).addCommentToItem(inputCommentDto);
    }
}