MERGE INTO rating (rating_id, name) VALUES (1, 'G');
MERGE INTO rating (rating_id, name) VALUES (2, 'PG');
MERGE INTO rating (rating_id, name) VALUES (3, 'PG-13');
MERGE INTO rating (rating_id, name) VALUES (4, 'R');
MERGE INTO rating (rating_id, name) VALUES (5, 'NC-17');

MERGE INTO genre (genre_id, name) VALUES (1, 'Комедия');
MERGE INTO genre (genre_id, name) VALUES (2, 'Драма');
MERGE INTO genre (genre_id, name) VALUES (3, 'Мультфильм');
MERGE INTO genre (genre_id, name) VALUES (4, 'Триллер');
MERGE INTO genre (genre_id, name) VALUES (5, 'Документальный');
MERGE INTO genre (genre_id, name) VALUES (6, 'Боевик');