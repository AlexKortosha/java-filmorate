package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {

    Collection<Director> findAll();

    Director add(Director director);

    Director update(Director director);

    Optional<Director> getById(Long id);

    void removeDirector(Long id);

    boolean existsById(Long id);

    List<Director> getDirectorByIds(Set<Long> directorIds);
}

