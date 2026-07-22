package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Запрос списка пользователей");
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создание пользователя {}", user);
        user.setId(getNextId());
        autofillEmptyFields(user);
        users.put(user.getId(), user);
        log.info("Пользователь создан с id:{}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Обновление пользователя: {}", user);
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id:" + user.getId() + " не найден");
        }
        autofillEmptyFields(user);
        users.put(user.getId(), user);
        log.info("Пользователь с id={} обновлён", user.getId());
        return user;
    }

    private void autofillEmptyFields(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Передано пустое имя для отображения, копируется значение логина {} в данное поле",
                    user.getLogin());
            user.setName(user.getLogin());
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
