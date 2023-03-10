package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private InputBookingDto inputBookingDto;
    private OutputBookingDto outputBookingDto;


    @BeforeEach
    void setUp() {
        inputBookingDto = new InputBookingDto();
        inputBookingDto.setStart(LocalDateTime.now());
        inputBookingDto.setEnd(LocalDateTime.now().plusDays(3));
        inputBookingDto.setItemId(1);
        inputBookingDto.setBookerId(3);
        outputBookingDto = new OutputBookingDto();
        outputBookingDto.setStart(inputBookingDto.getStart());
        outputBookingDto.setEnd(inputBookingDto.getEnd());
    }

    @SneakyThrows
    @Test
    void createBooking() {
        when(bookingService.createBooking(any())).thenReturn(outputBookingDto);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBookingDto))
                        .header("X-Sharer-User-Id", inputBookingDto.getBookerId()))
                .andExpect(status().isOk());

        verify(bookingService).createBooking(inputBookingDto);
    }

    @SneakyThrows
    @Test
    void approveBooking() {
        inputBookingDto.setId(10);
        when(bookingService.updateBooking(any(), any(), any())).thenReturn(outputBookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", inputBookingDto.getId())
                        .header("X-Sharer-User-Id", inputBookingDto.getBookerId())
                        .param("approved", String.valueOf(true)))
                .andExpect(status().isOk());

        verify(bookingService).updateBooking(inputBookingDto.getBookerId(), inputBookingDto.getId(), true);
    }

    @SneakyThrows
    @Test
    void getBooking() {
        inputBookingDto.setId(4);
        when(bookingService.getBookingById(any(), any()))
                .thenReturn(outputBookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", inputBookingDto.getId())
                        .header("X-Sharer-User-Id", inputBookingDto.getBookerId()))
                .andExpect(status().isOk());

        verify(bookingService).getBookingById(inputBookingDto.getId(), inputBookingDto.getBookerId());
    }

    @SneakyThrows
    @Test
    void getBookingsForUser() {
        List<OutputBookingDto> bookingList = getBookingList();
        when(bookingService.getUsersBooking(any(), any(), any(), any()))
                .thenReturn(bookingList);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", inputBookingDto.getBookerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bookingService).getUsersBooking(inputBookingDto.getBookerId(),
                "ALL", 0, 10);
    }

    @SneakyThrows
    @Test
    void getBookingsForOwner() {
        List<OutputBookingDto> bookingList = getBookingList();
        when(bookingService.getBookingsForOwner(any(), any(), any(), any()))
                .thenReturn(bookingList);

        mockMvc.perform(get("/bookings/owner", inputBookingDto.getBookerId())
                        .header("X-Sharer-User-Id", inputBookingDto.getBookerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bookingService).getBookingsForOwner(inputBookingDto.getBookerId(),
                "ALL", 0, 10);
    }

    private List<OutputBookingDto> getBookingList() {
        return List.of(outputBookingDto, new OutputBookingDto());
    }
}