package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @AfterEach
    void clear() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }

    static TaskManager taskManager = Managers.getDefault();

    @Test
    void addNewTask() {
        Task task = new Task("Задача", "Описание");
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
        Task task1 = new Task("Задача 1", "Описание 1");
        final int task1Id = taskManager.createTask(task1).getId();

        Task task2 = new Task("Задача 2", "Описание 2");
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

        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", epicId);
        final int subtaskId = taskManager.createSubtask(subtask).getId();
        subtask = new Subtask("Test", "Test", subtaskId);

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
        Task task = new Task("Задача", "Описание");
        int taskId = taskManager.createTask(task).getId();
        Task taskDuplicate = new Task("Задача", "Описание");
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
        Task task = new Task("Задача", "Описание");
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

        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", epicId);
        taskManager.createSubtask(subtask);
        Subtask subtask1 = new Subtask("Сабтаска1", "Описание сабтаски 1", epicId);
        taskManager.createSubtask(subtask1);

        assertFalse(taskManager.getAllSubtasks().isEmpty());

        taskManager.deleteEpic(epicId);
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

}