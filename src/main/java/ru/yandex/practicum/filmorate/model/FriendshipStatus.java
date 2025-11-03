package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public enum FriendshipStatus {
    CONFIRMED(1),
    NOT_CONFIRMED(2);
    private final int id;

    FriendshipStatus(int id) {
        this.id = id;
    }

}
