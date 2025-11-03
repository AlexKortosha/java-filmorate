package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM FILM";
        return jdbcTemplate.query(sql, new FilmMapper());
    }

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO film (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Фильм добавлен с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE film SET name=?, description=?, release_date=?, duration=? WHERE id=?";
        int rows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        log.info("Фильм с id={} обновлён", film.getId());
        return film;
    }

    @Override
    public Film getById(Long id) {
        String sql = "SELECT * FROM film WHERE id=?";
        return jdbcTemplate.query(sql, new FilmMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }
}
