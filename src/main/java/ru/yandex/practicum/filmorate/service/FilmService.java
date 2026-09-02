package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmMapper filmMapper;
    private final GenreMapper genreMapper;

    @Qualifier("filmLikeDbStorage")
    private final FilmLikeStorage filmLikeStorage;

    private final MpaService mpaService;
    private final GenreService genreService;

    public Collection<FilmDto> getAllFilms() {
        log.info("Request to retrieve the list of all films");

        return filmStorage.getAllFilms().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(Long id) {
        log.info("Request to retrieve film with id={}", id);

        Film film = filmStorage.getFilmById(id);
        return filmMapper.toDto(film);
    }

    public void deleteFilm(Long id) {
        log.info("Deleting film with id={}", id);

        filmStorage.deleteFilm(id);
    }

    public FilmDto createFilm(FilmDto filmDto) {
        log.info("Creating film: {}", filmDto);

        Film film = filmMapper.toEntity(filmDto);
        checkMpaAndGenres(film);
        validate(film);

        Film newFilm = filmStorage.createFilm(film);
        log.info("Film created with id={}", newFilm.getId());

        return filmMapper.toDto(newFilm);
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        log.info("Updating film: {}", filmDto);

        Film film = filmMapper.toEntity(filmDto);
        filmStorage.getFilmById(film.getId());
        validate(film);
        checkMpaAndGenres(film);

        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Film with id={} has been updated", updatedFilm.getId());

        return filmMapper.toDto(updatedFilm);
    }

    public void addLikeToFilmByUser(Long filmId, Long userId) {
        log.info("Adding a like to film id={} from user id={}", filmId, userId);

        filmStorage.getFilmById(filmId);
        userService.getUserById(userId);
        filmLikeStorage.addLike(filmId, userId);
    }

    public void removeLikeFromFilmByUser(Long filmId, Long userId) {
        log.info("Removing a like from film id={} by user id={}", filmId, userId);

        filmStorage.getFilmById(filmId);
        userService.getUserById(userId);
        filmLikeStorage.removeLike(filmId, userId);
    }

    public Collection<FilmDto> getFilmsByPopular(int count) {
        log.info("Request to retrieve {} most popular films", count);

        if (count <= 0) {
            throw new ValidationException("The count value must be a positive number");
        }

        return filmStorage.getFilmsByPopular(count).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException(
                    "The release date must not be earlier than "
                            + MIN_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            );
        }
    }

    private void checkMpaAndGenres(Film film) {
        mpaService.getById(film.getMpa().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {

            List<Long> validGenreIds = genreService.getAll()
                    .stream()
                    .map(genreMapper::toEntity)
                    .map(Genre::getId)
                    .toList();

            List<Long> filmGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .toList();

            filmGenreIds.stream()
                    .filter(id -> !validGenreIds.contains(id))
                    .findFirst()
                    .ifPresent(id -> {
                        throw new NotFoundException("Genre with id=" + id + " Not found");
                    });
        }
    }
}