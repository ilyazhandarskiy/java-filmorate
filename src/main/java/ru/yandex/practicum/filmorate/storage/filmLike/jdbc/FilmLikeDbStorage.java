package ru.yandex.practicum.filmorate.storage.filmLike.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class FilmLikeDbStorage implements FilmLikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(Long filmId, Long userId) {
        Integer exists = jdbcTemplate.query(
                "SELECT * FROM user_like_film WHERE film_id = ? AND user_id = ?",
                rs -> rs.next() ? 1 : null,
                filmId,
                userId
        );
        if (exists != null) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO user_like_film (film_id, user_id, created_at) VALUES (?, ?, ?)",
                filmId,
                userId,
                Timestamp.from(Instant.now())
        );
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM user_like_film WHERE film_id = ? AND user_id = ?",
                filmId,
                userId
        );
    }

}
