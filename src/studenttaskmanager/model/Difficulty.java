package studenttaskmanager.model;

public enum Difficulty {
    LEVEL_1("1", 1.0),
    LEVEL_2("2", 2.0),
    LEVEL_3("3", 3.0),
    LEVEL_4("4", 4.0),
    LEVEL_5("5", 5.0),
    LEVEL_6("6", 6.0),
    LEVEL_7("7", 7.0),
    LEVEL_8("8", 8.0),
    LEVEL_9("9", 9.0),
    LEVEL_10("10", 10.0);

    private final String label;
    private final double weight;

    Difficulty(String label, double weight) {
        this.label = label;
        this.weight = weight;
    }

    public static final Difficulty DEFAULT = LEVEL_5;

    public String getLabel() {
        return label;
    }

    public double getWeight() {
        return weight;
    }

    public static Difficulty fromCsv(String value) {
        if (value == null) {
            return DEFAULT;
        }
        String normalized = value.trim();
        if (normalized.equalsIgnoreCase("EASY")) {
            return LEVEL_3;
        }
        if (normalized.equalsIgnoreCase("MEDIUM")) {
            return LEVEL_5;
        }
        if (normalized.equalsIgnoreCase("HARD")) {
            return LEVEL_8;
        }
        for (Difficulty difficulty : values()) {
            if (difficulty.name().equalsIgnoreCase(normalized)
                    || difficulty.label.equalsIgnoreCase(normalized)) {
                return difficulty;
            }
        }
        return DEFAULT;
    }

    @Override
    public String toString() {
        return label;
    }
}
