package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FilmMapper {

    public FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setMpa(film.getMpa());
        dto.setLikesCount(film.getLikesCount());

        Set<Genre> set = film.getGenres();
        if (set != null) {
            dto.setGenres(new LinkedHashSet<>(set));
        }

        return dto;
    }

    public Film toEntity(FilmDto dto) {
        if (dto == null) {
            return null;
        }

        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setMpa(dto.getMpa());
        film.setLikesCount(dto.getLikesCount());

        Set<Genre> set = dto.getGenres();
        if (set != null) {
            film.setGenres(new LinkedHashSet<>(set));
        }

        return film;
    }

}
