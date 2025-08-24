package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    // Создание задач
    Task createTask(Task task) throws Exception;

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask) throws Exception;

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
    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getAllSubtasks();

    // Получение подзадач эпика
    ArrayList<Subtask> getEpicSubtasks(int epicId);

    // Обновление статуса эпика
    void updateEpicStatus(int epicId);

    List<Task> getHistory();
}
