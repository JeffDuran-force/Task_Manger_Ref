package studenttaskmanager.model;

import studenttaskmanager.exception.TaskValidationException;

public abstract class TaskType implements Reportable {
    private static final TaskType[] VALUES = {
            new AssignmentType(),
            new ProjectType(),
            new ExamType(),
            new ReadingType(),
            new LabType(),
            new OtherType()
    };

    private final String label;

    protected TaskType(String label) {
        if (label == null || label.isBlank()) {
            throw new TaskValidationException("Task type label cannot be empty.");
        }
        this.label = label;
    }

    public final String getLabel() {
        return label;
    }

    public abstract double getWeight();

    public abstract String getCode();

    public static TaskType[] values() {
        return VALUES.clone();
    }

    public static TaskType defaultType() {
        return VALUES[0];
    }

    public static TaskType fromCsv(String value) {
        if (value == null) {
            return VALUES[VALUES.length - 1];
        }
        String normalized = value.trim();
        for (TaskType type : VALUES) {
            if (type.getCode().equalsIgnoreCase(normalized)
                    || type.label.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return VALUES[VALUES.length - 1];
    }

    @Override
    public String describe() {
        return label + " (weight " + getWeight() + ")";
    }

    @Override
    public String toString() {
        return label;
    }
}
