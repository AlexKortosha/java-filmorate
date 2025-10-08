package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос списка всех пользователей (всего: {})", users.size());
        return users.values();
    }

    @PostMapping
    public User newUser(@RequestBody User user) {
        log.info("Попытка добавить нового пользователя: {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: email пустой");
            throw new ValidationException("email должен быть указан");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: email '{}' не содержит '@'", user.getEmail());
            throw new ValidationException("email должен содержать символ:'@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин '{}' недопустим", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не указано, установлено как логин: {}", user.getName());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения {} в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен с id={}", user.getId());
        return user;

    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Попытка обновить пользователя: {}", newUser);

       if( newUser.getId() == null) {
           log.error("Ошибка валидации: id не указан");
           throw new ValidationException("id должен быть указан");
       }

        User existingUser = users.get(newUser.getId());
        if (existingUser == null) {
            log.error("Ошибка: пользователь с id={} не найден", newUser.getId());
            throw new ValidationException("Пользователь с таким id не найден");
        }
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().isBlank()) {
                log.error("Ошибка валидации: email пустой");
                throw new ValidationException("email должен быть указан");
            }
            if (!newUser.getEmail().contains("@")) {
                log.error("Ошибка валидации: email '{}' не содержит '@'", newUser.getEmail());
                throw new ValidationException("email должен содержать символ '@'");
            }
            boolean emailExists = users.values().stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(newUser.getEmail()));
            if (emailExists) {
                log.error("Ошибка валидации: email '{}' уже используется другим пользователем", newUser.getEmail());
                throw new ValidationException("Этот email уже используется");
            }
            existingUser.setEmail(newUser.getEmail());
        }

        if (newUser.getName() != null) {
            if (newUser.getName().isBlank()) {
                log.warn("Имя пользователя пустое, установлено как логин: {}", existingUser.getLogin());
                existingUser.setName(existingUser.getLogin());
            } else {
                existingUser.setName(newUser.getName());
            }
        }
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                log.error("Ошибка валидации: логин '{}' недопустим", newUser.getLogin());
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            existingUser.setLogin(newUser.getLogin());
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.error("Ошибка валидации: дата рождения {} в будущем", newUser.getBirthday());
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(newUser.getBirthday());
        }

        users.put(existingUser.getId(), existingUser);
        log.info("Пользователь успешно обновлён: {}", existingUser);

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
