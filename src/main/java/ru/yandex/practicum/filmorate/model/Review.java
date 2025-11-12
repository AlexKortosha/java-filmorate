package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    @JsonProperty("reviewId")
    @JsonAlias("reviewId")
    private Long id;

    @NotBlank(message = "Отзыв должен содержать текст")
    private String content;

    @JsonIgnore
    private User user;

    @JsonIgnore
    private Film film;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;

    private Integer useful = 0;
}
