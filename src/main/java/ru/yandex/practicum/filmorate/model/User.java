package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    Long id;

    @NotNull(message = "Электронная почта не указана")
    @NotBlank(message = "Электронная почта пользователя пуста")
    @Email(message = "Электронная почта указана некорректно")
    String email;

    @NotNull(message = "Логин не указан")
    @NotBlank(message = "Логин пользователя пуст")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    String login;

    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

    private final Set<Long> friends = new HashSet<>();
}
