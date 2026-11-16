package studenttaskmanager.exception;

public class TaskValidationException extends IllegalArgumentException {
    public TaskValidationException(String message) {
        super(message);
    }

    public TaskValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
