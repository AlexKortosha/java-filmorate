package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


@AllArgsConstructor
@Getter
public enum ReactionType {
    LIKE(1, "LIKE"),
    DISLIKE(2, "DISLIKE");

    private final int id;
    private final String description;

    public static ReactionType fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown reaction id: " + id));
    }
}
