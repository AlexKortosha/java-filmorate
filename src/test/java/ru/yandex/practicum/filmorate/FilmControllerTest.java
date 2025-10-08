package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.controller.FilmController;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldAddValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A film about dreams within dreams");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film created = filmController.newFilm(film);

        assertNotNull(created.getId());
        assertEquals("Inception", created.getName());
    }

    @Test
    void shouldThrowIfNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.newFilm(film));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }

    @Test
    void shouldThrowIfDescriptionTooLong() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.newFilm(film));
        assertTrue(ex.getMessage().contains("Максимальная длина описания"));
    }

    @Test
    void shouldThrowIfReleaseBeforeFirstCinema() {
        Film film = new Film();
        film.setName("Old Movie");
        film.setDescription("Silent film");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.newFilm(film));
        assertTrue(ex.getMessage().contains("дата релиза"));
    }

    @Test
    void shouldThrowIfDurationIsZeroOrNegative() {
        Film film = new Film();
        film.setName("Bad Film");
        film.setDescription("Bad duration");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(0);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.newFilm(film));
        assertTrue(ex.getMessage().contains("Продолжительность"));
    }

}
