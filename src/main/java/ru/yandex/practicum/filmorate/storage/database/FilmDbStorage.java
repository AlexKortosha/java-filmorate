package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
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

        List<Long> filmIds = films.stream().map(Film::getId).toList();
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String sqlGenres = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ") " +
                "ORDER BY g.genre_id";
        Map<Long, LinkedHashSet<Genre>> filmGenres = new HashMap<>();
        jdbcTemplate.query(sqlGenres, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
            filmGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        String sqlLikes = "SELECT film_id, user_id FROM film_like WHERE film_id IN (" + inSql + ")";
        Map<Long, Set<Long>> filmLikes = new HashMap<>();
        jdbcTemplate.query(sqlLikes, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(filmGenres.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }
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
    public List<Film> getMostPopularFilms(int count) {
        String sql = """
        SELECT f.*, r.name AS rating_name, COUNT(fl.user_id) AS likes_count
        FROM film f
        JOIN rating r ON f.rating_id = r.rating_id
        LEFT JOIN film_like fl ON f.film_id = fl.film_id
        GROUP BY f.film_id, r.rating_id, r.name, f.name, f.description, f.release_date, f.duration
        ORDER BY likes_count DESC
        LIMIT ?
        """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);

        if (films.isEmpty()) {
            return films;
        }

        List<Long> filmIds = films.stream().map(Film::getId).toList();
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String sqlGenres = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ") " +
                "ORDER BY g.genre_id";

        Map<Long, LinkedHashSet<Genre>> filmGenres = new HashMap<>();
        jdbcTemplate.query(sqlGenres, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
            filmGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(filmGenres.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

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
}
