package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import java.util.Collection;
import java.util.Optional;

@Repository("MpaDbStorage")
public class MpaDbStorage implements MpaStorage{
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> getAllMpa() {
        return jdbcTemplate.query("SELECT * FROM mpa", (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getLong("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                }
            );
    }

    @Override
    public Optional<Mpa> getMpaById(Long mpaId) {
        return jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?", (rs, rowNum) -> {
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("id"));
                mpa.setName(rs.getString("name"));
                return mpa;
        },
        mpaId
        ).stream().findFirst();
    }
}
