package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<Collection<Director>> findAll() {
        return ResponseEntity.ok(directorService.findAll());
    }

    @PostMapping
    public ResponseEntity<Director> addDirector(@Valid @RequestBody Director director) {
        Director created = directorService.addDirector(director);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody Director director) {
        return ResponseEntity.ok(directorService.updateDirector(director));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> getDirector(@PathVariable Long id) {
        return ResponseEntity.ok(directorService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeDirector(@PathVariable Long id) {
        directorService.removeDirector(id);
        return ResponseEntity.ok().build();
    }
}
