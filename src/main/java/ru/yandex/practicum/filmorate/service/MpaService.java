package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.database.MpaDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public Collection<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }

    public Mpa getById(int id) {
        return mpaDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("рейтинг MPA с id " + id + " не найден"));
    }
}
