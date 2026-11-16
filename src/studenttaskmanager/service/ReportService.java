package studenttaskmanager.service;

import studenttaskmanager.model.Task;

import studenttaskmanager.exception.TaskPersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReportService {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter GENERATED_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.ENGLISH);

    public String generateReport(List<Task> tasks, double hoursPerDay) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Task> dueThisWeek = new ArrayList<>();
        long completedThisWeek = 0;
        for (Task task : tasks) {
            LocalDate deadline = task.getDeadline();
            if (!deadline.isBefore(weekStart) && !deadline.isAfter(weekEnd)) {
                dueThisWeek.add(task);
            }
            LocalDate completed = task.getCompletedDate();
            if (completed != null && !completed.isBefore(weekStart)
                    && !completed.isAfter(weekEnd)) {
                completedThisWeek++;
            }
        }
        dueThisWeek.sort(Comparator.comparing(Task::getDeadline)
                .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));

        long total = tasks.size();
        long done = tasks.stream().filter(Task::isDone).count();
        long overdue = tasks.stream().filter(Task::isOverdue).count();
        double completionRate = total == 0 ? 0.0 : (done * 100.0) / total;
        double hoursDueThisWeek = dueThisWeek.stream()
                .mapToDouble(Task::getEstimatedHours)
                .sum();

        StringBuilder report = new StringBuilder();
        report.append("STUDENT TASK MANAGER REPORT\n");
        report.append("=".repeat(74)).append("\n");
        report.append("Generated: ").append(LocalDateTime.now().format(GENERATED_FORMAT)).append("\n");
        report.append(String.format(Locale.ENGLISH,
                "Study capacity: %.1f hours/day (weekly limit %.1f hours)%n",
                hoursPerDay, hoursPerDay * 7.0));
        report.append("\n");

        report.append(String.format(Locale.ENGLISH, "WEEKLY SUMMARY (%s - %s)%n",
                weekStart.format(DATE_FORMAT), weekEnd.format(DATE_FORMAT)));
        report.append("-".repeat(74)).append("\n");
        report.append("Tasks due this week:      ").append(dueThisWeek.size()).append("\n");
        report.append(String.format(Locale.ENGLISH,
                "Estimated hours due:      %.1f h%n", hoursDueThisWeek));
        report.append("Tasks completed this week: ").append(completedThisWeek).append("\n");
        report.append("Overdue tasks:             ").append(overdue).append("\n");
        report.append("\n");

        report.append("COMPLETION RATE\n");
        report.append("-".repeat(74)).append("\n");
        report.append(String.format(Locale.ENGLISH,
                "%d of %d tasks complete (%.1f%%)%n", done, total, completionRate));
        report.append("\n");

        report.append("TASKS PER SUBJECT\n");
        report.append("-".repeat(74)).append("\n");
        report.append(String.format(Locale.ENGLISH, "%-24s %6s %6s %8s %14s%n",
                "Subject", "Total", "Done", "Pending", "Pending hours"));
        Map<String, List<Task>> bySubject = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Task task : tasks) {
            bySubject.computeIfAbsent(task.getSubject(), ignored -> new ArrayList<>()).add(task);
        }
        if (bySubject.isEmpty()) {
            report.append("No tasks recorded.\n");
        } else {
            for (Map.Entry<String, List<Task>> entry : bySubject.entrySet()) {
                List<Task> subjectTasks = entry.getValue();
                long subjectDone = subjectTasks.stream().filter(Task::isDone).count();
                long pending = subjectTasks.size() - subjectDone;
                double pendingHours = subjectTasks.stream()
                        .filter(task -> !task.isDone())
                        .mapToDouble(Task::getEstimatedHours)
                        .sum();
                report.append(String.format(Locale.ENGLISH, "%-24s %6d %6d %8d %14.1f%n",
                        entry.getKey(), subjectTasks.size(), subjectDone, pending, pendingHours));
            }
        }
        report.append("\n");

        report.append("TASKS DUE THIS WEEK\n");
        report.append("-".repeat(74)).append("\n");
        if (dueThisWeek.isEmpty()) {
            report.append("No tasks are due this week.\n");
        } else {
            for (Task task : dueThisWeek) {
                String startInfo = task.isDone()
                        ? "done"
                        : task.suggestedStartDate(hoursPerDay).isBefore(today)
                        ? "start now"
                        : "start by " + task.suggestedStartDate(hoursPerDay)
                                .format(SHORT_DATE_FORMAT);
                report.append(String.format(Locale.ENGLISH,
                        "- %s [%s] | due %s | %.1f h | %s%n",
                        task.getTitle(), task.getSubject(),
                        task.getDeadline().format(DATE_FORMAT),
                        task.getEstimatedHours(), startInfo));
            }
        }
        report.append("\n");

        report.append("OVERDUE TASKS\n");
        report.append("-".repeat(74)).append("\n");
        List<Task> overdueTasks = tasks.stream()
                .filter(Task::isOverdue)
                .sorted(Comparator.comparing(Task::getDeadline)
                        .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
        if (overdueTasks.isEmpty()) {
            report.append("No overdue tasks.\n");
        } else {
            for (Task task : overdueTasks) {
                report.append(String.format(Locale.ENGLISH,
                        "- %s [%s] | due %s | priority %.1f%n",
                        task.getTitle(), task.getSubject(),
                        task.getDeadline().format(DATE_FORMAT), task.priorityScore()));
            }
        }
        return report.toString();
    }

    public void exportReport(String report, Path path) throws TaskPersistenceException {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, report, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new TaskPersistenceException("Could not export report to " + path, exception);
        }
    }
}
