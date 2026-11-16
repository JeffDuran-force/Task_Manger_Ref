package studenttaskmanager.model;

final class AssignmentType extends TaskType {
    AssignmentType() {
        super("Assignment");
    }

    @Override
    public double getWeight() {
        return 1.2;
    }

    @Override
    public String getCode() {
        return "ASSIGNMENT";
    }
}
