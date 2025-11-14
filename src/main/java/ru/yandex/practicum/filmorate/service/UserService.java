package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserStorage userStorage;
    private final EventService eventService;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User addUser(User user) {
        validateUser(user);
        log.info("Добавление пользователя: {}", user.getLogin());
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        validateUserExists(user.getId());
        validateUser(user);
        log.info("Обновление пользователя: {}", user.getId());
        return userStorage.update(user);
    }

    public User getUser(Long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        return user;
    }

    public List<User> getAllUsers() {

        return userStorage.getAllUsers();
    }


    public void addFriend(Long id, Long friendId) {
        validateUserExists(id);
        validateUserExists(friendId);

        userStorage.addFriend(id, friendId);
        log.info("Пользователь {} добавил в друзья {}", id, friendId);

        eventService.addEvent(id, "FRIEND", "ADD", friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        validateUserExists(id);
        validateUserExists(friendId);

        userStorage.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей {}", id, friendId);

        eventService.addEvent(id, "FRIEND", "REMOVE", friendId);
    }

    public List<User> getFriends(Long id) {

        validateUserExists(id);
        Set<Long> friendIds = userStorage.getUserFriends(id);

        return friendIds.stream()
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        validateUserExists(id);
        validateUserExists(otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    private User getUserWithFriends(Long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User validateUserExists(Long userId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        return user;
    }
}
