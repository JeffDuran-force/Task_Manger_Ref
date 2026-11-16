package studenttaskmanager.model;

public enum TaskStatus {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    DONE("Done");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDone() {
        return this == DONE;
    }

    public static TaskStatus fromCsv(String value) {
        if (value == null) {
            return NOT_STARTED;
        }
        String normalized = value.trim();
        for (TaskStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized)
                    || status.label.equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        return NOT_STARTED;
    }

    @Override
    public String toString() {
        return label;
    }
}
