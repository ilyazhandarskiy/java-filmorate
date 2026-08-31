package ru.yandex.practicum.filmorate.storage.filmLike.inMemory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InMemoryFilmLikeStorage implements FilmLikeStorage {
    private final Map<Long, Set<Long>> filmLikesByUsers = new HashMap<>();

    @Override
    public void addLike(Long filmId, Long userId) {
        filmLikesByUsers
                .computeIfAbsent(filmId, i -> new HashSet<>())
                .add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Set<Long> userIds = filmLikesByUsers.get(filmId);

        if (userIds == null) {
            return;
        }

        userIds.remove(userId);

        if (userIds.isEmpty()) {
            filmLikesByUsers.remove(filmId);
        }
    }

    @Override
    public int getFilmLikesCount(Long filmId) {
        return filmLikesByUsers.getOrDefault(filmId, Set.of()).size();
    }
}
