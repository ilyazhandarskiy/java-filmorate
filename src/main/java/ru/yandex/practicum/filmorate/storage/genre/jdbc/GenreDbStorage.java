package ru.yandex.practicum.filmorate.storage.genre.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> getAll() {
        return this.jdbcTemplate.query("SELECT id, name FROM genre ORDER BY id", this.genreRowMapper);
    }

    public Genre getById(Long id) throws NotFoundException {
        List<Genre> genres = this.jdbcTemplate.query(
                "SELECT id, name FROM genre WHERE id = ?",
                this.genreRowMapper,
                id
        );
        if (genres.isEmpty()) {
            throw new NotFoundException(String.format("Genre with id=%d not found", id));
        } else {
            return genres.getFirst();
        }
    }
}
