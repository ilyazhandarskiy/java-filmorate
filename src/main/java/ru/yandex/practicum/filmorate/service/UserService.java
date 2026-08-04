package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class UserService {
    private final UserStorage userStorage;
    //добавление в друзья, удаление из друзей, вывод списка общих друзей.
    // Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
    // То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.

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
        userStorage.updateUser(user);

        friend.getFriends().add(userId);
        userStorage.updateUser(friend);
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Идентификаторы пользователей не должны совпадать");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        userStorage.updateUser(user);

        friend.getFriends().remove(userId);
        userStorage.updateUser(friend);
    }

    public Collection<User> getFriendsByUserId(Long userId) {
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        return userStorage.getUserById(userId).getFriends().stream()
                .filter(id -> !id.equals(friendId))
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
