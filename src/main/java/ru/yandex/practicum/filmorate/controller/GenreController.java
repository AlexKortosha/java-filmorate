package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.database.GenreDbStorage;


import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreDbStorage genreStorage;

    @GetMapping
    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable int id) {
        return genreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }

}
