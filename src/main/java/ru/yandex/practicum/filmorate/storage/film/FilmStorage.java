package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    //определены методы добавления, удаления и модификации объектов.
    Collection<Film> getAllFilms();

    Collection<Film> getFilmsByPopular(int count);

    Film getFilmById(Long id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Long id);

    }
