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

    public void createEvent(Event event) {
        event.setTimestamp(System.currentTimeMillis());
        eventStorage.save(event);
    }

    public List<Event> getUserFeed(Long userId) {
        return eventStorage.findByUserId(userId);
    }
}