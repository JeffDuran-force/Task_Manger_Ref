package studenttaskmanager.model;

import studenttaskmanager.exception.TaskValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public final class Task implements Reportable {
    private final UUID id;
    private final String title;
    private final String subject;
    private final TaskType type;
    private final Difficulty difficulty;
    private final double estimatedHours;
    private final LocalDate deadline;
    private final TaskStatus status;
    private final String notes;
    private final LocalDate completedDate;

    public Task(UUID id, String title, String subject, TaskType type, Difficulty difficulty,
                double estimatedHours, LocalDate deadline, TaskStatus status, String notes,
                LocalDate completedDate) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = requireText(title, "title");
        this.subject = requireText(subject, "subject");
        this.type = Objects.requireNonNull(type, "type");
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        if (estimatedHours <= 0) {
            throw new TaskValidationException("Estimated hours must be greater than zero.");
        }
        this.estimatedHours = estimatedHours;
        this.deadline = Objects.requireNonNull(deadline, "deadline");
        this.status = status == null ? TaskStatus.NOT_STARTED : status;
        this.notes = notes == null ? "" : notes.trim();
        if (this.status.isDone()) {
            this.completedDate = completedDate == null ? LocalDate.now() : completedDate;
        } else {
            this.completedDate = null;
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new TaskValidationException("Task " + fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public TaskType getType() {
        return type;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public boolean isDone() {
        return status.isDone();
    }

    public boolean isOverdue() {
        return !isDone() && deadline.isBefore(LocalDate.now());
    }

    public long daysUntilDeadline() {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    public double priorityScore() {
        if (isDone()) {
            return 0.0;
        }
        long days = daysUntilDeadline();
        double urgency;
        if (days < 0) {
            urgency = 60.0;
        } else if (days == 0) {
            urgency = 50.0;
        } else {
            urgency = Math.max(0.0, 40.0 - (days * 2.0));
        }
        double score = (difficulty.getWeight() * 3.0)
                + (type.getWeight() * 10.0)
                + urgency;
        return Math.round(score * 10.0) / 10.0;
    }

    public long daysNeeded(double hoursPerDay) {
        double safeHoursPerDay = Math.max(0.25, hoursPerDay);
        return Math.max(1L, (long) Math.ceil(estimatedHours / safeHoursPerDay));
    }

    public LocalDate suggestedStartDate(double hoursPerDay) {
        return deadline.minusDays(daysNeeded(hoursPerDay));
    }

    public Task withStatus(TaskStatus newStatus) {
        LocalDate completion = newStatus.isDone()
                ? (completedDate == null ? LocalDate.now() : completedDate)
                : null;
        return new Task(id, title, subject, type, difficulty, estimatedHours, deadline,
                newStatus, notes, completion);
    }

    @Override
    public String describe() {
        return title + " [" + subject + "] - " + type.getLabel()
                + ", difficulty " + difficulty.getLabel();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Task task)) {
            return false;
        }
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
