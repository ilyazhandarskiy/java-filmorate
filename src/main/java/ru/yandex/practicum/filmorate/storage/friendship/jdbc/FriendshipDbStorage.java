package ru.yandex.practicum.filmorate.storage.friendship.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.jdbc.UserRowMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;


    @Override
    public void addFriend(Long userId, Long friendId) throws NotFoundException {
        Integer exists = jdbcTemplate.query(
                """
                        SELECT id
                        FROM friendship
                        WHERE user_id = ? AND friend_id = ?
                        """,
                rs -> rs.next() ? 1 : null,
                userId,
                friendId
        );
        if (exists != null) {
            return;
        }
        jdbcTemplate.update(
                """
                        INSERT INTO friendship (user_id, friend_id, created_at)
                        VALUES (?, ?, ?)
                        """,
                userId,
                friendId,
                Timestamp.from(Instant.now())
        );
    }

    @Override
    public void removeFriend(Long userId, Long friendId) throws NotFoundException {
        jdbcTemplate.update(
                """
                        DELETE FROM friendship
                        WHERE user_id = ? AND  friend_id = ?
                        """,
                userId,
                friendId
        );
    }

    @Override
    public List<User> getFriends(Long userId) {
        return jdbcTemplate.query(
                """
                        SELECT u.id, u.email, u.login, u.name, u.birthday
                        FROM friendship fr
                        JOIN users u ON u.ID = fr.FRIEND_ID
                        WHERE fr.USER_ID = ?
                        ORDER BY u.id
                        """,
                userRowMapper,
                userId
        );
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        return jdbcTemplate.query(
                """
                        SELECT u.id,
                               u.email,
                               u.login,
                               u.name,
                               u.birthday
                        FROM friendship f1
                        JOIN friendship f2
                          ON f1.friend_id = f2.friend_id
                        JOIN users u
                          ON u.id = f1.friend_id
                        WHERE f1.user_id = ?
                          AND f2.user_id = ?
                        ORDER BY u.id;
                        """,
                userRowMapper,
                userId,
                friendId
        );
    }
}
