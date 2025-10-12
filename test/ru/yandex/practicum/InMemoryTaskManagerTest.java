package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    static TaskManager taskManager = Managers.getDefault();

    @AfterEach
    void clear() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }

    @Test
    void createTaskAnyTypeAndSearchById() {
        Task task = new Task("Задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание эпика");
        int taskId = taskManager.createTask(task).getId();
        int epicId = taskManager.createEpic(epic).getId();
        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", epic.getId());
        int subtaskId = taskManager.createSubtask(subtask).getId();

        assertNotNull(taskManager.getTask(taskId));
        assertEquals(task, taskManager.getTask(taskId));

        assertNotNull(taskManager.getEpic(epicId));
        assertEquals(epic, taskManager.getEpic(epicId));

        assertNotNull(taskManager.getSubtask(subtaskId));
        assertEquals(subtask, taskManager.getSubtask(subtaskId));
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("Задача1", "Описание");
        Task task2 = new Task("Задача2", "Описание");
        Task task3 = new Task("Задача3", "Описание");
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);

        assertNotNull(taskManager.getAllTasks());
        assertEquals(tasks, taskManager.getAllTasks());
    }

    @Test
    void deleteAllTasks() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);

        assertNotNull(taskManager.getAllTasks());
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void updateTask() {
        Task task = new Task("Задача", "Описание");
        int taskId = taskManager.createTask(task).getId();

        Task newTask = new Task("Новая задача", "Описание");
        newTask.setId(taskId);
        taskManager.updateTask(newTask);

        assertEquals(newTask, taskManager.getTask(taskId));
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Задача", "Описание");
        int taskId = taskManager.createTask(task).getId();
        assertNotNull(taskManager.getAllTasks());
        assertTrue(taskManager.getAllTasks().contains(task));

        taskManager.deleteTask(taskId);
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void updateEpicStatusWhenSubtaskStatusUpdate() {
        Epic epic = new Epic("Эпик", "Описание");
        int epicId = taskManager.createEpic(epic).getId();

        assertEquals(Status.NEW, taskManager.getEpic(epicId).getStatus());

        Subtask subtaskNew = new Subtask("Сабтаска", "Описание", epicId);
        taskManager.createSubtask(subtaskNew);

        assertEquals(Status.NEW, taskManager.getEpic(epicId).getStatus());

        Subtask subtaskInProgress = new Subtask("Сабтаска", "Описание", epicId);
        subtaskInProgress.setStatus(Status.IN_PROGRESS);
        taskManager.createSubtask(subtaskInProgress);
        Subtask subtaskDone = new Subtask("Сабтаска", "Описание", epicId);
        subtaskDone.setStatus(Status.DONE);
        taskManager.createSubtask(subtaskDone);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());

        subtaskNew.setStatus(Status.DONE);
        subtaskInProgress.setStatus(Status.DONE);
        taskManager.updateSubtask(subtaskNew);
        taskManager.updateSubtask(subtaskInProgress);

        assertEquals(Status.DONE, taskManager.getEpic(epicId).getStatus());
    }

}
