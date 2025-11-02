package ru.yandex.practicum;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static ru.yandex.practicum.TaskType.TASK;

public class Task implements Comparable<Task> {
    private int id;
    private String name;
    private String description;
    private Status status;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.duration = Duration.ZERO;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TaskType getType() {
        return TASK;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public long getDuration() {
        return duration.toMinutes();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }

        return startTime.plus(duration);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", name='" + name + "', description='" + description + "', status=" + status
                + ", duration=" + duration.toMinutes() + ", startTime=" + startTime + ", endTime=" + getEndTime() + "}";
    }

    public String toString(Task task) {
        String toString = task.getId() + "," +
                task.getType() + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                task.getDuration() + "," +
                task.getStartTime();

        if (task instanceof Subtask subtask) {
            return toString + "," + subtask.getEpicId();
        }

        return toString + ",";
    }

    @Override
    public int compareTo(Task o) {
        return this.getStartTime().compareTo(o.getStartTime());
    }

    public boolean crossTasks(Task o) {
        long startTime1 = this.getStartTime().toEpochSecond(ZoneOffset.UTC);
        long endTime1 = this.getEndTime().toEpochSecond(ZoneOffset.UTC);
        long startTime2 = o.getStartTime().toEpochSecond(ZoneOffset.UTC);
        long endTime2 = o.getEndTime().toEpochSecond(ZoneOffset.UTC);

        return endTime1 > startTime2 && endTime2 > startTime1;
    }
}
