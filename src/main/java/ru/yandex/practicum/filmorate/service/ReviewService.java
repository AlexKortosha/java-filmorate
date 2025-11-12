package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review getReview(Long id) {
        if (!reviewStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        return reviewStorage.getReviewById(id);
    }

    public Collection<Review> getAllReviewsWithLimit(Integer count) {
        return reviewStorage.getAllReviewsWithLimit(count);
    }

    public Collection<Review> getReviewsByFilmWithLimit(Long filmId, Integer count) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        return reviewStorage.getReviewsByFilmWithLimit(filmId, count);
    }

    public Review createReview(Review review) {
        validateReview(review);

        if (!userStorage.existsById(review.getUserId())) {
            throw new NotFoundException("Пользователь с id " + review.getUserId() + " не найден");
        }
        if (!filmStorage.existsById(review.getFilmId())) {
            throw new NotFoundException("Фильм с id " + review.getFilmId() + " не найден");
        }

        User user = new User();
        user.setId(review.getUserId());
        review.setUser(user);

        Film film = new Film();
        film.setId(review.getFilmId());
        review.setFilm(film);

        Review createdReview = reviewStorage.createReview(review);
        log.info("Создан отзыв: {}", createdReview.getId());
        return createdReview;
    }

    public Review updateReview(Review review) {
        validateReview(review);

        if (!userStorage.existsById(review.getUserId())) {
            throw new NotFoundException("Пользователь с id " + review.getUserId() + " не найден");
        }
        if (!filmStorage.existsById(review.getFilmId())) {
            throw new NotFoundException("Фильм с id " + review.getFilmId() + " не найден");
        }
        if (!reviewStorage.existsById(review.getId())) {
            throw new NotFoundException("Отзыв с id " + review.getId() + " не найден");
        }

        Review updatedReview = reviewStorage.updateReview(review);
        log.info("Отзыв обновлен: {}", updatedReview.getId());
        return updatedReview;
    }

    public void deleteReViewById(Long id) {
        if (!reviewStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        reviewStorage.deleteReViewById(id);
        log.info("Отзыв удален: {}", id);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Текст отзыва не может быть пустым");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("Id пользователя обязательно для указания");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Id фильма обязательно для указания");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Тип отзыва обязателен для указания");
        }
    }

    public void addLike(Long reviewId, Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!reviewStorage.existsById(reviewId)) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (reviewStorage.likeExists(reviewId, userId)) {
            throw new ValidationException("Лайк пользователя с id " + userId + " к отзыву с id " + reviewId + " уже существует");
        }
        reviewStorage.addLike(reviewId, userId);
        log.info("Лайк пользователя: {} к отзыву {} добавлен", userId, reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!reviewStorage.existsById(reviewId)) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (reviewStorage.dislikeExists(reviewId, userId)) {
            throw new ValidationException("Дизлайк пользователя с id " + userId + " к отзыву с id " + reviewId + " уже существует");
        }
        reviewStorage.addDislike(reviewId, userId);
        log.info("Дизлайк пользователя: {} к отзыву {} добавлен", userId, reviewId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!reviewStorage.existsById(reviewId)) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (!reviewStorage.likeExists(reviewId, userId)) {
            throw new ValidationException("Лайк пользователя с id " + userId + " к отзыву с id " + reviewId + " не существует");
        }
        reviewStorage.deleteLike(reviewId, userId);
        log.info("Лайк пользователя: {} к отзыву {} удален", userId, reviewId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!reviewStorage.existsById(reviewId)) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        if (!reviewStorage.dislikeExists(reviewId, userId)) {
            throw new ValidationException("Дизлайк пользователя с id " + userId + " к отзыву с id " + reviewId + " не существует");
        }
        reviewStorage.deleteDislike(reviewId, userId);
        log.info("Дизлайк пользователя: {} к отзыву {} удален", userId, reviewId);
    }

}
