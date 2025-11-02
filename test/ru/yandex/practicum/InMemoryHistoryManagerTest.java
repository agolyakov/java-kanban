package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class InMemoryHistoryManagerTest {

    static TaskManager taskManager = Managers.getDefault();
    static HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    void savePreviousDataHistory() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), LocalDateTime.of(2025, 10, 19, 20, 15));
        Task taskTwo = new Task("Задача 2", "Описание 2", Duration.ofMinutes(5), LocalDateTime.of(2025, 10, 19, 20, 20));
        taskManager.createTask(task);
        taskManager.createTask(taskTwo);
        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(history.getFirst(), task);

        taskManager.getTask(taskTwo.getId());
        history = taskManager.getHistory();

        assertEquals(history.get(0), task);
        assertEquals(history.get(1), taskTwo);
    }

    @Test
    void shouldEmptyHistoryAfterRemoveAllTasks() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), LocalDateTime.of(2025, 10, 19, 20, 25));
        taskManager.createTask(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty());

        historyManager.remove(task.getId());
        history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldEmptyHistoryAfterDeleteTaskInTaskManager() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), LocalDateTime.of(2025, 10, 19, 20, 15));
        taskManager.createTask(task);
        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();
        assertFalse(history.isEmpty());

        taskManager.deleteTask(task.getId());
        history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }

}
