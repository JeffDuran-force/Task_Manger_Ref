package studenttaskmanager.model;

final class ReadingType extends TaskType {
    ReadingType() {
        super("Reading");
    }

    @Override
    public double getWeight() {
        return 0.8;
    }

    @Override
    public String getCode() {
        return "READING";
    }
}
