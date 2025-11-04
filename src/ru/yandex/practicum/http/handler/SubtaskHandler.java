package ru.yandex.practicum.http.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.Subtask;
import ru.yandex.practicum.TaskManager;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.OverlapsException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        Endpoint endpoint = getEndpoint(h.getRequestURI().getPath(), h.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASKS -> handleGetSubtasks(h);
            case GET_SUBTASK_BY_ID -> handleGetSubtaskById(h);
            case POST_SUBTASK -> handlePostSubtask(h);
            case DELETE_SUBTASK -> handleDeleteSubtask(h);
            case UNKNOWN -> sendNotFound(h, "Такого эндпоинта не существует");
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        String response = gson.toJson(taskManager.getAllSubtasks());
        sendText(exchange, response);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskIdOpt = getSubtaskId(exchange);

        if (subtaskIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера подзадачи");
            return;
        }

        int subtaskId = subtaskIdOpt.get();

        try {
            String response = gson.toJson(taskManager.getSubtask(subtaskId));
            sendText(exchange, response);
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Подзадача с таким ID не найдена");
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        if (body.isBlank()) {
            return;
        }

        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask.getId() == 0) {
            try {
                taskManager.createSubtask(subtask);
                exchange.sendResponseHeaders(201, 0);
                exchange.close();
            } catch (OverlapsException e) {
                sendHasOverlaps(exchange, "Подзадача имеет пересечение по времени");
            } catch (IllegalArgumentException e) {
                exchange.sendResponseHeaders(500,0);
                exchange.close();
            }
        } else {
            try {
                taskManager.updateSubtask(subtask);
                exchange.sendResponseHeaders(201, 0);
                exchange.close();
            } catch (OverlapsException e) {
                sendHasOverlaps(exchange, "Подзадача имеет пересечение по времени");
            } catch (NotFoundException e) {
                sendNotFound(exchange, "Подзадача с таким ID не найдена");
            }
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> subtaskIdOpt = getSubtaskId(exchange);

        if (subtaskIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера подзадачи");
            return;
        }

        int subtaskId = subtaskIdOpt.get();

        try {
            taskManager.deleteSubtask(subtaskId);
            sendText(exchange, "Подзадача успешно удалена");
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Подзадача с таким ID не найдена");
        }
    }

    private Optional<Integer> getSubtaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_SUBTASK;
            }
        }

        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    enum Endpoint {
        GET_SUBTASKS,
        GET_SUBTASK_BY_ID,
        POST_SUBTASK,
        DELETE_SUBTASK,
        UNKNOWN
    }
}
