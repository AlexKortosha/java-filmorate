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

    Review addLike(Long reviewId, Long userId);

    Review addDislike(Long reviewId, Long userId);

    Review deleteLike(Long reviewId, Long userId);

    Review deleteDislike(Long reviewId, Long userId);

    boolean likeExists(Long reviewId, Long userId);

    boolean dislikeExists(Long reviewId, Long userId);
}
