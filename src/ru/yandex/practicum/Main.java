package ru.yandex.practicum;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Main {

    static File savedFile = new File("resources/onlyHeader.csv");

    static TaskManager taskManagerForFile = Managers.getFromFile(savedFile);

    public static void main(String[] args) {

        System.out.println(taskManagerForFile.getAllTasks());
        System.out.println(taskManagerForFile.getAllSubtasks());
        System.out.println(taskManagerForFile.getAllEpics());

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        System.out.println("Создана задача " + taskManagerForFile.createTask(new Task("Задача 1", "Описание задачи 1", Duration.of(5, ChronoUnit.MINUTES), LocalDateTime.now())));
        System.out.println("Создана задача 2 " + taskManagerForFile.createTask(new Task("Задача 2", "Описание задачи 2", Duration.of(5, ChronoUnit.MINUTES), LocalDateTime.now().plusMinutes(5))));
        System.out.println("Создан эпик " + taskManagerForFile.createEpic(new Epic("Эпик 3", "Описание эпика 3")));
        System.out.println("Создана подзадача в Эпик " + taskManagerForFile.createSubtask(new Subtask("Подзадача 4", "Описание подзадачи 4", Duration.of(5, ChronoUnit.MINUTES), LocalDateTime.now().plusMinutes(10), 3)));
        System.out.println("Создана подзадача в Эпик " + taskManagerForFile.createSubtask(new Subtask("Подзадача 5", "Описание подзадачи 5", Duration.of(5, ChronoUnit.MINUTES), LocalDateTime.now().plusMinutes(15), 3)));
        System.out.println("Создан эпик 2 " + taskManagerForFile.createEpic(new Epic("Эпик 6", "Описание эпика 6")));
        System.out.println("Создана подзадача в Эпик 2 " + taskManagerForFile.createSubtask(new Subtask("Подзадача 7", "Описание подзадачи 7", Duration.of(5, ChronoUnit.MINUTES), LocalDateTime.now().plusMinutes(20), 6)));

        System.out.println();

        // Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        System.out.println("Список эпиков " + taskManagerForFile.getAllEpics());
        System.out.println("Список задач " + taskManagerForFile.getAllTasks());
        System.out.println("Список подзадач " + taskManagerForFile.getAllSubtasks());
        System.out.println("Список отсортированных задач и подзадач: " + taskManagerForFile.getPrioritizedTasks());

        System.out.println();

        // И, наконец, попробуйте удалить одну из задач и один из эпиков.
        taskManagerForFile.deleteTask(1);
        taskManagerForFile.deleteEpic(3);

        System.out.println();

        System.out.println(taskManagerForFile.getAllTasks());
        System.out.println(taskManagerForFile.getAllSubtasks());
        System.out.println(taskManagerForFile.getAllEpics());

        System.out.println();

        printAllTasks(taskManagerForFile);

    }

    private static void printAllTasks(TaskManager taskManagerForFile) {
        System.out.println("Задачи:");
        for (Task task : taskManagerForFile.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : taskManagerForFile.getAllEpics()) {
            System.out.println(epic);

            for (Task task : taskManagerForFile.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : taskManagerForFile.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : taskManagerForFile.getHistory()) {
            System.out.println(task);
        }
    }

}