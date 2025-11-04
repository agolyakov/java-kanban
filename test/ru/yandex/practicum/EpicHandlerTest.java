package ru.yandex.practicum;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import ru.yandex.practicum.http.adapter.DurationTypeAdapter;
import ru.yandex.practicum.http.adapter.LocalDateTimeTypeAdapter;
import ru.yandex.practicum.http.server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EpicHandlerTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    GsonBuilder gsonBuilder = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .serializeNulls();

    Gson gson = gsonBuilder.create();

    public EpicHandlerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");
        // конвертируем её в JSON
        epic.setStartTime(LocalDateTime.now());
        epic.setEndTime(LocalDateTime.now().plusMinutes(5));
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, epicResponse.statusCode());

        epic.setId(1);
        epicJson = gson.toJson(epic);
        HttpRequest epicRequest500 = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response500 = client.send(epicRequest500, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(500, response500.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Test epic 1", "Testing epic");
        // конвертируем её в JSON
        epic.setStartTime(LocalDateTime.now());
        epic.setEndTime(LocalDateTime.now().plusMinutes(5));
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, epicResponse.statusCode());

        // создаём задачу
        Epic epic2 = new Epic("Test epic 2", "Testing epic 2");
        epic2.setStartTime(LocalDateTime.now());
        epic2.setEndTime(LocalDateTime.now().plusMinutes(5));
        // конвертируем её в JSON
        String epicJson2 = gson.toJson(epic2);

        HttpRequest postRequest2 = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson2)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, postResponse2.statusCode());

        // создаём HTTP-клиент и запрос
        HttpRequest getRequest = HttpRequest.newBuilder().uri(epicUrl).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, getResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject2 = jsonArray.get(1).getAsJsonObject();

        assertEquals(epic.getName(), jsonObject.get("name").getAsString());
        assertEquals(epic2.getName(), jsonObject2.get("name").getAsString());
        assertEquals(2, jsonArray.size());

    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");

        // конвертируем её в JSON
        epic.setStartTime(LocalDateTime.now());
        epic.setEndTime(LocalDateTime.now().plusMinutes(5));
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, epicResponse.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();
        int taskId = tasksFromManager.getFirst().getId();

        URI urlGetTaskById = URI.create("http://localhost:8080/epics" + "/" + taskId);

        // создаём HTTP-клиент и запрос
        HttpRequest getRequest = HttpRequest.newBuilder().uri(urlGetTaskById).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, getResponse.statusCode());

        taskId = 404;
        URI urlGetTaskById404 = URI.create("http://localhost:8080/epics" + "/" + taskId);
        HttpRequest getRequest404 = HttpRequest.newBuilder().uri(urlGetTaskById404).GET().build();
        HttpResponse<String> getResponse404 = client.send(getRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse404.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(epic.getName(), jsonObject.get("name").getAsString());
    }

    @Test
    public void testGetEpicSubtasksById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test epic", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test subtask", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now(), 1);
        // конвертируем её в JSON
        epic.setStartTime(subtask.getStartTime());
        epic.setEndTime(epic.getStartTime().plusMinutes(subtask.getDuration()));
        String epicJson = gson.toJson(epic);
        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, epicResponse.statusCode());


        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();
        int taskId = tasksFromManager.getFirst().getId();

        URI urlGetEpicSubtasksById = URI.create("http://localhost:8080/epics" + "/" + taskId + "/" + "subtasks");

        // создаём HTTP-клиент и запрос
        HttpRequest getRequest = HttpRequest.newBuilder().uri(urlGetEpicSubtasksById).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, getResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();

        assertEquals(subtask.getName(), jsonObject.get("name").getAsString());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Test", "Testing epic");

        // конвертируем её в JSON
        epic.setStartTime(LocalDateTime.now());
        epic.setEndTime(LocalDateTime.now().plusMinutes(5));
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, epicResponse.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpics();
        int taskId = tasksFromManager.getFirst().getId();

        URI urlDeleteEpicById = URI.create("http://localhost:8080/epics" + "/" + taskId);

        // создаём HTTP-клиент и запрос
        HttpRequest deleteRequest = HttpRequest.newBuilder().uri(urlDeleteEpicById).DELETE().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, deleteResponse.statusCode());

        taskId = 404;
        URI urlDeleteEpic404 = URI.create("http://localhost:8080/epics" + "/" + taskId);
        HttpRequest deleteRequest404 = HttpRequest.newBuilder().uri(urlDeleteEpic404).DELETE().build();
        HttpResponse<String> deleteResponse404 = client.send(deleteRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, deleteResponse404.statusCode());

        List<Epic> tasksFromManagerAtferDelete = manager.getAllEpics();

        assertEquals(0, tasksFromManagerAtferDelete.size());
    }
}

