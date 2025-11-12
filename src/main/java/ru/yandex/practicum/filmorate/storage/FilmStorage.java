package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAll();

    Film add(Film film);

    Film update(Film film);

    Film getById(Long id);

    Optional<Film> findFilmById(Long id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getMostPopularFilms(int count);

    boolean existsById(Long id);
}
