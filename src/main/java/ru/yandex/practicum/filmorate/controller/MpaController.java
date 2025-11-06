package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<Collection<Mpa>> getAllMpa() {
        return ResponseEntity.ok(mpaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getMpa(@PathVariable int id) {
        return ResponseEntity.ok(mpaService.getById(id));
    }

}
