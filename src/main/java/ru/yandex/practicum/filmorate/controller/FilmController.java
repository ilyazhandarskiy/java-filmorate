package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable @NotNull Long id) {
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilmById(@PathVariable @NotNull Long id) {
        filmService.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLikeToFilmByUser(@PathVariable @NotNull Long id,
                                    @PathVariable @NotNull Long userId) {
        filmService.addLikeToFilmByUser(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromFilmByUser(@PathVariable @NotNull Long id,
                                         @PathVariable @NotNull Long userId) {
        filmService.removeLikeFromFilmByUser(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getFilmsByPopular(count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@RequestBody @Valid FilmDto filmDto) {
        return filmService.createFilm(filmDto);
    }

    @PutMapping
    public FilmDto updateFilm(@RequestBody @Valid FilmDto filmDto) {
        return filmService.updateFilm(filmDto);
    }
}
