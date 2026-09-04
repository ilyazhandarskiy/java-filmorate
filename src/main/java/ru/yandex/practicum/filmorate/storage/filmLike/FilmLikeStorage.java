package ru.yandex.practicum.filmorate.storage.filmLike;

public interface FilmLikeStorage {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}
