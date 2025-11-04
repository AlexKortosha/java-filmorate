package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM rating ORDER BY rating_id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public Optional<Mpa> getById(int id) {
        String sql = "SELECT * FROM rating WHERE rating_id = ?";
        List<Mpa> result = jdbcTemplate.query(sql, this::mapRowToMpa, id);
        return result.stream().findFirst();
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM rating WHERE rating_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public List<Mpa> getMpaByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String inClause = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = String.format("SELECT * FROM mpa_ratings WHERE id IN (%s) ORDER BY id", inClause);

        return jdbcTemplate.query(sql, ids.toArray(), this::mapRowToMpa);
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("rating_id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}
