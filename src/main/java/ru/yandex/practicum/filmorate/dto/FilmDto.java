package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;

    @NotNull(message = "Название не указано")
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность фильма не может быть отрицательным")
    private int duration;

    private final Set<Long> likes = new HashSet<>();
    private final Set<Genre> genres = new HashSet<>();
    private Mpa mpa;
}
