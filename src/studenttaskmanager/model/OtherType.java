package studenttaskmanager.model;

final class OtherType extends TaskType {
    OtherType() {
        super("Other");
    }

    @Override
    public double getWeight() {
        return 0.6;
    }

    @Override
    public String getCode() {
        return "OTHER";
    }
}
