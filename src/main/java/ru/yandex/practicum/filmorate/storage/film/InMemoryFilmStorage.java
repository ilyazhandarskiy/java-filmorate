package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        checkIdExistence(id);
        return films.get(id);
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        checkIdExistence(film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        checkIdExistence(id);
        films.remove(id);
    }

    private void checkIdExistence(Long id) {
        if (!films.containsKey(id)) {
            log.warn("Фильм с id: {} не найден", id);
            throw new NotFoundException("Фильм с переданным id: " + id + " не найден");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
