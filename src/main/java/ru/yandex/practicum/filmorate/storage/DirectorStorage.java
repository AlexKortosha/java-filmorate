package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {

    Collection<Director> findAll();

    Director add(Director director);

    Director update(Director director);

    Optional<Director> getById(Long id);

    void removeDirector(Long id);

    boolean existsById(Long id);
}

