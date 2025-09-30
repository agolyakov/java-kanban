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
        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(history.getFirst(), task);

        taskManager.getTask(taskTwo.getId());
        history = taskManager.getHistory();

        assertEquals(history.get(0), task);
        assertEquals(history.get(1), taskTwo);
    }

    @Test
    void shouldEmptyHistoryAfterRemoveAllTasks() throws Exception {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty());

        historyManager.remove(task.getId());
        history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldEmptyHistoryAfterDeleteTaskInTaskManager() throws Exception {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();
        assertFalse(history.isEmpty());

        taskManager.deleteTask(task.getId());
        history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }

}
