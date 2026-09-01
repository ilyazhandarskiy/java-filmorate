package ru.yandex.practicum.filmorate.storage.film.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String BASE_FILM_SELECT = """
            WITH film_likes AS (
                SELECT
                    ulf.film_id,
                    COUNT(*) AS likes_count
                FROM user_like_film ulf
                GROUP BY ulf.film_id
            )
            SELECT
                f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                ar.id AS age_rating_id,
                ar.name AS age_rating_name,
                COALESCE(fl.likes_count, 0) AS likes_count
            FROM film f
            JOIN age_rating ar
                ON ar.id = f.age_rating_id
            LEFT JOIN film_likes fl
                ON fl.film_id = f.id
            """;

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(BASE_FILM_SELECT + " ORDER BY f.id", filmRowMapper);
        fillGenres(films);
        return films;
    }

    @Override
    public Film getFilmById(Long id) throws NotFoundException {
        List<Film> films = jdbcTemplate.query(
                BASE_FILM_SELECT + " WHERE f.id = ?",
                filmRowMapper,
                id
        );
        if (films.isEmpty()) {
            throw new NotFoundException(String.format("entity with id=%d does not exists", id));
        }
        Film film = films.getFirst();
        fillGenres(List.of(film));
        return film;
    }

    @Override
    @Transactional
    public Film createFilm(Film film) {
        String sql = """
                INSERT INTO film (name, description, release_date, duration, age_rating_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        Long mpaId = film.getMpa().getId();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, mpaId);
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        saveFilmGenres(id, film.getGenres());
        log.debug("Film saved to DB with id {}", id);
        return getFilmById(id);
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        String sql = """
                UPDATE film
                SET name = ?, description = ?, release_date = ?, duration = ?, age_rating_id = ?
                WHERE id = ?
                """;
        long mpaId = film.getMpa().getId();
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId()
        );
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        saveFilmGenres(film.getId(), film.getGenres());
        log.debug("Film updated in DB with id {}", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    @Transactional
    public void deleteFilm(Long id) throws NotFoundException {
        getFilmById(id);
        jdbcTemplate.update("DELETE FROM film WHERE id = ?", id);
        log.debug("Film removed from DB with id {}", id);
    }

    private void saveFilmGenres(long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        Set<Long> uniqueGenreIds = genres.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        for (Long genreId : uniqueGenreIds) {
            jdbcTemplate.update(sql, filmId, genreId);
        }
    }

    private void fillGenres(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }
        Map<Long, Film> filmsById = new LinkedHashMap<>();
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>());
            filmsById.put(film.getId(), film);
        }

        String placeholders = String.join(",", Collections.nCopies(films.size(), "?"));
        String sql = """
                SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
                FROM film_genre fg
                JOIN genre g ON g.id = fg.genre_id
                WHERE fg.film_id IN (%s)
                ORDER BY g.id
                """.formatted(placeholders);
        Object[] args = films.stream().map(Film::getId).toArray();

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("genre_name"));
            Film film = filmsById.get(filmId);
            if (film != null) {
                film.getGenres().add(genre);
            }
            return null;
        }, args);

        for (Film film : films) {
            Set<Genre> sorted = film.getGenres().stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(sorted);
        }
    }
}
