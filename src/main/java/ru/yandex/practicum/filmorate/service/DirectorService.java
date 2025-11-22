package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director addDirector(Director director) {
        validateDirector(director);
        log.info("Добавление директора: {}", director.getName());
        return directorStorage.add(director);
    }

    public Director updateDirector(Director director) {
        Long id = director.getId();

        if (!directorStorage.existsById(id)) {
            throw new NotFoundException("Директор с id " + id + " не найден.");
        }

        validateDirector(director);
        log.info("Обновление директора с id={}", id);
        return directorStorage.update(director);
    }

    public Director getById(Long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Директор с id " + id + " не найден."));
    }

    public void removeDirector(Long id) {
        if (!directorStorage.existsById(id)) {
            throw new NotFoundException("Директор с id " + id + " не найден.");
        }

        directorStorage.removeDirector(id);
    }

    private void validateDirector(Director director) {
        if (director.getName().isBlank()) {
            throw new ValidationException("Имя режиссера должно быть указано.");
        }
    }
}
