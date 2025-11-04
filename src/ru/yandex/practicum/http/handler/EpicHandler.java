package ru.yandex.practicum.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.Epic;
import ru.yandex.practicum.TaskManager;
import ru.yandex.practicum.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        Endpoint endpoint = getEndpoint(h.getRequestURI().getPath(), h.getRequestMethod());

        switch (endpoint) {
            case GET_EPICS -> handleGetEpics(h);
            case GET_EPIC_BY_ID -> handleGetEpicById(h);
            case GET_EPIC_SUBTASKS -> handleGetEpicSubtasks(h);
            case POST_EPIC -> handlePostEpic(h);
            case DELETE_EPIC -> handleDeleteEpic(h);
            case UNKNOWN -> sendNotFound(h, "Такого эндпоинта не существует");
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        String response = gson.toJson(taskManager.getAllEpics());
        sendText(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getEpicId(exchange);

        if (epicIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера эпика");
            return;
        }

        int epicId = epicIdOpt.get();

        try {
            String response = gson.toJson(taskManager.getEpic(epicId));
            sendText(exchange, response);
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Эпик с таким ID не найден");
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getEpicId(exchange);

        if (epicIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера эпика");
            return;
        }

        int epicId = epicIdOpt.get();

        String response = gson.toJson(taskManager.getEpicSubtasks(epicId));
        sendText(exchange, response);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        if (body.isBlank()) {
            return;
        }

        Epic epic = gson.fromJson(body, Epic.class);

        try {
            taskManager.createEpic(epic);
            exchange.sendResponseHeaders(201, 0);
            exchange.close();
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(500,0);
            exchange.close();
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOpt = getEpicId(exchange);

        if (epicIdOpt.isEmpty()) {
            sendNotFound(exchange, "Получен неизвестный формат номера эпика");
            return;
        }

        int epicId = epicIdOpt.get();

        try {
            taskManager.deleteEpic(epicId);
            sendText(exchange, "Эпик успешно удален");
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Эпик с таким ID не найден");
        }
    }

    private Optional<Integer> getEpicId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_EPIC;
            }
        }

        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_EPIC;
            }
        }

        if (pathParts.length == 4 &&
                pathParts[1].equals("epics") &&
                pathParts[3].equals("subtasks") &&
                requestMethod.equals("GET")) {
            return Endpoint.GET_EPIC_SUBTASKS;
        }

        return Endpoint.UNKNOWN;
    }

    enum Endpoint {
        GET_EPICS,
        GET_EPIC_SUBTASKS,
        GET_EPIC_BY_ID,
        POST_EPIC,
        DELETE_EPIC,
        UNKNOWN
    }
}
