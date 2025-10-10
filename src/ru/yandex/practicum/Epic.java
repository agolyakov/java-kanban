package ru.yandex.practicum;

import java.util.ArrayList;
import static ru.yandex.practicum.TaskType.EPIC;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public TaskType getType() {
        return EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" + super.toString() + ", subtasks=" + subtaskIds + "}";
    }

    public String toString(Epic epic) {
        return super.toString(epic);
    }

}

