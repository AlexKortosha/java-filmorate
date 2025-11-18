package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    public void createEvent(Event event) {
        eventStorage.save(event);
    }

    public List<Event> getUserFeed(Long userId) {
        User user = userStorage.getById(userId);

        if (user == null) {
            throw new NotFoundException("пользователь не найден");
        }

        return eventStorage.findByUserId(userId);
    }
}