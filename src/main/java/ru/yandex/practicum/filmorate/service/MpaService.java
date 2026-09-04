package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;
    private final MpaMapper mpaMapper;

    public List<MpaDto> getAll() {
        log.debug("Request to get all MPA ratings");
        List<Mpa> ratings = mpaStorage.getAll();
        log.info("Returning {} MPA ratings", ratings.size());
        return ratings.stream().map(mpaMapper::toDto).collect(Collectors.toList());
    }

    public MpaDto getById(Long id) {
        log.debug("Request to get MPA by id {}", id);
        Mpa mpa = mpaStorage.getById(id);
        log.info("Found MPA by id {}: {}", id, mpa);
        return mpaMapper.toDto(mpa);
    }
}
