package studenttaskmanager.service;

public enum SortMode {
    SMART_PRIORITY("Smart priority"),
    DEADLINE("Deadline"),
    DIFFICULTY("Difficulty");

    private final String label;

    SortMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
