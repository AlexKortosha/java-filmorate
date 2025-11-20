package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
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
    private final EventService eventService;

    public Review getReview(Long id) {
        validateReviewExists(id);
        return reviewStorage.getReviewById(id);
    }

    public Collection<Review> getAllReviewsWithLimit(Integer count) {
        return reviewStorage.getAllReviewsWithLimit(count);
    }

    public Collection<Review> getReviewsByFilmWithLimit(Long filmId, Integer count) {
        validateFilmExists(filmId);
        return reviewStorage.getReviewsByFilmWithLimit(filmId, count);
    }

    public Review createReview(Review review) {
        validateReview(review);
        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());

        User user = new User();
        user.setId(review.getUserId());
        review.setUser(user);

        Film film = new Film();
        film.setId(review.getFilmId());
        review.setFilm(film);

        Review createdReview = reviewStorage.createReview(review);
        log.info("Создан отзыв: {}", createdReview.getId());

        eventService.createEvent(new Event(
                null,
                System.currentTimeMillis(),
                createdReview.getUserId(),
                Event.EventType.REVIEW,
                Event.Operation.ADD,
                createdReview.getId()
        ));
        return createdReview;
    }

    public Review updateReview(Review review) {
        validateReview(review);
        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());
        validateReviewExists(review.getId());


        Review updatedReview = reviewStorage.updateReview(review);
        log.info("Отзыв обновлен: {}", updatedReview.getId());

        eventService.createEvent(new Event(
                null,
                System.currentTimeMillis(),
                updatedReview.getUserId(),
                Event.EventType.REVIEW,
                Event.Operation.UPDATE,
                updatedReview.getId()
        ));
        return updatedReview;
    }

    public void deleteReViewById(Long id) {
        validateReviewExists(id);
        Review review = reviewStorage.getReviewById(id);
        reviewStorage.deleteReViewById(id);
        log.info("Отзыв удален: {}", id);

        eventService.createEvent(new Event(
                null,
                System.currentTimeMillis(),
                review.getUserId(),
                Event.EventType.REVIEW,
                Event.Operation.REMOVE,
                id
        ));
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
        validateReviewAndUser(reviewId, userId);
        validateReactionNotExists(reviewId, userId, ReactionType.LIKE);
        reviewStorage.addLike(reviewId, userId);
        log.info("Лайк пользователя: {} к отзыву {} добавлен", userId, reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        validateReactionNotExists(reviewId, userId, ReactionType.DISLIKE);
        reviewStorage.addDislike(reviewId, userId);
        log.info("Дизлайк пользователя: {} к отзыву {} добавлен", userId, reviewId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        validateReactionExists(reviewId, userId, ReactionType.LIKE);
        reviewStorage.deleteLike(reviewId, userId);
        log.info("Лайк пользователя: {} к отзыву {} удален", userId, reviewId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        validateReactionExists(reviewId, userId, ReactionType.DISLIKE);
        reviewStorage.deleteDislike(reviewId, userId);
        log.info("Дизлайк пользователя: {} к отзыву {} удален", userId, reviewId);
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void validateFilmExists(Long filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    private void validateReviewExists(Long reviewId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);
    }

    private void validateReactionNotExists(Long reviewId, Long userId, ReactionType reactionType) {
        if (reviewStorage.reactionExists(reviewId, userId, reactionType.getId())) {
            throw new ValidationException(reactionType.getDescription() + " пользователя с id " + userId +
                    " к отзыву с id " + reviewId + " уже существует");
        }
    }

    private void validateReactionExists(Long reviewId, Long userId, ReactionType reactionType) {
        if (!reviewStorage.reactionExists(reviewId, userId, reactionType.getId())) {
            throw new ValidationException(reactionType.getDescription() + " пользователя с id " + userId +
                    " к отзыву с id " + reviewId + " не существует");
        }
    }
}
