package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemReqDtoForResponse;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService requestService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private Integer userId;
    private ItemReqDtoForResponse itemReqDtoForResponse = new ItemReqDtoForResponse();

    @BeforeEach
    void setUp() {
        userId = 1;
        itemReqDtoForResponse.setDescription("Очень плохой первый отзыв");
    }

    @SneakyThrows
    @Test
    void createRequest() {
        ItemRequestOutDto outDto = new ItemRequestOutDto();
        outDto.setDescription("Плохой отзыв");
        ItemRequestCreateDto inputDto = new ItemRequestCreateDto();
        inputDto.setDescription("Плохой отзыв");

        when(requestService.createRequest(any(), any()))
                .thenReturn(outDto);

        mockMvc.perform(post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))
                .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Плохой отзыв"));

                verify(requestService).createRequest(userId, inputDto);
    }

    @SneakyThrows
    @Test
    void getRequestsByUser() {
        List<ItemReqDtoForResponse> list = List.of(itemReqDtoForResponse);

        when(requestService.getRequestsByUser(any()))
                .thenReturn(list);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(requestService).getRequestsByUser(userId);
    }

    @SneakyThrows
    @Test
    void getById() {
        Integer reqId = 101;

        when(requestService.getItemRequestDtoById(any(), any()))
                .thenReturn(itemReqDtoForResponse);

        mockMvc.perform(get("/requests/{requestId}", reqId)
                .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(itemReqDtoForResponse.getDescription()));
    }

    @SneakyThrows
    @Test
    void getAll() {
        List<ItemReqDtoForResponse> list = List.of(itemReqDtoForResponse);

        when(requestService.getAll(any(), any(), any()))
                .thenReturn(list);

        mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(requestService).getAll(userId, 0, 10);
    }
}