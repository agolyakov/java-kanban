package ru.yandex.practicum;

import java.io.File;

public class Main {

    static File savedFile = new File("resources/savedFile.csv");

    static TaskManager taskManagerForFile = Managers.getFromFile(savedFile);

    public static void main(String[] args) {

        System.out.println(taskManagerForFile.getAllTasks());
        System.out.println(taskManagerForFile.getAllSubtasks());
        System.out.println(taskManagerForFile.getAllEpics());

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        System.out.println("Создана задача " + taskManagerForFile.createTask(new Task("Задача 1", "Описание задачи 1")));
        System.out.println("Создана задача 2 " + taskManagerForFile.createTask(new Task("Задача 2", "Описание задачи 2")));
        System.out.println("Создан эпик " + taskManagerForFile.createEpic(new Epic("Эпик 3", "Описание эпика 3")));
        System.out.println("Создана подзадача в Эпик " + taskManagerForFile.createSubtask(new Subtask("Подзадача 4", "Описание подзадачи 4", 3)));
        System.out.println("Создана подзадача в Эпик " + taskManagerForFile.createSubtask(new Subtask("Подзадача 5", "Описание подзадачи 5", 3)));
        System.out.println("Создан эпик 2 " + taskManagerForFile.createEpic(new Epic("Эпик 6", "Описание эпика 6")));
        System.out.println("Создана подзадача в Эпик 2 " + taskManagerForFile.createSubtask(new Subtask("Подзадача 7", "Описание подзадачи 7", 6)));

        System.out.println();

        // Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        System.out.println("Список эпиков " + taskManagerForFile.getAllEpics());
        System.out.println("Список задач " + taskManagerForFile.getAllTasks());
        System.out.println("Список подзадач " + taskManagerForFile.getAllSubtasks());

        System.out.println();

        // Измените статусы созданных объектов, распечатайте их.
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        taskManagerForFile.getTask(1).setStatus(Status.IN_PROGRESS);
        taskManagerForFile.getTask(2).setStatus(Status.DONE);
        taskManagerForFile.updateTask(taskManagerForFile.getTask(1));
        taskManagerForFile.updateTask(taskManagerForFile.getTask(2));
        taskManagerForFile.getSubtask(4).setStatus(Status.IN_PROGRESS);
        taskManagerForFile.getSubtask(5).setStatus(Status.DONE);
        taskManagerForFile.getSubtask(7).setStatus(Status.DONE);
        taskManagerForFile.updateSubtask(taskManagerForFile.getSubtask(4));
        taskManagerForFile.updateSubtask(taskManagerForFile.getSubtask(5));
        taskManagerForFile.updateSubtask(taskManagerForFile.getSubtask(7));
        System.out.println(taskManagerForFile.getAllTasks());
        System.out.println(taskManagerForFile.getAllSubtasks());
        System.out.println(taskManagerForFile.getAllEpics());

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