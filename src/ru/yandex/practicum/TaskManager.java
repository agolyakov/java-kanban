package ru.yandex.practicum;

import java.util.List;
import java.util.TreeSet;

public interface TaskManager {
    // Создание задач
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    // Обновление задач
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    // Удаление всех задач
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    // Получение задач по ID
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    // Удаление по ID
    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // Получение списков задач
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    TreeSet<Task> getPrioritizedTasks();

    // Получение подзадач эпика
    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();
}
