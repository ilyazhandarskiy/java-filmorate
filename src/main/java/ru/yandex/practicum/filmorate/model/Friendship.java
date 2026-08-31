package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long id;
    private Long userId;
    private Long friendId;
    private LocalDateTime createdAt;
}
