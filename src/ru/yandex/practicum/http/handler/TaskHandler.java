package ru.yandex.practicum.http.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.Task;
import ru.yandex.practicum.TaskManager;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.OverlapsException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        Endpoint endpoint = getEndpoint(h.getRequestURI().getPath(), h.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS -> handleGetTasks(h);
            case GET_TASK_BY_ID -> handleGetTaskById(h);
            case POST_TASK -> handlePostTask(h);
            case DELETE_TASK -> handleDeleteTask(h);
            case UNKNOWN -> sendNotFound(h, "Такого эндпоинта не существует");
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        String response = gson.toJson(taskManager.getAllTasks());
        sendText(exchange, response);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);

        if (taskIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера задачи");
            return;
        }

        int taskId = taskIdOpt.get();

        try {
            String response = gson.toJson(taskManager.getTask(taskId));
            sendText(exchange, response);
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Задача с таким ID не найдена");
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        if (body.isBlank()) {
            return;
        }

        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == 0) {
            try {
                taskManager.createTask(task);
                exchange.sendResponseHeaders(201, 0);
                exchange.close();
            } catch (OverlapsException e) {
                sendHasOverlaps(exchange, "Задача имеет пересечение по времени");
            }
        } else {
            try {
                taskManager.updateTask(task);
                exchange.sendResponseHeaders(201, 0);
                exchange.close();
            } catch (OverlapsException e) {
                sendHasOverlaps(exchange, "Задача имеет пересечение по времени");
            }
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);

        if (taskIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера задачи");
            return;
        }

        int taskId = taskIdOpt.get();

        try {
            taskManager.deleteTask(taskId);
            sendText(exchange, "Задача успешно удалена");
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Задача с таким ID не найдена");
        }
    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        }

        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    enum Endpoint {
        GET_TASKS,
        GET_TASK_BY_ID,
        POST_TASK,
        DELETE_TASK,
        UNKNOWN
    }
}
