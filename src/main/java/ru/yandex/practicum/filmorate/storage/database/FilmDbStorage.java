package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.*, r.name AS rating_name
                FROM film f
                JOIN rating r ON f.rating_id = r.rating_id
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        films.forEach(this::loadGenresAndLikes);
        return films;
    }

    @Override
    public Film add(Film film) {
        String sql = """
                INSERT INTO film (name, description, release_date, duration, rating_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa() != null ? film.getMpa().getId() : 1);
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        updateFilmGenres(film);
        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE film SET name=?, description=?, release_date=?, duration=?, rating_id=?
                WHERE film_id=?
                """;

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : 1,
                film.getId());

        if (updated == 0)
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");

        updateFilmGenres(film);
        return getById(film.getId());
    }

    @Override
    public Film getById(Long id) {
        String sql = """
                SELECT f.*, r.name AS rating_name
                FROM film f
                JOIN rating r ON f.rating_id = r.rating_id
                WHERE f.film_id = ?
                """;
        Film film = jdbcTemplate.query(sql, this::mapRowToFilm, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        loadGenresAndLikes(film);
        return film;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        try {
            return Optional.of(getById(id));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("INSERT INTO film_like (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM film_like WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        // Загружаем все фильмы с MPA
        String sql = """
            SELECT f.*, r.name AS rating_name
            FROM film f
            JOIN rating r ON f.rating_id = r.rating_id
            """;
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        // Загружаем жанры и лайки для каждого фильма
        films.forEach(this::loadGenresAndLikes);

        // Сортируем по количеству лайков по убыванию и ограничиваем количеством
        return films.stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    // --- вспомогательные методы ---
    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        Mpa mpa = new Mpa(rs.getInt("rating_id"), rs.getString("rating_name"));
        film.setMpa(mpa);
        return film;
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }

    private void loadGenresAndLikes(Film film) {

        String sqlGenres = """
        SELECT g.genre_id, g.name
        FROM genre g
        JOIN film_genre fg ON g.genre_id = fg.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.genre_id
        """;

        List<Genre> genreList = jdbcTemplate.query(
                sqlGenres,
                (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("name")),
                film.getId()
        );


        LinkedHashSet<Genre> genres = new LinkedHashSet<>(genreList);
        film.setGenres(genres);


        String sqlLikes = "SELECT user_id FROM film_like WHERE film_id = ?";
        Set<Long> likes = new HashSet<>(jdbcTemplate.queryForList(sqlLikes, Long.class, film.getId()));
        film.setLikes(likes);
    }
}
