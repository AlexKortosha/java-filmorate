package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    Collection<User> findAll();

    User add(User user);

    User update(User user);

    User getById(Long id);

    Optional<User> findUserById(int id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getUserFriends(Long userId);

    List<User> getCommonFriends(Long userId1, Long userId2);
}
