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

public class SubtaskHandlerTest {

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

    public SubtaskHandlerTest() throws IOException {
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
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test", "Testing task",
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

        Subtask subtask406 = new Subtask("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now(), 1);

        String subtaskJson406 = gson.toJson(subtask406);
        HttpRequest request406 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson406)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response406 = client.send(request406, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response406.statusCode());

        Subtask subtask500 = new Subtask("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(5), 500);

        String subtaskJson500 = gson.toJson(subtask500);
        HttpRequest request500 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson500)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response500 = client.send(request500, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(500, response500.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Subtask> subtasksFromManager = manager.getAllSubtasks();

        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", subtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test", "Testing task",
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

        List<Subtask> tasksFromManager = manager.getAllSubtasks();

        Subtask updTask = new Subtask("Test update", "Testing update task",
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(5), 1);
        updTask.setId(tasksFromManager.get(0).getId());

        String updTaskJson = gson.toJson(updTask);

        HttpRequest updRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updTaskJson)).build();
        HttpResponse<String> updResponse = client.send(updRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updResponse.statusCode());

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response406 = client.send(updRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response406.statusCode());

        updTask.setId(404);

        updTaskJson = gson.toJson(updTask);

        HttpRequest updRequest404 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updTaskJson)).build();
        HttpResponse<String> updResponse404 = client.send(updRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, updResponse404.statusCode());

        List<Subtask> updTasksFromManager = manager.getAllSubtasks();

        assertEquals("Test update", updTasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test", "Testing task",
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

        // создаём задачу
        Subtask subtask2 = new Subtask("Test", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(5), 1);
        // конвертируем её в JSON
        String subtaskJson2 = gson.toJson(subtask2);

        HttpRequest postRequest2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson2)).build();

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

        assertEquals(subtask.getName(), jsonObject.get("name").getAsString());
        assertEquals(subtask2.getName(), jsonObject2.get("name").getAsString());
        assertEquals(2, jsonArray.size());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test", "Testing task",
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

        List<Subtask> tasksFromManager = manager.getAllSubtasks();
        int subtaskId = tasksFromManager.getFirst().getId();

        URI urlGetSubtaskById = URI.create("http://localhost:8080/subtasks" + "/" + subtaskId);

        // создаём HTTP-клиент и запрос
        HttpRequest getRequest = HttpRequest.newBuilder().uri(urlGetSubtaskById).GET().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, getResponse.statusCode());

        subtaskId = 404;
        URI urlGetSubtaskById404 = URI.create("http://localhost:8080/subtasks" + "/" + subtaskId);
        HttpRequest getRequest404 = HttpRequest.newBuilder().uri(urlGetSubtaskById404).GET().build();
        HttpResponse<String> getResponse404 = client.send(getRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse404.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(subtask.getName(), jsonObject.get("name").getAsString());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic");
        // создаём задачу
        Subtask subtask = new Subtask("Test", "Testing task",
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

        List<Subtask> tasksFromManager = manager.getAllSubtasks();
        int subtaskId = tasksFromManager.getFirst().getId();

        URI urlDeleteSubtask = URI.create("http://localhost:8080/subtasks" + "/" + subtaskId);

        // создаём HTTP-клиент и запрос
        HttpRequest deleteRequest = HttpRequest.newBuilder().uri(urlDeleteSubtask).DELETE().build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, deleteResponse.statusCode());

        subtaskId = 404;
        URI urlDeleteSubtask404 = URI.create("http://localhost:8080/subtasks" + "/" + subtaskId);
        HttpRequest deleteRequest404 = HttpRequest.newBuilder().uri(urlDeleteSubtask404).DELETE().build();
        HttpResponse<String> deleteResponse404 = client.send(deleteRequest404, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, deleteResponse404.statusCode());

        List<Subtask> tasksFromManagerAtferDelete = manager.getAllSubtasks();

        assertEquals(0, tasksFromManagerAtferDelete.size());
    }
}

