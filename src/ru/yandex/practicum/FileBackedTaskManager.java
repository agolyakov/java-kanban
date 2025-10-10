package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {

    File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String header = fileReader.readLine(); // Первая строка заголовок в csv
            if (header == null || !header.startsWith("id,type,name,status,description,epic")) {
                throw new ManagerLoadException("Файл имеет неверный формат.");
            }

            String line;
            while ((line = fileReader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }

            for (String s : lines) {
                Task task = manager.fromString(s);
                manager.addTaskFromLoad(task);
            }

        } catch (IOException e) {
            throw new ManagerLoadException("Произошла ошибка во время чтения файла.");
        }

        return manager;
    }

    Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            throw new ManagerLoadException("Неверный формат строки: " + value);
        }

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];
        int epicId = -1;
        if (parts.length > 5) {
            epicId = Integer.parseInt(parts[5]);
        }

        switch (type) {
            case "TASK":
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(Status.valueOf(status));
                return task;

            case "EPIC":
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(Status.valueOf(status));
                return epic;

            case "SUBTASK":
                if (epicId == -1) {
                    throw new IllegalArgumentException("У подзадачи отсутствует ID эпика.");
                }
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(Status.valueOf(status));
                return subtask;

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private <T extends Task> void addTaskFromLoad(T task) {
        if (task instanceof Epic epic) {
            super.createEpic(epic);
        } else if (task instanceof Subtask subtask) {
            super.createSubtask(subtask);
        } else {
            super.createTask(task);
        }
    }

    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    public void save() {
        try (BufferedWriter fileWriter = new BufferedWriter(
                new FileWriter(file.getPath(), StandardCharsets.UTF_8))) {

            fileWriter.write("id,type,name,status,description,epic\n");

            // Добавляем в один список и записываем все задачи, эпики и подзадачи
            List<Task> allTasks = new ArrayList<>();
            allTasks.addAll(getAllTasks());
            allTasks.addAll(getAllEpics());
            allTasks.addAll(getAllSubtasks());

            for (Task task: allTasks) {
                fileWriter.write(task.toString(task) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время сохранения файла.");
        }
    }
}
