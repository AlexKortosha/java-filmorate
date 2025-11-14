package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;

    public void addEvent(Long userId, String eventType, String operation, Long entityId) {
        eventStorage.addEvent(userId, eventType, operation, entityId);
    }

    public List<Event> getFeed(Long userId) {
        return eventStorage.getFeed(userId);
    }
}