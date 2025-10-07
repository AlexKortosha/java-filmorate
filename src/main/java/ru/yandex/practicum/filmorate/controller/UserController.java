package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User newUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("email должен быть указан");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("email должен содержать символ:'@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;

    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
       if( newUser.getId() == null) {
           throw new ValidationException("id должен быть указан");
       }

        User existingUser = users.get(newUser.getId());
        if (existingUser == null) {
            throw new ValidationException("Пользователь с таким id не найден");
        }
        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (!newUser.getEmail().contains("@")) {
                throw new ValidationException("email должен содержать символ '@'");
            }
            boolean emailExists = users.values().stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(newUser.getEmail()));
            if (emailExists) {
                throw new ValidationException("Этот email уже используется");
            }
            existingUser.setEmail(newUser.getEmail());
        }

        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        if (newUser.getLogin() != null) {
            existingUser.setLogin(newUser.getLogin());
        }
        if (newUser.getBirthday() != null) {
            existingUser.setBirthday(newUser.getBirthday());
        }

        users.put(existingUser.getId(), existingUser);
        return  existingUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return  ++currentMaxId;
    }
}
