package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper = new FilmRowMapper();

    @Override
    public Collection<Film> findAll() {
        String sqlFilm = """
                SELECT f.*, r.name AS rating_name
                FROM film f
                JOIN rating r ON f.rating_id = r.rating_id
                """;

        List<Film> films = jdbcTemplate.query(sqlFilm, filmRowMapper);

        if (films.isEmpty()) {
            return films;
        }

        loadGenresForFilms(films);

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
        Film film = jdbcTemplate.query(sql, filmRowMapper, id)
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
    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        String getMostPopularFilmsSql = """
                SELECT f.*,
                       r.name AS rating_name,
                       COUNT(fl.user_id) AS like_count
                FROM film f
                JOIN rating r ON f.rating_id = r.rating_id
                JOIN film_like fl ON f.film_id = fl.film_id
                LEFT JOIN film_genre fg ON f.film_id = fg.film_id
                WHERE (:genreId IS NULL OR fg.genre_id = :genreId)
                  AND (:year IS NULL OR EXTRACT(YEAR FROM f.release_date) = :year)
                GROUP BY f.film_id
                ORDER BY like_count DESC
                LIMIT :count;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count)
                .addValue("genreId", genreId)
                .addValue("year", year);

        List<Film> films = namedParameterJdbcTemplate.query(getMostPopularFilmsSql, params, filmRowMapper);
        loadGenresForFilms(films);

        return films;
    }

    @Override
    public List<Film> getRecommendationFilms(Long userId) {
        String getSimilarUserSql = """
                SELECT fl2.user_id
                FROM film_like fl1
                JOIN film_like fl2 ON (fl1.film_id = fl2.film_id)
                WHERE fl1.user_id = ?
                  AND fl2.user_id != ?
                GROUP BY fl2.user_id
                ORDER BY COUNT(*) DESC
                LIMIT 1
                """;

        long similarUserId;
        try {
            similarUserId = jdbcTemplate.queryForObject(getSimilarUserSql, Long.class, userId, userId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }

        String getRecommendationFilmsSql = """
                SELECT f.*,
                       r.name AS rating_name
                FROM film f
                JOIN rating r ON (f.rating_id = r.rating_id)
                WHERE film_id IN (
                    SELECT fl.film_id
                    FROM film_like fl
                    WHERE fl.user_id = ?
                        AND fl.film_id NOT IN (
                            SELECT film_id
                            FROM film_like
                            WHERE user_id = ?
                    )
                )
                """;

        List<Film> films = jdbcTemplate.query(getRecommendationFilmsSql, filmRowMapper, similarUserId, userId);
        loadGenresForFilms(films);

        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = """
            SELECT f.*, r.name AS rating_name
            FROM film f
            JOIN rating r ON f.rating_id = r.rating_id
            JOIN film_like fl1 ON f.film_id = fl1.film_id
            JOIN film_like fl2 ON f.film_id = fl2.film_id
            WHERE fl1.user_id = ? AND fl2.user_id = ?
            GROUP BY f.film_id
            ORDER BY COUNT(fl1.user_id) DESC
            """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, userId, friendId);
        loadGenresForFilms(films);

        return films;
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

        List<Genre> genreList = jdbcTemplate.query(sqlGenres,
                (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("name")),
                film.getId());

        film.setGenres(new LinkedHashSet<>(genreList));
    }

    private void loadGenresForFilms(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        if (filmIds.isEmpty()) {
            return;
        }

        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ") " +
                "ORDER BY g.genre_id";

        Map<Long, LinkedHashSet<Genre>> filmGenres = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
            filmGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(filmGenres.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }
    }
}
