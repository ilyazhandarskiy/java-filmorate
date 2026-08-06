package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAllUsers() {
        log.info("Запрос списка пользователей");
        return userStorage.getAllUsers();
    }

    public User getUserById(Long userId) {
        return userStorage.getUserById(userId);
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteUser(userId);
    }

    public User createUser(User user) {
        log.info("Создание пользователя {}", user);
        autofillEmptyFields(user);
        User createdUser = userStorage.createUser(user);
        log.info("Пользователь создан с id:{}", createdUser.getId());
        return createdUser;
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя: {}", user);
        autofillEmptyFields(user);
        User updatedUser = userStorage.updateUser(user);
        log.info("Пользователь с id={} обновлён", updatedUser.getId());
        return user;
    }

    public void addToFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Идентификаторы пользователей не должны совпадать");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Идентификаторы пользователей не должны совпадать");
        }
        userStorage.getUserById(userId).getFriends().remove(friendId);
        userStorage.getUserById(friendId).getFriends().remove(userId);
    }

    public Collection<User> getFriendsByUserId(Long userId) {
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        Set<Long> currentUserFriends = userStorage.getUserById(userId).getFriends();
        Set<Long> otherUserFriends = userStorage.getUserById(friendId).getFriends();
        return currentUserFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    private void autofillEmptyFields(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Передано пустое имя для отображения, копируется значение логина {} в данное поле",
                    user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
