package ru.yandex.practicum.filmorate.storage.user.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User createUser(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);
        log.debug("User saved to DB with id {}", id);
        return user;
    }

    @Override
    public User updateUser(User user) throws NotFoundException {
        getUserById(user.getId());
        String sql = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        log.debug("User updated in DB with id {}", user.getId());
        return user;
    }

    @Override
    public void deleteUser(Long id) throws NotFoundException {
        getUserById(id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        log.debug("User removed from DB with id {}", id);
    }

    @Override
    public List<User> getAllUsers() {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User getUserById(Long id) throws NotFoundException {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                WHERE id = ?
                """;
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        if (users.isEmpty()) {
            throw new NotFoundException(String.format("entity with id=%d does not exists", id));
        }
        return users.getFirst();
    }

}

