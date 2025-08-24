package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {
    @Test
    void initializeManagers() throws Exception {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

        assertNotNull(tasks, "После создания задачи, список задач не должен быть пустой.");
        assertEquals(1, tasks.size(), "После создания задачи, список задач не должен быть пустой.");
    }
}
