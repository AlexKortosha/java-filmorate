package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<Film> findAll() {
        log.info("Запрос списка всех фильмов (всего: {})", films.size());
        return films.values();
    }

    @Override
    public Film add(Film film) {
        film.setId(++currentId);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            log.error("Фильм с id={} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с таким id не найден");
        }
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id={} успешно обновлён", newFilm.getId());
        return newFilm;
    }

    @Override
    public Film getById(Long id) {
        return films.get(id);
    }

}
