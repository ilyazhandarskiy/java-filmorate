package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
public class GenreMapper {

    public GenreDto toDto(Genre genre) {
        if (genre == null) {
            return null;
        } else {
            GenreDto genreDto = new GenreDto();
            genreDto.setId(genre.getId());
            genreDto.setName(genre.getName());
            return genreDto;
        }
    }

    public Genre toEntity(GenreDto genreDto) {
        if (genreDto == null) {
            return null;
        } else {
            Genre genre = new Genre();
            genre.setId(genreDto.getId());
            genre.setName(genreDto.getName());
            return genre;
        }
    }
}
