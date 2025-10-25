package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;
    private static final LocalDate FIRST_CINEMA = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAll() {
        log.info("Запрос списка всех фильмов (всего: {})", films.size());
        return films.values();
    }

    @Override
    public Film add(Film film) {
        validateHistoricalDate(film);
        film.setId(++currentId);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            log.error("Фильм с id={} не найден", newFilm.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }
        validateHistoricalDate(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id={} успешно обновлён", newFilm.getId());
        return newFilm;
    }

    @Override
    public Film getById(Long id) {
        return films.get(id);
    }

    private void validateHistoricalDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_CINEMA)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

}
