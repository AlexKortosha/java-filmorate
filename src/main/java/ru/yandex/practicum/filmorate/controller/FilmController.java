package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    private static final LocalDate FIRST_CINEMA = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll0() {
        log.info("Запрос списка всех фильмов (всего: {})", films.size());
        return films.values();
    }

    @PostMapping
    public Film newFilm(@RequestBody Film film) {
        log.info("Попытка добавить новый фильм: {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FIRST_CINEMA)) {
            log.error("Ошибка валидации: дата релиза {} раньше {}", film.getReleaseDate(), FIRST_CINEMA);
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность {} недопустима", film.getDuration());
            throw new ValidationException("Продолжительность фльма должна быть положительным числом");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Попытка обновления фильма: {}", newFilm);

        if (newFilm.getId() == null) {
            log.error("Ошибка валидации: id не указан");
            throw new ValidationException("id должен быть указан");
        }
        Film existingFilm = films.get(newFilm.getId());
        if (existingFilm == null) {
            log.error("Фильм с id={} не найден", newFilm.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.error("Ошибка валидации: пустое название при обновлении");
                throw new ValidationException("Название не может быть пустым");
            }
            existingFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                log.error("Ошибка валидации: длина описания превышает 200 символов");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            existingFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(FIRST_CINEMA)) {
                log.error("Ошибка валидации: релизная дата {} раньше {}", newFilm.getReleaseDate(), FIRST_CINEMA);
                throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
            }
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() <= 0) {
            log.error("Ошибка валидации: такая продолжительность {} недопустима", newFilm.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        existingFilm.setDuration(newFilm.getDuration());

        films.put(existingFilm.getId(), existingFilm);
        log.info("Фильм с id={} успешно обновлён", existingFilm.getId());
        return existingFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return  ++currentMaxId;
    }


}
