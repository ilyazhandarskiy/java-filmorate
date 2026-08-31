package ru.yandex.practicum.filmorate.storage.friendship.inMemory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Component
@RequiredArgsConstructor
@Qualifier("inMemoryFriendshipStorage")
public class InMemoryFriendshipStorage implements FriendshipStorage {
    private final Map<Long, Set<Long>> friendsMap = new HashMap<>();
    private final UserStorage userStorage;

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return;
        }
        friendsMap.computeIfAbsent(userId, i -> new HashSet<>())
                .add(friendId);
        friendsMap.computeIfAbsent(friendId, i -> new HashSet<>())
                .add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return;
        }
        Set<Long> friendsA = friendsMap.get(userId);
        if (friendsA != null) {
            friendsA.remove(friendId);
            if (friendsA.isEmpty()) {
                friendsMap.remove(userId);
            }
        }
        Set<Long> friendsB = friendsMap.get(friendId);
        if (friendsB != null) {
            friendsB.remove(userId);
            if (friendsB.isEmpty()) {
                friendsMap.remove(friendId);
            }
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        return friendsMap.getOrDefault(userId, new HashSet<>())
                .stream()
                .map(userStorage::getUserById)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(Long userIdA, Long userIdB) {
        Set<Long> friendsA = friendsMap.getOrDefault(userIdA, Collections.emptySet());
        Set<Long> friendsB = friendsMap.getOrDefault(userIdB, Collections.emptySet());

        Set<Long> common = new HashSet<>(friendsA);
        common.retainAll(friendsB);
        return common.stream()
                .map(userStorage::getUserById)
                .toList();
    }
}
