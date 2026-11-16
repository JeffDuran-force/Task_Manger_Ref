package studenttaskmanager.exception;

public class TaskPersistenceException extends RuntimeException {
    public TaskPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
