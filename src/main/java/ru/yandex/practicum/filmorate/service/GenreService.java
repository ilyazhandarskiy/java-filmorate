package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;
    private final GenreMapper genreMapper;

    public List<GenreDto> getAll() {
        log.debug("Request to get all genres");
        List<Genre> genres = genreStorage.getAll();
        log.info("Returning {} genres", genres.size());
        return genres.stream().map(genreMapper::toDto).collect(Collectors.toList());
    }

    public GenreDto getById(Long id) {
        log.debug("Request to get genre by id {}", id);
        Genre genre = genreStorage.getById(id);
        log.info("Found genre by id {}: {}", id, genre);
        return genreMapper.toDto(genre);
    }

}