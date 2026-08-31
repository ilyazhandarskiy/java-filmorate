package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FriendshipService {
    @Qualifier("jdbcFriendshipStorage")
    private final FriendshipStorage friendsStorage;
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public void addFriend(Long userId, Long friendId) {
        log.info("Adding friend: user {} -> friend {}", userId, friendId);
        if (userId == null || friendId == null) {
            log.warn("Attempt to add friend with null userId or friendId");
            return;
        }
        if (userId.equals(friendId)) {
            log.warn("User {} tried to add himself as friend", userId);
            return;
        }
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        log.debug("Validation passed, storing friendship");
        friendsStorage.addFriend(userId, friendId);
        log.info("Friendship between {} and {} successfully added", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Removing friend: user {} -> friend {}", userId, friendId);
        if (userId == null || friendId == null) {
            log.warn("Attempt to remove friend with null userId or friendId");
            return;
        }
        if (userId.equals(friendId)) {
            log.warn("User {} tried to remove himself from friends", userId);
            return;
        }
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        log.debug("Validation passed, removing friendship");
        friendsStorage.removeFriend(userId, friendId);
        log.info("Friendship between {} and {} successfully removed", userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        log.info("Request to get friends of user {}", userId);
        if (userId == null) {
            log.warn("getFriends called with null userId");
            return Collections.emptyList();
        }
        userStorage.getUserById(userId);
        List<User> friendsList = friendsStorage.getFriends(userId);
        log.info("User {} has {} friends", userId, friendsList.size());
        log.debug("Friends list: {}", friendsList);
        return friendsList.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(Long userAId, Long userBId) {
        log.info("Request to get common friends of users {} and {}", userAId, userBId);
        if (userAId == null || userBId == null) {
            log.warn("getCommonFriends called with null id(s)");
            return Collections.emptyList();
        }
        userStorage.getUserById(userAId);
        userStorage.getUserById(userBId);
        if (userAId.equals(userBId)) {
            log.debug("User {} requested common friends with himself - returning his friends", userAId);
            return getFriends(userAId);
        }
        List<User> commonUsers = friendsStorage.getCommonFriends(userAId, userBId);
        log.info("Found {} common friends between {} and {}", commonUsers.size(), userAId, userBId);
        log.debug("Common friends list: {}", commonUsers);
        return commonUsers.stream().map(userMapper::toDto).collect(Collectors.toList());
    }
}