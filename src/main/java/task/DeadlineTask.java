package task;

/**
 * Represents a task that needs to be done before a specific date/time
 * e.g. _submit report by 11/10/2019 5pm_.
 */
public class DeadlineTask extends Task {
    private String deadlineDatetime;

    /**
     * Creates a new {@link DeadlineTask} that is incomplete.
     *
     * @param details          The details of the taks.
     * @param deadlineDatetime The deadline date/time of the task.
     */
    public DeadlineTask(String details, String deadlineDatetime) {
        super(details);
        this.deadlineDatetime = deadlineDatetime;
    }

    public String getDeadlineDatetime() {
        return this.deadlineDatetime;
    }

    @Override
    public String toString() {
        return "[D]%s (by: %s)".formatted(super.toString(), deadlineDatetime);
    }
}
