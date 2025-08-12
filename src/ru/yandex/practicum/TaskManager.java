package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int nextId = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Создание задач
    public Task createTask(Task task) {
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) throws Exception {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new Exception();
        }
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    // Обновление задач
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic != null) {
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    public void updateSubtask(Subtask subtask) {
        Subtask savedSubtask = subtasks.get(subtask.getId());
        if (savedSubtask != null) {
            savedSubtask.setName(subtask.getName());
            savedSubtask.setDescription(subtask.getDescription());
            savedSubtask.setStatus(subtask.getStatus());
            updateEpicStatus(savedSubtask.getEpicId());
        }
    }

    // Удаление всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    // Получение задач по ID
    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    // Удаление по ID
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    // Получение списков задач
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получение подзадач эпика
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        ArrayList<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    // Обновление статуса эпика
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (int subtaskId : subtaskIds) {
            Status status = subtasks.get(subtaskId).getStatus();
            if (status != Status.DONE) allDone = false;
            if (status != Status.NEW) allNew = false;
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
