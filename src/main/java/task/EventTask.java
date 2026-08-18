package task;

/**
 * Represents a task that starts at a specific date/time and ends at a specific
 * date/time, e.g. _team project meeting 2/10/2019 2-4pm_
 */
public class EventTask extends Task {
    private String startDatetime;
    private String endDatetime;

    /**
     * Creates a new {@link EventTask} that is incomplete.
     *
     * @param details       The details of the task.
     * @param startDatetime The start date/time of the task.
     * @param endDatetime   The end date/time of the task.
     */
    public EventTask(String details, String startDatetime, String endDatetime) {
        super(details);
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    @Override
    public String toString() {
        return "[E]%s (from: %s | to: %s)".formatted(super.toString(), startDatetime, endDatetime);
    }
}
