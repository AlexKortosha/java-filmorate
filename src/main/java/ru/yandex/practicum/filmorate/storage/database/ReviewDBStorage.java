package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;


import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Component("ReviewDbStorage")
@RequiredArgsConstructor
public class ReviewDBStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper = new ReviewRowMapper();

    @Override
    public Review getReviewById(Long id) {
        String sql = "SELECT " +
                "    r.review_id, " +
                "    r.content, " +
                "    r.is_positive, " +
                "    r.user_id, " +
                "    r.film_id, " +
                "    COALESCE(likes.like_count, 0) - COALESCE(dislikes.dislike_count, 0) AS useful " +
                "FROM reviews r " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as like_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'LIKE' " +
                "    GROUP BY review_id " +
                ") likes ON r.review_id = likes.review_id " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as dislike_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'DISLIKE' " +
                "    GROUP BY review_id " +
                ") dislikes ON r.review_id = dislikes.review_id " +
                "WHERE r.review_id = ?";

        return jdbcTemplate.query(sql, reviewRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + id + " не найден"));
    }

    @Override
    public Collection<Review> getReviewsByFilmWithLimit(Long filmId, Integer count) {
        String sql = "SELECT " +
                "    r.review_id, " +
                "    r.content, " +
                "    r.is_positive, " +
                "    r.user_id, " +
                "    r.film_id, " +
                "    COALESCE(likes.like_count, 0) - COALESCE(dislikes.dislike_count, 0) AS useful " +
                "FROM reviews r " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as like_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'LIKE' " +
                "    GROUP BY review_id " +
                ") likes ON r.review_id = likes.review_id " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as dislike_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'DISLIKE' " +
                "    GROUP BY review_id " +
                ") dislikes ON r.review_id = dislikes.review_id " +
                "WHERE r.film_id = ? " +
                "ORDER BY useful DESC " +  // Сортируем по полезности
                "LIMIT ?";

        return jdbcTemplate.query(sql, reviewRowMapper, filmId, count);
    }

    @Override
    public Collection<Review> getAllReviews() {
        String sql = "SELECT " +
                "    r.review_id, " +
                "    r.content, " +
                "    r.is_positive, " +
                "    r.user_id, " +
                "    r.film_id, " +
                "    COALESCE(likes.like_count, 0) - COALESCE(dislikes.dislike_count, 0) AS useful " +
                "FROM reviews r " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as like_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'LIKE' " +
                "    GROUP BY review_id " +
                ") likes ON r.review_id = likes.review_id " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as dislike_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'DISLIKE' " +
                "    GROUP BY review_id " +
                ") dislikes ON r.review_id = dislikes.review_id ";

        return  jdbcTemplate.query(sql, reviewRowMapper);
    }

    @Override
    public Collection<Review> getAllReviewsWithLimit(Integer count) {
        String sql = "SELECT " +
                "    r.review_id, " +
                "    r.content, " +
                "    r.is_positive, " +
                "    r.user_id, " +
                "    r.film_id, " +
                "    COALESCE(likes.like_count, 0) - COALESCE(dislikes.dislike_count, 0) AS useful " +
                "FROM reviews r " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as like_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'LIKE' " +
                "    GROUP BY review_id " +
                ") likes ON r.review_id = likes.review_id " +
                "LEFT JOIN ( " +
                "    SELECT review_id, COUNT(*) as dislike_count " +
                "    FROM review_like rl " +
                "    JOIN reactions re ON rl.reaction_id = re.reaction_id " +
                "    WHERE re.name = 'DISLIKE' " +
                "    GROUP BY review_id " +
                ") dislikes ON r.review_id = dislikes.review_id " +
                "ORDER BY useful DESC " +  // Сортируем по полезности
                "LIMIT ?";

        return jdbcTemplate.query(sql, reviewRowMapper, count);
    }

    @Override
    public Review createReview(Review review) {
        String sql = "INSERT INTO reviews (content, user_id, film_id, is_positive) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setLong(2, review.getUser().getId());
            ps.setLong(3, review.getFilm().getId());
            ps.setBoolean(4, review.getIsPositive());
            return ps;
        }, keyHolder);

        review.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, user_id = ?, film_id = ?, is_positive = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE review_id = ?";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.getUserId(),
                review.getFilmId(),
                review.getIsPositive(),
                review.getId());
        return getReviewById(review.getId());
    }

    @Override
    public void deleteReViewById(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        String deleteSql = "DELETE FROM review_like WHERE review_id = ? AND user_id = ? AND reaction_id = 2";
        jdbcTemplate.update(deleteSql, reviewId, userId);

        String insertSql = "INSERT INTO review_like (review_id, user_id, reaction_id) VALUES (?, ?, 1)";
        jdbcTemplate.update(insertSql, reviewId, userId);
        getReviewById(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String deleteSql = "DELETE FROM review_like WHERE review_id = ? AND user_id = ? AND reaction_id = 1";
        jdbcTemplate.update(deleteSql, reviewId, userId);

        String insertSql = "INSERT INTO review_like (review_id, user_id, reaction_id) VALUES (?, ?, 2)";
        jdbcTemplate.update(insertSql, reviewId, userId);
        getReviewById(reviewId);
    }

    @Override
    public void deleteLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_like WHERE review_id = ? AND user_id = ? AND reaction_id = 1";
        jdbcTemplate.update(sql, reviewId, userId);
        getReviewById(reviewId);
    }

    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_like WHERE review_id = ? AND user_id = ? AND reaction_id = 2";
        jdbcTemplate.update(sql, reviewId, userId);
        getReviewById(reviewId);
    }

    @Override
    public boolean likeExists(Long reviewId, Long userId) {
        String sql = "SELECT COUNT(*) FROM review_like WHERE review_id = ? AND user_id = ? AND reaction_id = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        return count > 0;
    }

    @Override
    public boolean dislikeExists(Long reviewId, Long userId) {
        String sql = "SELECT COUNT(*) FROM review_like WHERE review_id = ? AND user_id = ? AND reaction_id = 2";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        return count > 0;
    }

}
