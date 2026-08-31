package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public Collection<UserDto> getAllUsers() {
        log.info("Запрос списка пользователей");
        return userStorage.getAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        return userMapper.toDto(userStorage.getUserById(userId));
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteUser(userId);
    }

    public UserDto createUser(UserDto UserDTO) {
        User user = userMapper.toEntity(UserDTO);
        log.info("Создание пользователя {}", user);
        autofillEmptyFields(user);
        User createdUser = userStorage.createUser(user);
        log.info("Пользователь создан с id:{}", createdUser.getId());
        return userMapper.toDto(createdUser);
    }

    public UserDto updateUser(UserDto userDto) {
        log.info("Обновление пользователя: {}", userDto);
        User user = userMapper.toEntity(userDto);
        autofillEmptyFields(user);
        User updatedUser = userStorage.updateUser(user);
        log.info("Пользователь с id={} обновлён", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }


    private void autofillEmptyFields(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Передано пустое имя для отображения, копируется значение логина {} в данное поле",
                    user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
