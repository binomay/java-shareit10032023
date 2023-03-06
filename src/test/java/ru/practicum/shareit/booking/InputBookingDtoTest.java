package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.BookingStatus;
import ru.practicum.shareit.booking.dto.InputBookingDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class InputBookingDtoTest {

    @Autowired
    private JacksonTester<InputBookingDto> json;

    @Test
    void testInputBookingDto() throws IOException {
        InputBookingDto dto = new InputBookingDto();
        dto.setId(1);
        dto.setItemId(2);
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        dto.setStart(startDate);
        dto.setEnd(endDate);
        dto.setStatus(BookingStatus.APPROVED.getName());
        dto.setBookerId(3);

        JsonContent<InputBookingDto> dtoJson = json.write(dto);

        assertThat(dtoJson).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(dtoJson).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
        assertThat(dtoJson).extractingJsonPathNumberValue("$.bookerId").isEqualTo(3);
        assertThat(dtoJson).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.APPROVED.getName());
        assertThat(dtoJson).extractingJsonPathStringValue("$.start", startDate.toString());
        assertThat(dtoJson).extractingJsonPathStringValue("$.end", endDate.toString());
    }


}