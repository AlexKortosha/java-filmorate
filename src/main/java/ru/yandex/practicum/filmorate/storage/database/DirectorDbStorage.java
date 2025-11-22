package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper = new DirectorRowMapper();

    @Override
    public Collection<Director> findAll() {
        String sql = "SELECT * FROM directors ORDER BY director_id";
        return jdbcTemplate.query(sql, directorRowMapper);
    }

    @Override
    public Director add(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, director.getName());
            return preparedStatement;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Добавлен директор {} с id={}", director.getName(), director.getId());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";

        jdbcTemplate.update(sql,
                director.getName(),
                director.getId());

        return director;
    }

    @Override
    public Optional<Director> getById(Long id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        List<Director> result = jdbcTemplate.query(sql, directorRowMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public void removeDirector(Long id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM directors WHERE director_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }

    @Override
    public List<Director> getDirectorByIds(Set<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return List.of();
        }

        String inSql = directorIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));
        String sql = "SELECT * FROM directors WHERE director_id IN (" + inSql + ") ORDER BY director_id";

        return jdbcTemplate.query(sql, directorRowMapper, directorIds.toArray());
    }
}
