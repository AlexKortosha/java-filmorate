package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;


import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<Collection<Genre>> findAll() {
        return ResponseEntity.ok(genreService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getById(@PathVariable int id) {
        return ResponseEntity.ok(genreService.getById(id));
    }

}
