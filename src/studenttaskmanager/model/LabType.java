package studenttaskmanager.model;

final class LabType extends TaskType {
    LabType() {
        super("Lab");
    }

    @Override
    public double getWeight() {
        return 1.4;
    }

    @Override
    public String getCode() {
        return "LAB";
    }
}
