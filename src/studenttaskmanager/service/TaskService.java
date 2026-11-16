package studenttaskmanager.service;

import studenttaskmanager.data.CsvTaskRepository;
import studenttaskmanager.data.SettingsRepository;
import studenttaskmanager.exception.TaskNotFoundException;
import studenttaskmanager.exception.TaskValidationException;
import studenttaskmanager.model.Difficulty;
import studenttaskmanager.model.Task;
import studenttaskmanager.model.TaskStatus;
import studenttaskmanager.model.TaskType;
import studenttaskmanager.model.WorkloadWarning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TaskService {
    private final CsvTaskRepository repository;
    private final SettingsRepository settingsRepository;
    private final List<Task> tasks;
    private double hoursPerDay;

    public TaskService(CsvTaskRepository repository, SettingsRepository settingsRepository) {
        this.repository = repository;
        this.settingsRepository = settingsRepository;
        this.tasks = new ArrayList<>(repository.load());
        this.hoursPerDay = settingsRepository.loadHoursPerDay();
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public double getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(double hoursPerDay) {
        if (hoursPerDay < 0.25 || hoursPerDay > 24.0) {
            throw new TaskValidationException("Hours per day must be between 0.25 and 24.");
        }
        settingsRepository.saveHoursPerDay(hoursPerDay);
        this.hoursPerDay = hoursPerDay;
    }

    public void addTask(Task task) {
        repository.append(task);
        tasks.add(task);
    }

    public void updateTask(Task task) {
        int index = indexOf(task.getId());
        if (index < 0) {
            throw new TaskNotFoundException("Task was not found.");
        }
        repository.update(task);
        tasks.set(index, task);
    }

    public void deleteTask(Task task) {
        repository.delete(task.getId());
        tasks.removeIf(existing -> existing.getId().equals(task.getId()));
    }

    public void markDone(Task task) {
        updateTask(task.withStatus(TaskStatus.DONE));
    }

    public List<Task> getVisibleTasks(String keyword, String subject, TaskType type,
                                      TaskStatus status, Difficulty difficulty,
                                      SortMode sortMode) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (!matchesKeyword(task, needle)) {
                continue;
            }
            if (subject != null && !task.getSubject().equalsIgnoreCase(subject)) {
                continue;
            }
            if (type != null && task.getType() != type) {
                continue;
            }
            if (status != null && task.getStatus() != status) {
                continue;
            }
            if (difficulty != null && task.getDifficulty() != difficulty) {
                continue;
            }
            result.add(task);
        }
        result.sort(comparatorFor(sortMode));
        return result;
    }

    public List<String> getSubjects() {
        Set<String> subjects = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Task task : tasks) {
            subjects.add(task.getSubject());
        }
        return new ArrayList<>(subjects);
    }

    public List<WorkloadWarning> getWorkloadWarnings() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Double> hoursByDay = new TreeMap<>();
        for (Task task : tasks) {
            if (task.isDone()) {
                continue;
            }
            LocalDate effectiveDate = task.getDeadline().isBefore(today)
                    ? today
                    : task.getDeadline();
            hoursByDay.merge(effectiveDate, task.getEstimatedHours(), Double::sum);
        }

        List<WorkloadWarning> warnings = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> entry : hoursByDay.entrySet()) {
            if (entry.getValue() > hoursPerDay + 0.0001) {
                warnings.add(new WorkloadWarning(WorkloadWarning.Scope.DAY,
                        entry.getKey(), entry.getKey(), entry.getValue(), hoursPerDay));
            }
        }

        Map<LocalDate, Double> hoursByWeek = new TreeMap<>();
        for (Map.Entry<LocalDate, Double> entry : hoursByDay.entrySet()) {
            LocalDate weekStart = entry.getKey()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            hoursByWeek.merge(weekStart, entry.getValue(), Double::sum);
        }
        double weeklyLimit = hoursPerDay * 7.0;
        for (Map.Entry<LocalDate, Double> entry : hoursByWeek.entrySet()) {
            if (entry.getValue() > weeklyLimit + 0.0001) {
                warnings.add(new WorkloadWarning(WorkloadWarning.Scope.WEEK,
                        entry.getKey(), entry.getKey().plusDays(6),
                        entry.getValue(), weeklyLimit));
            }
        }
        warnings.sort(Comparator.comparing(WorkloadWarning::getPeriodStart)
                .thenComparing(warning -> warning.getScope().name()));
        return warnings;
    }

    private boolean matchesKeyword(Task task, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        String searchable = String.join(" ",
                task.getTitle(),
                task.getSubject(),
                task.getNotes(),
                task.getType().getLabel(),
                task.getDifficulty().getLabel(),
                task.getStatus().getLabel()).toLowerCase(Locale.ROOT);
        return searchable.contains(needle);
    }

    private Comparator<Task> comparatorFor(SortMode sortMode) {
        Comparator<Task> base;
        if (sortMode == SortMode.DEADLINE) {
            base = Comparator.comparing(Task::getDeadline)
                    .thenComparing(Comparator.comparingDouble(Task::priorityScore).reversed())
                    .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
        } else if (sortMode == SortMode.DIFFICULTY) {
            base = Comparator.<Task>comparingDouble(
                            task -> task.getDifficulty().getWeight()).reversed()
                    .thenComparing(Task::getDeadline)
                    .thenComparing(Comparator.comparingDouble(Task::priorityScore).reversed())
                    .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
        } else {
            base = Comparator.comparingDouble(Task::priorityScore).reversed()
                    .thenComparing(Task::getDeadline)
                    .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
        }
        return Comparator.<Task, Boolean>comparing(Task::isOverdue).reversed()
                .thenComparing(Task::isDone)
                .thenComparing(base);
    }

    private int indexOf(java.util.UUID id) {
        for (int index = 0; index < tasks.size(); index++) {
            if (tasks.get(index).getId().equals(id)) {
                return index;
            }
        }
        return -1;
    }
}
