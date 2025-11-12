package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review getReviewById(Long id);

    Collection<Review> getAllReviewsWithLimit(Integer count);

    Collection<Review> getAllReviews();

    Collection<Review> getReviewsByFilmWithLimit(Long filmId, Integer count);

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReViewById(Long id);

    boolean existsById(Long id);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);

    boolean likeExists(Long reviewId, Long userId);

    boolean dislikeExists(Long reviewId, Long userId);
}
