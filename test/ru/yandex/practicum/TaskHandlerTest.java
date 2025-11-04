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

public class TaskHandlerTest {

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

    public TaskHandlerTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response406 = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response406.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());

        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        Task updTask = new Task("Test update", "Testing update task",
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(5));
        updTask.setId(tasksFromManager.get(0).getId());

        String updTaskJson = gson.toJson(updTask);

        HttpRequest updRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updTaskJson)).build();
        HttpResponse<String> updResponse = client.send(updRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updResponse.statusCode());

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response406 = client.send(updRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response406.statusCode());

        List<Task> updTasksFromManager = manager.getAllTasks();

        assertEquals("Test update", updTasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest postRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, postResponse.statusCode());

        // создаём задачу
        Task task2 = new Task("Test 2", "Testing task 2",
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(5));
        // конвертируем её в JSON
        String taskJson2 = gson.toJson(task2);

        HttpRequest postRequest2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson2)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, postResponse2.statusCode());

        // создаём HTTP-клиент и запрос
        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, getResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject2 = jsonArray.get(1).getAsJsonObject();

        assertEquals(task.getName(), jsonObject.get("name").getAsString());
        assertEquals(task2.getName(), jsonObject2.get("name").getAsString());
        assertEquals(2, jsonArray.size());

    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        int taskId = tasksFromManager.getFirst().getId();

        URI urlGetTaskById = URI.create("http://localhost:8080/tasks" + "/" + taskId);

        // создаём HTTP-клиент и запрос
        HttpRequest getRequest = HttpRequest.newBuilder().uri(urlGetTaskById).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, getResponse.statusCode());

        taskId = 404;
        URI urlGetTaskById404 = URI.create("http://localhost:8080/tasks" + "/" + taskId);
        HttpRequest getRequest404 = HttpRequest.newBuilder().uri(urlGetTaskById404).GET().build();
        HttpResponse<String> getResponse404 = client.send(getRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse404.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(task.getName(), jsonObject.get("name").getAsString());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        int taskId = tasksFromManager.getFirst().getId();

        URI urlDeleteTask = URI.create("http://localhost:8080/tasks" + "/" + taskId);

        // создаём HTTP-клиент и запрос
        HttpRequest deleteRequest = HttpRequest.newBuilder().uri(urlDeleteTask).DELETE().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, deleteResponse.statusCode());

        taskId = 404;
        URI urlDeleteTask404 = URI.create("http://localhost:8080/tasks" + "/" + taskId);
        HttpRequest deleteRequest404 = HttpRequest.newBuilder().uri(urlDeleteTask404).DELETE().build();
        HttpResponse<String> deleteResponse404 = client.send(deleteRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, deleteResponse404.statusCode());

        List<Task> tasksFromManagerAtferDelete = manager.getAllTasks();

        assertEquals(0, tasksFromManagerAtferDelete.size());
    }
}
