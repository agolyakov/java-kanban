package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class InMemoryHistoryManagerTest {

    static TaskManager taskManager = Managers.getDefault();
    static HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    void savePreviousDataHistory() throws Exception {
        Task task = new Task("Задача", "Описание");
        Task taskTwo = new Task("Задача 2", "Описание 2");
        taskManager.createTask(task);
        taskManager.createTask(taskTwo);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(history.getFirst(), task);

        historyManager.add(taskTwo);

        assertEquals(history.get(0), task);
        assertEquals(history.get(1), taskTwo);
    }
}
