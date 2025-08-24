package ru.yandex.practicum;

import java.util.List;

public class Main {

    static TaskManager taskManager = Managers.getDefault();

    public static void main(String[] args) throws Exception {

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        System.out.println("Создана задача " + taskManager.createTask(new Task("Задача", "Описание задачи")));
        System.out.println("Создана задача 2 " + taskManager.createTask(new Task("Задача 2", "Описание задачи 2")));
        System.out.println("Создан эпик " + taskManager.createEpic(new Epic("Эпик", "Описание эпика")));
        System.out.println("Создана подзадача в Эпик " + taskManager.createSubtask(new Subtask("Подзадача", "Описание подзадачи", 3)));
        System.out.println("Создана подзадача в Эпик " + taskManager.createSubtask(new Subtask("Подзадача", "Описание подзадачи", 3)));
        System.out.println("Создан эпик 2 " + taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2")));
        System.out.println("Создана подзадача в Эпик 2 " + taskManager.createSubtask(new Subtask("Подзадача", "Описание подзадачи", 6)));

        System.out.println();

        // Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        System.out.println("Список эпиков " + taskManager.getAllEpics());
        System.out.println("Список задач " + taskManager.getAllTasks());
        System.out.println("Список подзадач " + taskManager.getAllSubtasks());

        System.out.println();

        // Измените статусы созданных объектов, распечатайте их.
        // Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        taskManager.getTask(1).setStatus(Status.IN_PROGRESS);
        taskManager.getTask(2).setStatus(Status.DONE);
        taskManager.updateTask(taskManager.getTask(1));
        taskManager.updateTask(taskManager.getTask(2));
        taskManager.getSubtask(4).setStatus(Status.IN_PROGRESS);
        taskManager.getSubtask(5).setStatus(Status.DONE);
        taskManager.getSubtask(7).setStatus(Status.DONE);
        taskManager.updateSubtask(taskManager.getSubtask(4));
        taskManager.updateSubtask(taskManager.getSubtask(5));
        taskManager.updateSubtask(taskManager.getSubtask(7));
        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllSubtasks());
        System.out.println(taskManager.getAllEpics());

        // И, наконец, попробуйте удалить одну из задач и один из эпиков.
        taskManager.deleteTask(1);
        taskManager.deleteEpic(3);

        System.out.println();

        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllSubtasks());
        System.out.println(taskManager.getAllEpics());

        System.out.println();

        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : taskManager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : taskManager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }

}