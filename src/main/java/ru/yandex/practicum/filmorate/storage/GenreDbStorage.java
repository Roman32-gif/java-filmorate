package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("GenreDbStorage")
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT * FROM genres", (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }
        );
    }

    @Override
    public Optional<Genre> getGenreById(Long genreId) {
        return jdbcTemplate.query("SELECT * FROM genres WHERE id = ?", (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, genreId
        ).stream().findFirst();
    }

    @Override
    public Collection<Genre> getGenresByIds(Collection<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        String placeholders = genreIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT * FROM genres WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                genreIds.toArray()
        );
    }
}
