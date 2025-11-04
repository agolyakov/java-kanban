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

public class PrioritizedHandlerTest {

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

    public PrioritizedHandlerTest() throws IOException {
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
    public void testGetPrioritized() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test task", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest requestTask = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(requestTask, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        Epic epic = new Epic("Test epic", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test subtask", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(5), 2);
        // конвертируем её в JSON
        epic.setStartTime(subtask.getStartTime());
        epic.setEndTime(epic.getStartTime().plusMinutes(subtask.getDuration()));
        String epicJson = gson.toJson(epic);
        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, epicResponse.statusCode());

        URI subtaskUrl = URI.create("http://localhost:8080/subtasks");
        HttpRequest subtaskRequest = HttpRequest.newBuilder().uri(subtaskUrl).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, subtaskResponse.statusCode());

        List<Task> tasks = manager.getAllTasks();
        List<Subtask> subtasks = manager.getAllSubtasks();
        List<Epic> epics = manager.getAllEpics();

        // создаем запрос для истории
        URI urlPrioritized = URI.create("http://localhost:8080/prioritized");
        HttpRequest requestPrioritized = HttpRequest.newBuilder().uri(urlPrioritized).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> responsePrioritized = client.send(requestPrioritized, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, responsePrioritized.statusCode());

        JsonElement jsonElement = JsonParser.parseString(responsePrioritized.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject2 = jsonArray.get(1).getAsJsonObject();

        assertEquals(task.getName(), jsonObject.get("name").getAsString());
        assertEquals(subtask.getName(), jsonObject2.get("name").getAsString());
        assertEquals(2, jsonArray.size());
    }

}
