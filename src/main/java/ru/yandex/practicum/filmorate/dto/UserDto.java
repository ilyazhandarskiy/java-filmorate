package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;

    @NotNull(message = "Электронная почта не указана")
    @NotBlank(message = "Электронная почта пользователя пуста")
    @Email(message = "Электронная почта указана некорректно")
    private String email;

    @NotNull(message = "Логин не указан")
    @NotBlank(message = "Логин пользователя пуст")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
