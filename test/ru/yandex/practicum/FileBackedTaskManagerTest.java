package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    @AfterEach
    void resetFile() {
        try (BufferedWriter fileWriter = new BufferedWriter(
                new FileWriter("resources/onlyHeader.csv", StandardCharsets.UTF_8))) {
            fileWriter.write("id,type,name,status,description,epic");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createTaskAnyTypeAndSearchById() {
        TaskManager fileBackedTaskManager = Managers.getFromFile(new File("resources/onlyHeader.csv"));
        Task task = new Task("Задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание эпика");
        int taskId = fileBackedTaskManager.createTask(task).getId();
        int epicId = fileBackedTaskManager.createEpic(epic).getId();
        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", epic.getId());
        int subtaskId = fileBackedTaskManager.createSubtask(subtask).getId();

        assertNotNull(fileBackedTaskManager.getTask(taskId));
        assertEquals(task, fileBackedTaskManager.getTask(taskId));

        assertNotNull(fileBackedTaskManager.getEpic(epicId));
        assertEquals(epic, fileBackedTaskManager.getEpic(epicId));

        assertNotNull(fileBackedTaskManager.getSubtask(subtaskId));
        assertEquals(subtask, fileBackedTaskManager.getSubtask(subtaskId));
    }

    @Test
    void loadEmptyFile() {
        try {
            TaskManager fileBackedTaskManager = Managers.getFromFile(new File("resources/emptyFile.csv"));
            fail("Ожидалось исключение ManagerLoadException");
        } catch (ManagerLoadException e) {
            assertEquals("Файл имеет неверный формат.", e.getMessage());
        }
    }

    @Test
    void loadAnyTask() {
        TaskManager fileBackedTaskManager = Managers.getFromFile(new File("resources/savedFile.csv"));

        assertNotNull(fileBackedTaskManager.getAllTasks());
        assertNotNull(fileBackedTaskManager.getAllSubtasks());
        assertNotNull(fileBackedTaskManager.getAllEpics());
    }

    @Test
    void checkFormatSaveFile() {
        TaskManager fileBackedTaskManager = Managers.getFromFile(new File("resources/onlyHeader.csv"));
        Task task = new Task("Задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание эпика");
        fileBackedTaskManager.createTask(task);
        fileBackedTaskManager.createEpic(epic);
        Subtask subtask = new Subtask("Сабтаска", "Описание сабтаски", epic.getId());
        fileBackedTaskManager.createSubtask(subtask);

        String expHeader = "id,type,name,status,description,epic";
        String expSubtask = String.format("%d,%s,%s,%s,%s,%d",
                subtask.getId(),
                subtask.getType(),
                subtask.getName(),
                subtask.getStatus(),
                subtask.getDescription(),
                subtask.getEpicId());
        String expTask = String.format("%d,%s,%s,%s,%s,",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription());
        String expEpic = String.format("%d,%s,%s,%s,%s,",
                epic.getId(),
                epic.getType(),
                epic.getName(),
                epic.getStatus(),
                epic.getDescription());
        try {
            String[] strings = Files.readString(Paths.get("resources/onlyHeader.csv"),StandardCharsets.UTF_8).split("\n");
            assertEquals(expHeader, strings[0]);
            assertEquals(expTask, strings[1]);
            assertEquals(expEpic, strings[2]);
            assertEquals(expSubtask, strings[3]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
