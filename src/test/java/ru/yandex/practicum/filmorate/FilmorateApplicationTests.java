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
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FilmorateApplicationTests {
    private static final String BASE = "http://localhost:8080";
    private static HttpClient client;
    private static Gson gson;


    @BeforeAll
    static void beforeAll() {
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

        assertEquals(200, resp.statusCode());
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

        assertEquals(200, resp.statusCode());

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
}