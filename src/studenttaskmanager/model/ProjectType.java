package studenttaskmanager.model;

final class ProjectType extends TaskType {
    ProjectType() {
        super("Project");
    }

    @Override
    public double getWeight() {
        return 1.8;
    }

    @Override
    public String getCode() {
        return "PROJECT";
    }
}
