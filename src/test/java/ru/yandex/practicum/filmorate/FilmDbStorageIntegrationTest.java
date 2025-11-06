package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.database.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.database.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.database.MpaDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageIntegrationTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaDbStorage;

    @Test
    void testAddAndGetFilm() {
        Mpa mpa = mpaDbStorage.getById(1).orElseThrow();
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.add(film);

        Film fetchedFilm = filmStorage.getById(savedFilm.getId());
        assertThat(fetchedFilm.getName()).isEqualTo("Test Film");
        assertThat(fetchedFilm.getMpa().getId()).isEqualTo(mpa.getId());
        assertThat(fetchedFilm.getGenres()).isNotNull(); // проверяем, что жанры подгружены (могут быть пустыми)
    }

    @Test
    void testUpdateFilm() {
        Mpa mpa = mpaDbStorage.getById(1).orElseThrow();
        Film film = new Film();
        film.setName("Original Film");
        film.setDescription("Original description");
        film.setReleaseDate(LocalDate.of(2019, 1, 1));
        film.setDuration(90);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.add(film);
        savedFilm.setName("Updated Film");
        savedFilm.setDuration(100);
        filmStorage.update(savedFilm);

        Film updatedFilm = filmStorage.getById(savedFilm.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDuration()).isEqualTo(100);
        assertThat(updatedFilm.getGenres()).isNotNull(); // проверяем наличие жанров
    }
}