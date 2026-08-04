package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User getUserById(Long id) {
        checkIdExistence(id);
        return users.get(id);
    }

    @Override
    public User createUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkIdExistence(user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        checkIdExistence(id);
        //удаляем из друзей у всех пользователей
        users.values().stream()
                .map(User::getFriends)
                .forEach(set -> set.remove(id));
        users.remove(id);
    }

    private void checkIdExistence(Long id) {
        if (!users.containsKey(id)) {
            log.warn("User с id: {} не найден", id);
            throw new NotFoundException("User с переданным id: " + id + " не найден");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
