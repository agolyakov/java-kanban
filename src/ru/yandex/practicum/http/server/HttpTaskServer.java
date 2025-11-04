package ru.yandex.practicum.http.server;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.Managers;
import ru.yandex.practicum.TaskManager;
import ru.yandex.practicum.http.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer httpServer;
    private TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);

        server.start();
    }

    public void start() {
        httpServer.createContext("/tasks", new TaskHandler(manager));
        httpServer.createContext("/subtasks", new SubtaskHandler(manager));
        httpServer.createContext("/epics", new EpicHandler(manager));
        httpServer.createContext("/history", new HistoryHandler(manager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager));

        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

}
