package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    Long id;

    @NotNull(message = "Название не указано")
    @NotBlank(message = "Название не может быть пустым")
    String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    String description;
    LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность фильма не может быть отрицательным")
    int duration;
}
