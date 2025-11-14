package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void addEvent(Long userId, String eventType, String operation, Long entityId) {
        String sql =
                "INSERT INTO event (timestamp, user_id, event_type, operation, entity_id) " +
                        "VALUES (?, ?, ?, ?, ?)";

        jdbc.update(sql,
                System.currentTimeMillis(),
                userId,
                eventType,
                operation,
                entityId
        );
    }

    @Override
    public List<Event> getFeed(Long userId) {
        String sql =
                "SELECT * FROM event WHERE user_id = ? ORDER BY timestamp";

        return jdbc.query(sql, (rs, rowNum) ->
                Event.builder()
                        .eventId(rs.getLong("event_id"))
                        .timestamp(rs.getLong("timestamp"))
                        .userId(rs.getLong("user_id"))
                        .eventType(rs.getString("event_type"))
                        .operation(rs.getString("operation"))
                        .entityId(rs.getLong("entity_id"))
                        .build(), userId);
    }
}