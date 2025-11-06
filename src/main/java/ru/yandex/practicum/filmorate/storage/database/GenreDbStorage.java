package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper = new GenreRowMapper();

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genre ORDER BY genre_id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Optional<Genre> getById(int id) {
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        List<Genre> result = jdbcTemplate.query(sql, genreRowMapper, id);
        return result.stream().findFirst();
    }

    public void validateGenreIdsExist(Set<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        List<Genre> foundGenres = getGenresByIds(genreIds);
        Set<Integer> foundIds = foundGenres.stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Integer> nonExistentIds = genreIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());

        if (!nonExistentIds.isEmpty()) {
            throw new RuntimeException("Жанры с id " + nonExistentIds + " не найдены.");
        }
    }

    @Override
    public List<Genre> getGenresByIds(Set<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }
        String inSql = genreIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM genre WHERE genre_id IN (" + inSql + ") ORDER BY genre_id";
        return jdbcTemplate.query(sql, genreIds.toArray(), genreRowMapper);
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM genre WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

}
