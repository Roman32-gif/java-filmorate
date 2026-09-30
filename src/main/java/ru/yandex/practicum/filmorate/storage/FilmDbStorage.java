package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Repository("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> allFilms() {
       List<Film> films = jdbcTemplate.query(
               """
                    SELECT f.id,
                     f.name,
                     f.description,
                     f.release_date,
                     f.duration,
                     f.mpa_id,
                     m.name AS mpa_name
                    FROM Film f
                    LEFT JOIN mpa m ON f.mpa_id = m.id
                    """,
                (rs, rowNum) -> mappingFilm(rs)
        );
       loadGenres(films);
       loadLikes(films);
       return films;
    }

    @Override
    public Film createFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO Film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        saveGenres(film);

        return getFilmById(film.getId()).orElse(film);
    }

    @Override
    public Film updateFilm(Film film) {

        jdbcTemplate.update(
                """
                UPDATE Film
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        saveGenres(film);

        return getFilmById(film.getId()).orElse(film);
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        List<Film> films = jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM Film f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """,
                (rs, rowNum) -> mappingFilm(rs),
                filmId
        );

        if (films.isEmpty()) {
            return Optional.empty();
        }

        loadGenres(films);
        loadLikes(films);

        return Optional.of(films.get(0));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                filmId,
                userId
        );
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId,
                userId
        );
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name,
                       COUNT(fl.user_id) AS like_count
                FROM Film f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id,
                         f.name,
                         f.description,
                         f.release_date,
                         f.duration,
                         f.mpa_id,
                         m.name
                ORDER BY like_count DESC
                LIMIT ?
                """,
                (rs, rowNum) -> mappingFilm(rs),
                count
        );

        loadGenres(films);
        loadLikes(films);

        return films;
    }

    private Film mappingFilm(ResultSet rs) throws SQLException {

        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Long mpaId = rs.getObject("mpa_id", Long.class);

        if (mpaId != null) {
            film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")
            ));
        }
    return film;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        Set<Long> genreIds = film.getGenres().stream()
                .filter(genre -> genre != null)
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (genreIds.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {

                    private final List<Long> ids = new ArrayList<>(genreIds);

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                }
        );
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        Map<Long, List<Genre>> genresByFilmId = new HashMap<>();

        jdbcTemplate.query(
                """
                SELECT fg.film_id,
                       g.id,
                       g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (%s)
                ORDER BY fg.film_id, fg.genre_id
                """.formatted(placeholders),
                rs -> {
                    Long filmId = rs.getLong("film_id");

                    Genre genre = new Genre(
                            rs.getLong("id"),
                            rs.getString("name")
                    );

                    genresByFilmId
                            .computeIfAbsent(filmId, key -> new ArrayList<>())
                            .add(genre);
                },
                filmIds.toArray()
        );

        for (Film film : films) {
            film.setGenres(
                    genresByFilmId.getOrDefault(
                            film.getId(),
                            List.of()
                    )
            );
        }
    }

    private void loadLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        Map<Long, Set<Long>> likesByFilmId = new HashMap<>();

        jdbcTemplate.query(
                """
                SELECT film_id, user_id
                FROM film_likes
                WHERE film_id IN (%s)
                """.formatted(placeholders),
                rs -> {
                    Long filmId = rs.getLong("film_id");
                    Long userId = rs.getLong("user_id");

                    likesByFilmId
                            .computeIfAbsent(
                                    filmId,
                                    key -> new LinkedHashSet<>()
                            )
                            .add(userId);
                },
                filmIds.toArray()
        );

        for (Film film : films) {
            film.setLikes(
                    likesByFilmId.getOrDefault(
                            film.getId(),
                            new LinkedHashSet<>()
                    )
            );
        }
    }
}