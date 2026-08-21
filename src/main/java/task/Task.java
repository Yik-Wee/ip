package task;

/**
 * Represents a task, storing the task `details` and whether the task has been
 * completed or not.
 */
public abstract class Task {
    private String details;
    private boolean isCompleted;

    /**
     * Creates a new *incomplete* `Task`.
     *
     * @param details the details of the task.
     */
    public Task(String details) {
        this(details, false);
    }

    /**
     * Creates a new `Task`.
     *
     * @param details     the details of the task.
     * @param isCompleted whether the task has been completed.
     */
    public Task(String details, boolean isCompleted) {
        this.details = details;
        this.isCompleted = isCompleted;
    }

    /**
     * Marks the task as complete.
     */
    public void markComplete() {
        this.isCompleted = true;
    }

    /**
     * Marks the task as incomplete.
     */
    public void markIncomplete() {
        this.isCompleted = false;
    }

    public String getDetails() {
        return this.details;
    }

    public boolean getCompleted() {
        return this.isCompleted;
    }

    @Override
    public String toString() {
        String checkbox = this.isCompleted ? "[X]" : "[ ]";
        return "%s %s".formatted(checkbox, details);
    }
}
