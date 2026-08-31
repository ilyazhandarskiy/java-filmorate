package ru.yandex.practicum.filmorate.storage.filmLike;

public interface FilmLikeStorage {
    public void addLike(Long filmId, Long userId);

    public void removeLike(Long filmId, Long userId);

    public int getFilmLikesCount(Long filmId);
}
