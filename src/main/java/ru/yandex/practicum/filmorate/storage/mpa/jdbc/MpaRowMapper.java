package ru.yandex.practicum.filmorate.storage.mpa.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.annotation.Nullable;

@Component
public class MpaRowMapper implements RowMapper<Mpa> {

    @Nullable
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getLong("id"), rs.getString("name"));
    }
}
