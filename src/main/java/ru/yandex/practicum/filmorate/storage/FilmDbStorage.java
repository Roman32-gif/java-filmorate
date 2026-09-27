package ru.yandex.practicum.filmorate.storage;

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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> allFilms() {
        return jdbcTemplate.query(
                "SELECT * FROM Film",
                (rs, rowNum) -> mappingFilm(rs)
        );
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
        return jdbcTemplate.query(
                "SELECT * FROM Film WHERE id = ?",
                (rs, rowNum) -> mappingFilm(rs),
                filmId
        ).stream().findFirst();
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

    private void saveGenres(Film film) {
        Set<Long> genreIds = new LinkedHashSet<>();

        for (Genre genre : film.getGenres()) {
            if (genre != null) {
                genreIds.add(genre.getId());
            }
        }

        for (Long genreId : genreIds) {
            jdbcTemplate.update(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(),
                    genreId
            );
        }
    }

    private Film mappingFilm(ResultSet rs) throws SQLException {

        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(
                rs.getDate("release_date").toLocalDate()
        );
        film.setDuration(rs.getInt("duration"));

        /*
         * MPA
         */
        Long mpaId = rs.getObject("mpa_id", Long.class);

        if (mpaId != null) {

            Optional<Mpa> mpa = jdbcTemplate.query(
                    "SELECT id, name FROM mpa WHERE id = ?",
                    (mpaRs, rowNum) -> new Mpa(
                            mpaRs.getLong("id"),
                            mpaRs.getString("name")
                    ),
                    mpaId
            ).stream().findFirst();

            mpa.ifPresent(film::setMpa);
        }

        /*
         * Genres
         */
        List<Genre> genres = jdbcTemplate.query(
                """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY fg.genre_id
                """,
                (genreRs, rowNum) -> new Genre(
                        genreRs.getLong("id"),
                        genreRs.getString("name")
                ),
                film.getId()
        );

        film.setGenres(genres);

        /*
         * Likes
         */
        Set<Long> likes = new LinkedHashSet<>();

        jdbcTemplate.query(
                "SELECT user_id FROM film_likes WHERE film_id = ?",
                (likeRs, rowNum) -> {
                    likes.add(likeRs.getLong("user_id"));
                    return null;
                },
                film.getId()
        );

        film.setLikes(likes);

        return film;
    }
}