package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Builder
@Data
public class UserDto {
    private Integer id;
    @NotBlank
    private String name;
    @NotEmpty(message = "Не указан e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;
    private final String dtoVer = "1.0 спринт 13";
}
