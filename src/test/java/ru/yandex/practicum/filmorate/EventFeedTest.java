package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class, FilmController.class})
public class EventFeedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private FilmService filmService;

    @MockBean
    private UserService userService;

    @Test
    public void testGetUserFeedApi() throws Exception {
        Long userId = 1L;

        Event event1 = new Event();
        event1.setEventId(100L);
        event1.setUserId(userId);
        event1.setEventType("LIKE");
        event1.setOperation("ADD");
        event1.setEntityId(200L);
        event1.setTimestamp(System.currentTimeMillis());

        List<Event> userEvents = List.of(event1);

        when(eventService.getFeed(userId)).thenReturn(userEvents);

        mockMvc.perform(get("/users/{id}/feed", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("LIKE"))
                .andExpect(jsonPath("$[0].operation").value("ADD"))
                .andExpect(jsonPath("$[0].entityId").value(200))
                .andExpect(jsonPath("$[0].eventId").value(100));
    }

    @Test
    public void testEventServiceAddAndGetFeed() {
        EventService localEventService = new ru.yandex.practicum.filmorate.service.EventService(new InMemoryEventStorage());

        Long userId = 1L;
        localEventService.addEvent(userId, "FRIEND", "ADD", 300L);
        localEventService.addEvent(userId, "REVIEW", "UPDATE", 400L);

        List<Event> events = localEventService.getFeed(userId);
        assertThat(events).hasSize(2);
        assertThat(events).anyMatch(e -> e.getEventType().equals("FRIEND") && e.getOperation().equals("ADD"));
        assertThat(events).anyMatch(e -> e.getEventType().equals("REVIEW") && e.getOperation().equals("UPDATE"));
    }

    // Пример простого in-memory хранилища для тестирования EventService
    private static class InMemoryEventStorage implements ru.yandex.practicum.filmorate.storage.EventStorage {
        private final java.util.Map<Long, List<Event>> storage = new java.util.HashMap<>();
        private long nextEventId = 1;

        @Override
        public void addEvent(Long userId, String eventType, String operation, Long entityId) {
            Event event = new Event();
            event.setEventId(nextEventId++);
            event.setTimestamp(System.currentTimeMillis());
            event.setUserId(userId);
            event.setEventType(eventType);
            event.setOperation(operation);
            event.setEntityId(entityId);

            storage.computeIfAbsent(userId, k -> new java.util.ArrayList<>()).add(event);
        }

        @Override
        public List<Event> getFeed(Long userId) {
            return storage.getOrDefault(userId, List.of());
        }
    }
}