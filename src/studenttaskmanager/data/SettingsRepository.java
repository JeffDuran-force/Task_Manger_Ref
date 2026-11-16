package studenttaskmanager.data;

import studenttaskmanager.exception.TaskPersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class SettingsRepository {
    private static final double DEFAULT_HOURS_PER_DAY = 3.0;

    private final Path file;

    public SettingsRepository(Path file) {
        this.file = file;
    }

    public double loadHoursPerDay() throws TaskPersistenceException {
        if (!Files.exists(file)) {
            return DEFAULT_HOURS_PER_DAY;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("key,")) {
                    continue;
                }
                String[] parts = trimmed.split(",", 2);
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase("hoursPerDay")) {
                    double value = Double.parseDouble(parts[1].trim());
                    if (value >= 0.25 && value <= 24.0) {
                        return value;
                    }
                }
            }
            return DEFAULT_HOURS_PER_DAY;
        } catch (IOException exception) {
            throw new TaskPersistenceException("Could not load settings from " + file, exception);
        } catch (NumberFormatException exception) {
            return DEFAULT_HOURS_PER_DAY;
        }
    }

    public void saveHoursPerDay(double hoursPerDay) throws TaskPersistenceException {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String content = "key,value\nhoursPerDay,"
                    + String.format(Locale.ROOT, "%.2f", hoursPerDay) + "\n";
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new TaskPersistenceException("Could not save settings to " + file, exception);
        }
    }
}
