package studenttaskmanager.data;

import studenttaskmanager.exception.TaskPersistenceException;
import studenttaskmanager.model.Difficulty;
import studenttaskmanager.model.Task;
import studenttaskmanager.model.TaskStatus;
import studenttaskmanager.model.TaskType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CsvTaskRepository {
    private static final String HEADER =
            "id,title,subject,type,difficulty,estimatedHours,deadline,status,notes,completedDate";

    private final Path file;

    public CsvTaskRepository(Path file) {
        this.file = file;
    }

    public synchronized List<Task> load() throws TaskPersistenceException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<Task> tasks = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank() || line.toLowerCase().startsWith("id,")) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() < 8) {
                    continue;
                }
                try {
                    tasks.add(taskFromFields(fields));
                } catch (RuntimeException ignored) {
                }
            }
            return tasks;
        } catch (IOException exception) {
            throw new TaskPersistenceException("Could not load tasks from " + file, exception);
        }
    }

    public synchronized void append(Task task) throws TaskPersistenceException {
        BufferedWriter writer = null;
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            boolean writeHeader = !Files.exists(file) || Files.size(file) == 0;
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (writeHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            writer.write(toCsv(task));
            writer.newLine();
            writer.flush();
        } catch (IOException exception) {
            throw new TaskPersistenceException("Could not append task to " + file, exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public synchronized void update(Task task) throws TaskPersistenceException {
        removeById(task.getId());
        append(task);
    }

    public synchronized void delete(UUID id) throws TaskPersistenceException {
        removeById(id);
    }

    private void removeById(UUID id) throws TaskPersistenceException {
        if (!Files.exists(file)) {
            return;
        }
        try {
            List<String> existingLines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> keptLines = new ArrayList<>();
            for (String line : existingLines) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (!fields.isEmpty() && fields.get(0).trim().equals(id.toString())) {
                    continue;
                }
                keptLines.add(line);
            }
            Files.write(file, keptLines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new TaskPersistenceException("Could not remove task from " + file, exception);
        }
    }

    private static Task taskFromFields(List<String> fields) {
        UUID id = UUID.fromString(fields.get(0).trim());
        String title = fields.get(1);
        String subject = fields.get(2);
        TaskType type = TaskType.fromCsv(fields.get(3));
        Difficulty difficulty = Difficulty.fromCsv(fields.get(4));
        double hours = Double.parseDouble(fields.get(5).trim());
        LocalDate deadline = LocalDate.parse(fields.get(6).trim());
        TaskStatus status = TaskStatus.fromCsv(fields.get(7));
        String notes = fields.size() > 8 ? fields.get(8) : "";
        LocalDate completedDate = fields.size() > 9 && !fields.get(9).isBlank()
                ? LocalDate.parse(fields.get(9).trim())
                : null;
        return new Task(id, title, subject, type, difficulty, hours, deadline, status,
                notes, completedDate);
    }

    private static String toCsv(Task task) {
        return String.join(",",
                task.getId().toString(),
                encode(task.getTitle()),
                encode(task.getSubject()),
                task.getType().getCode(),
                task.getDifficulty().getLabel(),
                Double.toString(task.getEstimatedHours()),
                task.getDeadline().toString(),
                task.getStatus().name(),
                encode(task.getNotes()),
                task.getCompletedDate() == null ? "" : task.getCompletedDate().toString());
    }

    private static String encode(String value) {
        String cleaned = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ');
        return "\"" + cleaned.replace("\"", "\"\"") + "\"";
    }

    private static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (inQuotes) {
                if (character == '"') {
                    if (index + 1 < line.length() && line.charAt(index + 1) == '"') {
                        current.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(character);
                }
            } else if (character == '"') {
                inQuotes = true;
            } else if (character == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }
}
