package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerValidationTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        UserStorage inMemoryStorage = new UserStorage() {

            private final Map<Long, User> users = new HashMap<>();
            private long nextId = 1;

            @Override
            public Collection<User> findAll() {
                return users.values();
            }

            @Override
            public User add(User user) {
                user.setId(nextId++);
                users.put(user.getId(), user);
                return user;
            }

            @Override
            public User update(User user) {
                users.put(user.getId(), user);
                return user;
            }

            @Override
            public User getById(Long id) {
                return users.get(id);
            }

            @Override
            public Optional<User> findUserById(int id) {
                return Optional.ofNullable(users.get((long) id));
            }

            @Override
            public List<User> getAllUsers() {
                return new ArrayList<>(users.values());
            }

            @Override
            public void addFriend(Long userId, Long friendId) {

            }

            @Override
            public void removeFriend(Long userId, Long friendId) {
                //тест
            }

            @Override
            public Set<Long> getUserFriends(Long userId) {
                return new HashSet<>();
            }

            @Override
            public List<User> getCommonFriends(Long userId1, Long userId2) {
                return new ArrayList<>();
            }
        };

        controller = new UserController(new UserService(inMemoryStorage));
    }

    @Test
    void validateUserEmailIsEmpty() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void validateUserEmailInvalid() {
        User user = new User();
        user.setEmail("invalidEmail");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void validateUserLoginIsEmpty() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void validateUserLoginWithSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("bad login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void validateUserBirthdayInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }
}