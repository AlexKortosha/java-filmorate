package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.database.MpaDbStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorStorage directorStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        validateMpa(film);
        validateGenres(film);
        validateDirector(film);
        log.info("Добавление фильма: {}", film.getName());
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }
        validateFilm(film);
        validateMpa(film);
        validateGenres(film);
        validateDirector(film);
        log.info("Обновление фильма с id={}", film.getId());
        return filmStorage.update(film);
    }

    public Film getFilm(Long id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        return film;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        User user = userStorage.getById(userId);

        if (film == null || user == null) {
            throw new NotFoundException("Фильм или пользователь не найден");
        }

        filmStorage.addLike(filmId.intValue(), userId.intValue());

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        User user = userStorage.getById(userId);

        if (film == null || user == null) {
            throw new NotFoundException("Фильм или пользователь не найден");
        }

        filmStorage.removeLike(filmId.intValue(), userId.intValue());
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        return filmStorage.getMostPopularFilms(count, genreId, year);
    }

    public List<Film> getRecommendationFilms(Long userId) {
        return filmStorage.getRecommendationFilms(userId);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        userStorage.getById((long) userId);
        userStorage.getById((long) friendId);

        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getDirectorFilmsByLikes(Long directorId) {
        if (!directorStorage.existsById(directorId)) {
            throw new NotFoundException("Директор с id " + directorId + " не найден.");
        }

        return filmStorage.getDirectorFilmsByLikes(directorId);
    }

    public List<Film> getDirectorFilmsByYears(Long directorId) {
        if (!directorStorage.existsById(directorId)) {
            throw new NotFoundException("Директор с id " + directorId + " не найден.");
        }

        return filmStorage.getDirectorFilmsByYears(directorId);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void validateMpa(Film film) {
        if (film.getMpa() == null) {
            throw new ValidationException("MPA должен быть указан");
        }

        if (!mpaDbStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден.");
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        List<Genre> foundGenres = genreDbStorage.getGenresByIds(genreIds); //Все жанры

        Set<Integer> foundIds = foundGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Integer> missingIds = genreIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Жанры с id " + missingIds + " не найдены.");
        }
    }

    private void validateDirector(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        Set<Long> directorIds = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        List<Director> foundDirectors = directorStorage.getDirectorByIds(directorIds);

        Set<Long> foundIds = foundDirectors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = directorIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Директора с id " + missingIds + " не найдены.");
        }
    }
}
