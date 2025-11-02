package ru.yandex.practicum;

import java.time.Duration;
import java.time.LocalDateTime;

import static ru.yandex.practicum.TaskType.SUBTASK;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, Duration duration, LocalDateTime startTime, int epicId) {
        super(name, description, duration, startTime);
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

