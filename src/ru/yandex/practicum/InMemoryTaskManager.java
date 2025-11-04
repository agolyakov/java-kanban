package ru.yandex.practicum;

import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.OverlapsException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Task::compareTo);

    // Создание задач
    @Override
    public Task createTask(Task task) {
        if (isCrossTasks(task)) {
            throw new OverlapsException("Задача имеет пересечение по времени");
        }

        int id = task.getId();
        if (id == 0) {
            task = setNextId(task);
            id = task.getId();
        }

        if (tasks.containsKey(id)) {
            throw new IllegalArgumentException("Задача с таким ID уже существует.");
        }

        tasks.put(id, task);
        updateNextId(id);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = epic.getId();
        if (id == 0) {
            epic = setNextId(epic);
            id = epic.getId();
        }

        if (epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпик с таким ID уже существует.");
        }

        epics.put(id, epic);
        updateNextId(id);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (isCrossTasks(subtask)) {
            throw new OverlapsException("Подзадача имеет пересечение по времени");
        }

        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпика не существует");
        }

        int id = subtask.getId();
        if (id == 0) {
            subtask = setNextId(subtask);
            id = subtask.getId();
        }

        if (subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Подзадача с таким ID уже существует.");
        }

        subtasks.put(id, subtask);

        epics.get(subtask.getEpicId()).addSubtaskId(id);
        updateEpicStatus(subtask.getEpicId());
        updateEpicTimes(subtask.getEpicId());
        updateNextId(id);
        return subtask;
    }

    // Обновление задач
    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new NotFoundException("Задача с таким ID не существует.");
        }

        if (isCrossTasks(task)) {
            throw new OverlapsException("Задача имеет пересечение по времени");
        }

        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NotFoundException("Эпик с таким ID не существует.");
        }

        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NotFoundException("Подзадача с таким ID не существует.");
        }

        if (isCrossTasks(subtask)) {
            throw new OverlapsException("Подзадача имеет пересечение по времени");
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
        updateEpicTimes(subtask.getEpicId());
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        });
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        });
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
        });

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        });
        subtasks.clear();


        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTimes(epic.getId());
        });
    }

    // Получение задач по ID
    @Override
    public Task getTask(int id) {
        if (!tasks.containsKey(id)) {
            throw new NotFoundException("Задача с таким ID не существует");
        }

        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new NotFoundException("Эпик с таким ID не существует");
        }

        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new NotFoundException("Подзадача с таким ID не существует");
        }

        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    // Удаление по ID
    @Override
    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) {
            throw new NotFoundException("Задача с таким ID не существует");
        }

        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new NotFoundException("Эпик с таким ID не существует");
        }

        Epic epic = epics.get(id);

        epic.getSubtaskIds().forEach(subtaskId -> {
            historyManager.remove(subtaskId);
            prioritizedTasks.remove(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
        });

        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new NotFoundException("Подзадача с таким ID не существует");
        }

        prioritizedTasks.remove(subtasks.get(id));
        Subtask subtask = subtasks.get(id);

        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtaskId(id);
        updateEpicStatus(epic.getId());
        updateEpicTimes(epic.getId());

        subtasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        prioritizedTasks.addAll(tasks.values()
                .stream()
                .filter(task -> task.getStartTime() != null)
                .toList());
        prioritizedTasks.addAll(subtasks.values()
                .stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .toList());

        return prioritizedTasks;
    }

    private boolean isCrossTasks(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }

        return getPrioritizedTasks().stream()
                .anyMatch(newTask::crossTasks);

    }

    // Получение списков задач
    @Override
    public List<Task> getAllTasks() {
        tasks.values().forEach(historyManager::add);
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        epics.values().forEach(historyManager::add);
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        subtasks.values().forEach(historyManager::add);
        return new ArrayList<>(subtasks.values());
    }

    // Получение подзадач эпика
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private void updateNextId(int value) {
        if (value > nextId) nextId = value;
    }

    private <T extends Task> T setNextId(T obj) {
        findMaxId();
        obj.setId(++nextId);
        return obj;
    }

    private void findMaxId() {
        int maxId = 0;

        if (!epics.isEmpty()) {
            maxId = Math.max(maxId, Collections.max(epics.keySet()));
        }
        if (!subtasks.isEmpty()) {
            maxId = Math.max(maxId, Collections.max(subtasks.keySet()));
        }
        if (!tasks.isEmpty()) {
            maxId = Math.max(maxId, Collections.max(tasks.keySet()));
        }

        nextId = maxId;
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

    private void updateEpicTimes(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        epic.setDuration(
                Duration.of(
                        subtaskIds.stream()
                                .map(id -> subtasks.get(id).getDuration())
                                .mapToLong(Long::longValue).sum(), ChronoUnit.MINUTES));

        epic.setStartTime(
                subtaskIds.stream()
                        .map(id -> subtasks.get(id).getStartTime())
                        .min(Comparator.naturalOrder()).orElse(null));

        epic.setEndTime(epic.getStartTime().plusMinutes(epic.getDuration()));
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}
