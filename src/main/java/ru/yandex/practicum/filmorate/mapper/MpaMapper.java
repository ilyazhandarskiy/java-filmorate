package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class MpaMapper {

    public MpaDto toDto(Mpa mpa) {
        if (mpa == null) {
            return null;
        } else {
            MpaDto mpaDto = new MpaDto();
            mpaDto.setId(mpa.getId());
            mpaDto.setName(mpa.getName());
            return mpaDto;
        }
    }
}
