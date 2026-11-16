package studenttaskmanager.model;

final class ExamType extends TaskType {
    ExamType() {
        super("Exam");
    }

    @Override
    public double getWeight() {
        return 2.2;
    }

    @Override
    public String getCode() {
        return "EXAM";
    }
}
