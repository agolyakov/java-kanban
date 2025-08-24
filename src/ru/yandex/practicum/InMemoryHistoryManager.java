package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();
    public static final int MAX_HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (history.size() < MAX_HISTORY_SIZE) {
            history.add(task);
        } else {
            history.removeFirst();
            history.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
