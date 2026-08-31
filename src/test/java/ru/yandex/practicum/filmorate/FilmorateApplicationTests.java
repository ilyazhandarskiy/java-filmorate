package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FilmorateApplicationTests {
    private static final String BASE = "http://localhost:8080";
    private static HttpClient client;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() throws Exception {
        SpringApplication.run(FilmorateApplication.class);
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
                    }

                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return LocalDate.parse(in.nextString());
                    }
                })
                .create();
        //создаем фильм
        String filmJson = "{\n" +
                "  \"name\": \"Test\",\n" +
                "  \"description\": \"" + "+".repeat(200) + "\",\n" +
                "  \"releaseDate\": \"1895-12-28\",\n" +
                "  \"duration\": 1\n" +
                "}";

        HttpRequest filmRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(filmJson, StandardCharsets.UTF_8))
                .build();

        client.send(filmRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        //создание пользователя
        String userJson = "{\n" +

                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"test@test.ru\",\n" +
                "  \"birthday\": \"" + LocalDate.now() + "\"\n" +
                "}";
        HttpRequest userRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(userJson, StandardCharsets.UTF_8))
                .build();

        client.send(userRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    }

    @Test
    void contextLoads() {
    }

    //---FilmController---
    @DisplayName("GET /films - Получение фильмов")
    @Test
    void getFilms_shouldReturnHTTP200() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
    }

    @DisplayName("PUT /films - Проверить возврат 404 при несуществующем id")
    @Test
    void updateFilmWithIncorrectId_shouldReturnHTTP404() throws Exception {
        String json = "{\n" +
                "  \"id\": 0,\n" +
                "  \"name\": \"test\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
    }

    @DisplayName("POST /films - Проверить, что название не может быть пустым")
    @Test
    void createFilmWithEmptyName_shouldReturnHTTP400() throws Exception {
        String json = "{\n" +
                "  \"name\": \"\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("POST /films - Проверить граничные состояния: макс длина описания 200 и дата 1895-12-28 ")
    @Test
    void createFilmWithBoundaryCase_shouldReturnHTTP200() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Test\",\n" +
                "  \"description\": \"" + "+".repeat(200) + "\",\n" +
                "  \"releaseDate\": \"1895-12-28\",\n" +
                "  \"duration\": 1\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode());
    }

    @DisplayName("POST /films - Проверить, что макс длина описания 201")
    @Test
    void createFilmWithDesc201_shouldReturnHTTP400() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Test\",\n" +
                "  \"description\": \"" + "+".repeat(201) + "\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("POST /films - Проверить, что релиз фильма не раньше 28 декабря 1895 года")
    @Test
    void createFilmWithIncorrectReleaseDate_shouldReturnHTTP400() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Test\",\n" +
                "  \"description\": \"Test\",\n" +
                "  \"releaseDate\": \"1895-12-27\",\n" +
                "  \"duration\": 100\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("POST /films  - Проверить, что продолжительность фильма должна быть положительной")
    @Test
    void createFilmWithIncorrectDuration_shouldReturnHTTP400() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Test\",\n" +
                "  \"description\": \"Test\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 0 \n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("GET /films/{id} - Получение фильма по id")
    @Test
    void getFilmById_shouldReturnHTTP200() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/1"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
    }

    @DisplayName("GET /films/{id} - Неуспешное получение фильма по несуществующему id")
    @Test
    void getFilmByIdWithIncorrectId_shouldReturnHTTP404() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/999999"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
    }

    @DisplayName("DELETE /films/{id} - Удаление фильма по id")
    @Test
    void deleteFilmById_shouldReturnHTTP204() throws Exception {
        // Сначала создаём фильм
        String json = "{\n" +
                "  \"name\": \"Film to delete\",\n" +
                "  \"description\": \"desc\",\n" +
                "  \"releaseDate\": \"2000-01-01\",\n" +
                "  \"duration\": 100\n" +
                "}";

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createResp =
                client.send(createReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createResp.statusCode());

        // Получаем id созданного фильма
        Film film = gson.fromJson(
                createResp.body(),
                new TypeToken<ru.yandex.practicum.filmorate.model.Film>() {
                }.getType()
        );
        Long id = film.getId();

        // Удаляем фильм
        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/" + id))
                .DELETE()
                .build();

        HttpResponse<String> deleteResp =
                client.send(deleteReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, deleteResp.statusCode());
    }

    @DisplayName("PUT /films/{id}/like/{userId} - Добавление лайка фильму")
    @Test
    void addLikeToFilmByUser_shouldReturnHTTP204() throws Exception {
        // Создаём пользователя
        String userJson = "{\n" +
                "  \"login\": \"likeUser\",\n" +
                "  \"name\": \"Like User\",\n" +
                "  \"email\": \"likeuser@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createUserReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(userJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUserResp =
                client.send(createUserReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUserResp.statusCode());

        User user = gson.fromJson(
                createUserResp.body(),
                new TypeToken<User>() {
                }.getType()
        );
        Long userId = user.getId();

        // Создаём фильм
        String filmJson = "{\n" +
                "  \"name\": \"Film for like\",\n" +
                "  \"description\": \"desc\",\n" +
                "  \"releaseDate\": \"2001-01-01\",\n" +
                "  \"duration\": 120\n" +
                "}";

        HttpRequest createFilmReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(filmJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createFilmResp =
                client.send(createFilmReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createFilmResp.statusCode());

        ru.yandex.practicum.filmorate.model.Film film = gson.fromJson(
                createFilmResp.body(),
                new TypeToken<ru.yandex.practicum.filmorate.model.Film>() {
                }.getType()
        );
        Long filmId = film.getId();

        // Добавляем лайк
        HttpRequest likeReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/" + filmId + "/like/" + userId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> likeResp =
                client.send(likeReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, likeResp.statusCode());
    }

    @DisplayName("DELETE /films/{id}/like/{userId} - Удаление лайка у фильма")
    @Test
    void removeLikeFromFilmByUser_shouldReturnHTTP204() throws Exception {
        // Чтобы было что удалять, сначала используем тест выше по сути:
        String userJson = "{\n" +
                "  \"login\": \"unlikeUser\",\n" +
                "  \"name\": \"Unlike User\",\n" +
                "  \"email\": \"unlikeuser@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createUserReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(userJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUserResp =
                client.send(createUserReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUserResp.statusCode());

        User user = gson.fromJson(
                createUserResp.body(),
                new TypeToken<User>() {
                }.getType()
        );
        Long userId = user.getId();

        String filmJson = "{\n" +
                "  \"name\": \"Film for unlike\",\n" +
                "  \"description\": \"desc\",\n" +
                "  \"releaseDate\": \"2001-01-01\",\n" +
                "  \"duration\": 120\n" +
                "}";

        HttpRequest createFilmReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(filmJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createFilmResp =
                client.send(createFilmReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createFilmResp.statusCode());

        ru.yandex.practicum.filmorate.model.Film film = gson.fromJson(
                createFilmResp.body(),
                new TypeToken<ru.yandex.practicum.filmorate.model.Film>() {
                }.getType()
        );
        Long filmId = film.getId();

        // Добавляем лайк
        HttpRequest likeReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/" + filmId + "/like/" + userId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> likeResp =
                client.send(likeReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, likeResp.statusCode());

        // Удаляем лайк
        HttpRequest unlikeReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/" + filmId + "/like/" + userId))
                .DELETE()
                .build();

        HttpResponse<String> unlikeResp =
                client.send(unlikeReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, unlikeResp.statusCode());
    }

    @DisplayName("GET /films/popular - Получение популярных фильмов со значением по умолчанию count=10")
    @Test
    void getPopularFilmsDefaultCount_shouldReturnHTTP200() throws Exception {
        for (int i = 1; i <= 10; i++) {
            String json = "{\n" +
                    "  \"name\": \"Film " + i + "\",\n" +
                    "  \"description\": \"desc " + i + "\",\n" +
                    "  \"releaseDate\": \"2000-01-01\",\n" +
                    "  \"duration\": " + (100 + i) + "\n" +
                    "}";

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/films"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> createResp =
                    client.send(createReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertEquals(201, createResp.statusCode());
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/popular"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        java.lang.reflect.Type type = new TypeToken<List<Film>>() {}.getType();
        List<Film> films = gson.fromJson(resp.body(), type);

        assertEquals(10, films.size());
    }

    @DisplayName("GET /films/popular?count=1 - Получение одного популярного фильма")
    @Test
    void getPopularFilmsWithCount_shouldReturnHTTP200() throws Exception {
        for (int i = 1; i <= 10; i++) {
            String json = "{\n" +
                    "  \"name\": \"Film " + i + "\",\n" +
                    "  \"description\": \"desc " + i + "\",\n" +
                    "  \"releaseDate\": \"2000-01-01\",\n" +
                    "  \"duration\": " + (100 + i) + "\n" +
                    "}";

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/films"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> createResp =
                    client.send(createReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertEquals(201, createResp.statusCode());
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/films/popular?count=1"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        java.lang.reflect.Type type = new TypeToken<List<Film>>() {}.getType();
        List<Film> films = gson.fromJson(resp.body(), type);

        assertEquals(1, films.size());
    }

    //---UserController---

    @DisplayName("GET /users - Получить всех пользователей")
    @Test
    void getAllUsers_shouldReturnHTTP200() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
    }

    @DisplayName("POST /users - Проверить, что имейл не может быть пустой и без @")
    @Test
    void createUserWithIncorrectEmail_shouldReturnHTTP400() throws Exception {
        // Проверка при пустом имейле
        String json = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        // Проверка при некорректном имейле
        json = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("POST /users - Проверить, что логин не может быть пустым или содержать пробелы")
    @Test
    void createUserWithIncorrectLogin_shouldReturnHTTP400() throws Exception {
        // Проверка на пустой логин
        String json = "{\n" +
                "  \"login\": \"\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"test@test.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        // Проверка на наличие пробелов
        json = "{\n" +
                "  \"login\": \"dolore red\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"test@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("POST /users - Создание пользователя на граничных состояниях: пустой name, " +
            "login автоматически == name, " +
            "birthday = LocalDate.now().Now")
    @Test
    void createUserWithBoundaryCase_shouldReturnHTTP200() throws Exception {
        String json = "{\n" +

                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"test@test.ru\",\n" +
                "  \"birthday\": \"" + LocalDate.now() + "\"\n" +
                "}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode());

        User user = gson.fromJson(resp.body(), new TypeToken<User>() {
        }.getType());
        assertEquals(user.getLogin(), user.getName());

    }

    @DisplayName("POST /users - День рождения не может быть в будущем")
    @Test
    void createUserWithIncorrectBirthday_shouldReturnHTTP400() throws Exception {
        String json = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"test@mail.ru\",\n" +
                "  \"birthday\": \"2946-08-20\"\n" +
                "}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @DisplayName("PUT /users - Проверить возврат 404 при несуществующем id")
    @Test
    void updateUserWithIncorrectId_shouldReturnHTTP404() throws Exception {
        String json = "{\n" +
                "  \"id\": 0,\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"test@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
    }

    @DisplayName("GET /users/{id} - Получение пользователя по id")
    @Test
    void getUserById_shouldReturnHTTP200() throws Exception {
        // Сначала создаём пользователя, чтобы затем получить его по id
        String json = "{\n" +
                "  \"login\": \"getUser\",\n" +
                "  \"name\": \"Get User\",\n" +
                "  \"email\": \"getuser@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createResp =
                client.send(createReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createResp.statusCode());

        // Достаём id созданного пользователя из ответа
        User created = gson.fromJson(createResp.body(), new TypeToken<User>() {}.getType());
        Long id = created.getId();

        // Запрашиваем пользователя по полученному id
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id))
                .GET()
                .build();

        HttpResponse<String> getResp =
                client.send(getReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getResp.statusCode());
    }

    @DisplayName("GET /users/{id} - Неуспешное получение пользователя по несуществующему id")
    @Test
    void getUserByIdWithIncorrectId_shouldReturnHTTP404() throws Exception {
        // Пытаемся получить пользователя, которого нет в системе
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/999999"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
    }

    @DisplayName("DELETE /users/{id} - Удаление пользователя по id")
    @Test
    void deleteUserById_shouldReturnHTTP204() throws Exception {
        // Создаём пользователя, чтобы затем удалить его
        String json = "{\n" +
                "  \"login\": \"deleteUser\",\n" +
                "  \"name\": \"Delete User\",\n" +
                "  \"email\": \"deleteuser@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createResp =
                client.send(createReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createResp.statusCode());

        // Берём id созданного пользователя
        User created = gson.fromJson(createResp.body(), new TypeToken<User>() {}.getType());
        Long id = created.getId();

        // Удаляем пользователя по id
        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id))
                .DELETE()
                .build();

        HttpResponse<String> deleteResp =
                client.send(deleteReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, deleteResp.statusCode());
    }

    @DisplayName("PUT /users/{id}/friends/{friendId} - Добавление в друзья")
    @Test
    void addToFriends_shouldReturnHTTP204() throws Exception {
        // Создаём первого пользователя
        String user1Json = "{\n" +
                "  \"login\": \"user1\",\n" +
                "  \"name\": \"User One\",\n" +
                "  \"email\": \"user1@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createUser1Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user1Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser1Resp =
                client.send(createUser1Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser1Resp.statusCode());

        User user1 = gson.fromJson(createUser1Resp.body(), new TypeToken<User>() {}.getType());
        Long id1 = user1.getId();

        // Создаём второго пользователя
        String user2Json = "{\n" +
                "  \"login\": \"user2\",\n" +
                "  \"name\": \"User Two\",\n" +
                "  \"email\": \"user2@test.ru\",\n" +
                "  \"birthday\": \"1992-02-02\"\n" +
                "}";

        HttpRequest createUser2Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user2Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser2Resp =
                client.send(createUser2Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser2Resp.statusCode());

        User user2 = gson.fromJson(createUser2Resp.body(), new TypeToken<User>() {}.getType());
        Long id2 = user2.getId();

        // Добавляем второго пользователя в друзья к первому
        HttpRequest addFriendReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends/" + id2))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> addFriendResp =
                client.send(addFriendReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, addFriendResp.statusCode());

        // Получаем первого пользователя и проверяем, что он видит второго в списке друзей
        HttpRequest getUser1Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1))
                .GET()
                .build();

        HttpResponse<String> getUser1Resp =
                client.send(getUser1Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getUser1Resp.statusCode());

        User updatedUser1 = gson.fromJson(getUser1Resp.body(), new TypeToken<User>() {}.getType());
        // TODO
        // assertEquals(true, updatedUser1.getFriends().contains(id2));

        // Получаем второго пользователя и проверяем, что он тоже видит первого в списке друзей
        HttpRequest getUser2Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id2))
                .GET()
                .build();

        HttpResponse<String> getUser2Resp =
                client.send(getUser2Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getUser2Resp.statusCode());

        User updatedUser2 = gson.fromJson(getUser2Resp.body(), new TypeToken<User>() {}.getType());
        // TODO
        //assertEquals(true, updatedUser2.getFriends().contains(id1));
    }

    @DisplayName("DELETE /users/{id}/friends/{friendId} - Удаление из друзей")
    @Test
    void deleteFromFriends_shouldReturnHTTP204() throws Exception {
        // Создаём первого пользователя
        String user1Json = "{\n" +
                "  \"login\": \"user1Del\",\n" +
                "  \"name\": \"User One Del\",\n" +
                "  \"email\": \"user1del@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createUser1Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user1Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser1Resp =
                client.send(createUser1Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser1Resp.statusCode());

        User user1 = gson.fromJson(createUser1Resp.body(), new TypeToken<User>() {}.getType());
        Long id1 = user1.getId();

        // Создаём второго пользователя
        String user2Json = "{\n" +
                "  \"login\": \"user2Del\",\n" +
                "  \"name\": \"User Two Del\",\n" +
                "  \"email\": \"user2del@test.ru\",\n" +
                "  \"birthday\": \"1992-02-02\"\n" +
                "}";

        HttpRequest createUser2Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user2Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser2Resp =
                client.send(createUser2Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser2Resp.statusCode());

        User user2 = gson.fromJson(createUser2Resp.body(), new TypeToken<User>() {}.getType());
        Long id2 = user2.getId();

        // Сначала добавляем дружбу между пользователями
        HttpRequest addFriendReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends/" + id2))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> addFriendResp =
                client.send(addFriendReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, addFriendResp.statusCode());

        // Потом удаляем этого друга
        HttpRequest deleteFriendReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends/" + id2))
                .DELETE()
                .build();

        HttpResponse<String> deleteFriendResp =
                client.send(deleteFriendReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, deleteFriendResp.statusCode());
    }

    @DisplayName("GET /users/{id}/friends - Получение списка друзей пользователя")
    @Test
    void getFriends_shouldReturnHTTP200() throws Exception {
        // Создаём первого пользователя
        String user1Json = "{\n" +
                "  \"login\": \"friendsUser1\",\n" +
                "  \"name\": \"Friends User One\",\n" +
                "  \"email\": \"friendsuser1@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createUser1Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user1Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser1Resp =
                client.send(createUser1Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser1Resp.statusCode());

        User user1 = gson.fromJson(createUser1Resp.body(), new TypeToken<User>() {}.getType());
        Long id1 = user1.getId();

        // Создаём второго пользователя, который станет другом первого
        String user2Json = "{\n" +
                "  \"login\": \"friendsUser2\",\n" +
                "  \"name\": \"Friends User Two\",\n" +
                "  \"email\": \"friendsuser2@test.ru\",\n" +
                "  \"birthday\": \"1992-02-02\"\n" +
                "}";

        HttpRequest createUser2Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user2Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser2Resp =
                client.send(createUser2Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser2Resp.statusCode());

        User user2 = gson.fromJson(createUser2Resp.body(), new TypeToken<User>() {}.getType());
        Long id2 = user2.getId();

        // Добавляем второго пользователя в друзья к первому
        HttpRequest addFriendReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends/" + id2))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> addFriendResp =
                client.send(addFriendReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, addFriendResp.statusCode());

        // Проверяем, что список друзей успешно возвращается
        HttpRequest getFriendsReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends"))
                .GET()
                .build();

        HttpResponse<String> getFriendsResp =
                client.send(getFriendsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getFriendsResp.statusCode());
    }

    @DisplayName("GET /users/{id}/friends/common/{friendId} - Получение общих друзей")
    @Test
    void getCommonFriends_shouldReturnHTTP200() throws Exception {
        // Создаём первого пользователя
        String user1Json = "{\n" +
                "  \"login\": \"commonUser1\",\n" +
                "  \"name\": \"Common User One\",\n" +
                "  \"email\": \"commonuser1@test.ru\",\n" +
                "  \"birthday\": \"1990-01-01\"\n" +
                "}";

        HttpRequest createUser1Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user1Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser1Resp =
                client.send(createUser1Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser1Resp.statusCode());

        User user1 = gson.fromJson(createUser1Resp.body(), new TypeToken<User>() {}.getType());
        Long id1 = user1.getId();

        // Создаём второго пользователя
        String user2Json = "{\n" +
                "  \"login\": \"commonUser2\",\n" +
                "  \"name\": \"Common User Two\",\n" +
                "  \"email\": \"commonuser2@test.ru\",\n" +
                "  \"birthday\": \"1992-02-02\"\n" +
                "}";

        HttpRequest createUser2Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(user2Json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createUser2Resp =
                client.send(createUser2Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createUser2Resp.statusCode());

        User user2 = gson.fromJson(createUser2Resp.body(), new TypeToken<User>() {}.getType());
        Long id2 = user2.getId();

        // Создаём общего друга
        String friendJson = "{\n" +
                "  \"login\": \"commonFriend\",\n" +
                "  \"name\": \"Common Friend\",\n" +
                "  \"email\": \"commonfriend@test.ru\",\n" +
                "  \"birthday\": \"1993-03-03\"\n" +
                "}";

        HttpRequest createFriendReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(friendJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> createFriendResp =
                client.send(createFriendReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, createFriendResp.statusCode());

        User friend = gson.fromJson(createFriendResp.body(), new TypeToken<User>() {}.getType());
        Long friendId = friend.getId();

        // Добавляем общего друга к первому пользователю
        HttpRequest addFriendToUser1Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends/" + friendId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> addFriendToUser1Resp =
                client.send(addFriendToUser1Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, addFriendToUser1Resp.statusCode());

        // Добавляем того же друга ко второму пользователю
        HttpRequest addFriendToUser2Req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id2 + "/friends/" + friendId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> addFriendToUser2Resp =
                client.send(addFriendToUser2Req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, addFriendToUser2Resp.statusCode());

        // Запрашиваем список общих друзей
        HttpRequest getCommonReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/users/" + id1 + "/friends/common/" + id2))
                .GET()
                .build();

        HttpResponse<String> getCommonResp =
                client.send(getCommonReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getCommonResp.statusCode());
    }
}