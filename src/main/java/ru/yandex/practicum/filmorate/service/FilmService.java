package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserService userService;

    public Collection<Film> getAllFilms() {
        log.info("Запрос списка всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void deleteFilm(Long id) {
        filmStorage.deleteFilm(id);
    }

    public Film createFilm(Film film) {
        log.info("Добавление фильма: {}", film);
        validate(film);
        Film newFilm = filmStorage.createFilm(film);
        log.info("Фильм с id: {} добавлен", newFilm.getId());
        return newFilm;
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", film);
        validate(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм с id: {} обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    public void addLikeToFilmByUser(Long filmId, Long userId) {
        log.info("Добавление лайка к фильму id: {} от пользователя id: {}", filmId, userId);
        userService.getUserById(userId);
        filmStorage.getFilmById(filmId).getLikes().add(userId);
    }

    public void removeLikeFromFilmByUser(Long filmId, Long userId) {
        log.info("Удаление лайка из фильма id: {} от пользователя id: {}", filmId, userId);
        userService.getUserById(userId);
        filmStorage.getFilmById(filmId).getLikes().remove(userId);
    }

    public Collection<Film> getFilmsByPopular(int count) {
        log.info("Запрос списка популярных фильмов в количестве count: {}", count);
        if (count <= 0) {
            throw new ValidationException("Значение count должно быть положительным числом");
        }
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Long.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }


    private void validate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше " +
                    MIN_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }
    }

}
