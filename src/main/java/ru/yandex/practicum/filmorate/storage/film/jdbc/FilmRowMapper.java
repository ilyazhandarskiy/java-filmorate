package ru.yandex.practicum.filmorate.storage.film.jdbc;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Nullable
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setLikesCount(rs.getInt("likes_count"));
        film.setMpa(new Mpa(rs.getLong("age_rating_id"), rs.getString("age_rating_name")));
        film.setGenres(new HashSet<>());

        return film;
    }
}
