package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

abstract class TaskManagerTest<T extends TaskManager> {

    static TaskManager taskManager = Managers.getDefault();

    @AfterEach
    void clear() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }

    @Test
    void createTaskAnyTypeAndSearchById() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        Epic epic = new Epic("Эпик", "Описание эпика");
        int taskId = taskManager.createTask(task).getId();
        int epicId = taskManager.createEpic(epic).getId();
        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", Duration.ofMinutes(5), getRandomLocalDate(), epic.getId());
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
        Task task1 = new Task("Задача1", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        Task task2 = new Task("Задача2", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        Task task3 = new Task("Задача3", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
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
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        taskManager.createTask(task);

        assertNotNull(taskManager.getAllTasks());
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void updateTask() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        int taskId = taskManager.createTask(task).getId();

        Task newTask = new Task("Новая задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        newTask.setId(taskId);
        taskManager.updateTask(newTask);

        assertEquals(newTask, taskManager.getTask(taskId));
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
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

        Subtask subtaskNew = new Subtask("Сабтаска", "Описание", Duration.ofMinutes(5), getRandomLocalDate(), epicId);
        taskManager.createSubtask(subtaskNew);

        assertEquals(Status.NEW, taskManager.getEpic(epicId).getStatus());

        Subtask subtaskInProgress = new Subtask("Сабтаска", "Описание", Duration.ofMinutes(5), getRandomLocalDate(), epicId);
        subtaskInProgress.setStatus(Status.IN_PROGRESS);
        taskManager.createSubtask(subtaskInProgress);
        Subtask subtaskDone = new Subtask("Сабтаска", "Описание", Duration.ofMinutes(5), getRandomLocalDate(), epicId);
        subtaskDone.setStatus(Status.DONE);
        taskManager.createSubtask(subtaskDone);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).getStatus());

        Subtask updateSubtaskNew = new Subtask(subtaskNew.getName(),subtaskNew.getDescription(),Duration.ofMinutes(5),getRandomLocalDate(),subtaskNew.getEpicId());
        Subtask updateSubtaskInProgress = new Subtask(subtaskInProgress.getName(),subtaskInProgress.getDescription(),Duration.ofMinutes(5),getRandomLocalDate(),subtaskInProgress.getEpicId());
        updateSubtaskNew.setId(subtaskNew.getId());
        updateSubtaskNew.setStatus(Status.DONE);
        updateSubtaskInProgress.setId(subtaskInProgress.getId());
        updateSubtaskInProgress.setStatus(Status.DONE);
        taskManager.updateSubtask(updateSubtaskNew);
        taskManager.updateSubtask(updateSubtaskInProgress);

        assertEquals(Status.DONE, taskManager.getEpic(epicId).getStatus());
    }

    @Test
    void addNewTask() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        final int taskId = taskManager.createTask(task).getId();

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void shouldEqualsObjectIfIdEquals() {
        Task task1 = new Task("Задача 1", "Описание 1", Duration.ofMinutes(5), getRandomLocalDate());
        final int task1Id = taskManager.createTask(task1).getId();

        Task task2 = new Task("Задача 2", "Описание 2", Duration.ofMinutes(5), getRandomLocalDate());
        taskManager.createTask(task2).setId(task1Id);

        assertEquals(task1, task2, "Задачи не совпадают.");
    }

    @Test
    void shouldEqualsExtendObjectIfIdEquals() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        final int epic1Id = taskManager.createEpic(epic1).getId();

        Epic epic2 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic2).setId(epic1Id);

        assertEquals(epic1, epic2, "Эпики не совпадают.");
    }

    @Test
    void shouldThrowExceptionWhenAddingEpicAsSubtask() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        final int epicId = taskManager.createEpic(epic).getId();

        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", Duration.ofMinutes(5), getRandomLocalDate(), epicId);
        final int subtaskId = taskManager.createSubtask(subtask).getId();
        subtask = new Subtask("Test", "Test", Duration.ofMinutes(5), getRandomLocalDate(), subtaskId);

        // Попытка добавить эпик в качестве сабтаска, которая должна вызвать исключение
        Subtask finalSubtask = subtask;
        try {
            taskManager.createSubtask(finalSubtask);
            fail("Ожидалось исключение Exception");
        } catch (Exception e) {
            assertEquals("Эпика не существует", e.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenCreateTaskWithDuplicateId() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        int taskId = taskManager.createTask(task).getId();
        Task taskDuplicate = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        taskDuplicate.setId(taskId);
        try {
            taskManager.createTask(taskDuplicate);
            fail("Ожидалось исключение Exception");
        } catch (Exception e) {
            assertEquals("Задача с таким ID уже существует.", e.getMessage());
        }
    }

    @Test
    void shouldNotChangeTaskAfterCreateInManager() {
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(5), getRandomLocalDate());
        int taskId = taskManager.createTask(task).getId();

        assertEquals(task.getName(), taskManager.getTask(taskId).getName());
        assertEquals(task.getDescription(), taskManager.getTask(taskId).getDescription());
        assertEquals(task.getStatus(), taskManager.getTask(taskId).getStatus());
        assertEquals(task.getId(), taskManager.getTask(taskId).getId());
    }

    @Test
    void shouldEmptySubtasksAfterDeleteEpic() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        final int epicId = taskManager.createEpic(epic).getId();

        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", Duration.ofMinutes(5), getRandomLocalDate(), epicId);
        taskManager.createSubtask(subtask);
        Subtask subtask1 = new Subtask("Сабтаска1", "Описание сабтаски 1", Duration.ofMinutes(5), getRandomLocalDate(), epicId);
        taskManager.createSubtask(subtask1);

        assertFalse(taskManager.getAllSubtasks().isEmpty());

        taskManager.deleteEpic(epicId);
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreateSubtaskWithUnknownEpic() {
        assertThrows(IllegalArgumentException.class, () -> {
            Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", Duration.ofMinutes(5), getRandomLocalDate(), 1);
            taskManager.createSubtask(subtask);
        }, "Создание сабтаски без существующего эпика должно приводить к исключению");
    }

    private LocalDateTime getRandomLocalDate() {
        long startEpochSecond = LocalDateTime.of(2020, 1, 1, 0, 0).atZone(java.time.ZoneOffset.UTC).toEpochSecond();
        long endEpochSecond = LocalDateTime.of(2025, 12, 31, 23, 59).atZone(java.time.ZoneOffset.UTC).toEpochSecond();

        long randomEpochSecond = ThreadLocalRandom.current()
                .nextLong(startEpochSecond, endEpochSecond);

        return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, java.time.ZoneOffset.UTC);
    }
}
