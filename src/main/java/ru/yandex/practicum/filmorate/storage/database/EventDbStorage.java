package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Event.EventType;
import ru.yandex.practicum.filmorate.model.Event.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Event event) {
        String sql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
    }

    @Override
    public List<Event> findByUserId(Long userId) {
        String sql = "SELECT event_id, timestamp, user_id, event_type, operation, entity_id " +
                "FROM events " +
                "WHERE user_id = ? " +
                "ORDER BY " +
                "CASE event_type " +
                "WHEN 'FRIEND' THEN 1 " +
                "WHEN 'REVIEW' THEN 2 " +
                "WHEN 'LIKE' THEN 3 " +
                "END, " +
                "timestamp ASC, event_id ASC";
        return jdbcTemplate.query(sql, new EventRowMapper(), userId);
    }

    private static class EventRowMapper implements RowMapper<Event> {
        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Event(
                    rs.getLong("event_id"),
                    rs.getLong("timestamp"),
                    rs.getLong("user_id"),
                    EventType.valueOf(rs.getString("event_type")),
                    Operation.valueOf(rs.getString("operation")),
                    rs.getLong("entity_id")
            );
        }
    }
}