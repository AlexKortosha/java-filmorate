package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAll();

    Film add(Film film);

    Film update(Film film);

    void delete(Long id);

    Film getById(Long id);

    Optional<Film> findFilmById(Long id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    boolean existsById(Long id);

    List<Film> getMostPopularFilms(int count, Integer genreId, Integer year);

    List<Film> getRecommendationFilms(Long userId);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getDirectorFilmsByLikes(Long directorId);

    List<Film> getDirectorFilmsByYears(Long directorId);

    List<Film> searchFilms(String query, boolean byTitle, boolean byDirector);
}
