package ru.yandex.practicum.filmorate.storage.mpa.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Primary
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM age_rating ORDER BY id",
                mpaRowMapper
        );
    }

    @Override
    public Mpa getById(Long id) throws NotFoundException {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT id, name FROM age_rating WHERE id = ?",
                mpaRowMapper,
                id
        );
        if (ratings.isEmpty()) {
            throw new NotFoundException(String.format("MPA with id=%d not found", id));
        }
        return ratings.getFirst();
    }
}