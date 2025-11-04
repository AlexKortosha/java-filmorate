package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {

    Collection<Genre> findAll();

    Optional<Genre> getById(int id);

    List<Genre> getGenresByIds(Set<Integer> genreIds);
}
