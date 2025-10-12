package ru.yandex.practicum;

import static ru.yandex.practicum.TaskType.SUBTASK;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" + super.toString() + ", epicId=" + epicId + "}";
    }

    public String toString(Subtask subtask) {
        return super.toString(subtask) + subtask.getEpicId();
    }

}

