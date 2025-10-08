package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.controller.UserController;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldAddValidUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("user123");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 5, 20));

        User created = userController.newUser(user);

        assertNotNull(created.getId());
        assertEquals("test@mail.com", created.getEmail());
    }

    @Test
    void shouldThrowIfEmailBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("user123");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 5, 20));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.newUser(user));
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    void shouldThrowIfEmailWithoutAt() {
        User user = new User();
        user.setEmail("userexample.com");
        user.setLogin("user123");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 5, 20));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.newUser(user));
        assertTrue(ex.getMessage().contains("@"));
    }

    @Test
    void shouldThrowIfLoginContainsSpace() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("bad login");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 5, 20));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.newUser(user));
        assertTrue(ex.getMessage().contains("Логин"));
    }

    @Test
    void shouldSetNameAsLoginIfNameBlank() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login123");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 5, 20));

        User created = userController.newUser(user);
        assertEquals("login123", created.getName());
    }

    @Test
    void shouldThrowIfBirthdayInFuture() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("user123");
        user.setName("User");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.newUser(user));
        assertTrue(ex.getMessage().contains("Дата рождения"));
    }

    @Test
    void shouldThrowIfEmptyBody() {
        assertThrows(NullPointerException.class, () -> userController.newUser(null));
    }
}
