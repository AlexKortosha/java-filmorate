package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @GetMapping
    public ResponseEntity<Collection<Review>> getReviews(@RequestParam(required = false) Long filmId, @RequestParam(defaultValue = "10") Integer count) {
        if (filmId != null) {
            return ResponseEntity.ok(reviewService.getReviewsByFilmWithLimit(filmId, count));
        } else {
            return ResponseEntity.ok(reviewService.getAllReviewsWithLimit(count));
        }

    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review createdReview = reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @PutMapping
    public ResponseEntity<Review> updateReview(@RequestBody Review review) {
        Review updatedReview = reviewService.updateReview(review);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReViewById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public ResponseEntity<Review> addLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.addLike(reviewId, userId);
        Review review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.addDislike(reviewId, userId);
        Review review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public ResponseEntity<Review> deleteLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.deleteLike(reviewId, userId);
        Review review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public ResponseEntity<Review> deleteDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.deleteDislike(reviewId, userId);
        Review review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }

}
