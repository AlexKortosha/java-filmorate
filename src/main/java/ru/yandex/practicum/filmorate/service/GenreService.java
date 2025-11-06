package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.database.GenreDbStorage;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;
    private final GenreStorage genreStorage;

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre getById(int id) {
        return genreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}
