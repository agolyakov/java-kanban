package ru.yandex.practicum;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFromFile(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }
}
