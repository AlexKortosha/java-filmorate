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
    public Film newFilm(@Valid @RequestBody Film film) {
        validateHistoricalDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            log.error("Фильм с id={} не найден", newFilm.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }
        validateHistoricalDate(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id={} успешно обновлён", newFilm.getId());
        return newFilm;
    }

    private void validateHistoricalDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_CINEMA)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
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
