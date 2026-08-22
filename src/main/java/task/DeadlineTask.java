package task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/**
 * Represents a task that needs to be done before a specific date/time
 * e.g. _submit report by 2026-12-25 17:00.
 */
public class DeadlineTask extends Task {
    public static final String DATE_TIME_INPUT_PATTERN = "yyyy-MM-dd[ HH[:]mm]";
    public static final DateTimeFormatter DATE_TIME_INPUT_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern(DATE_TIME_INPUT_PATTERN)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private static final String DATE_TIME_DISPLAY_PATTERN = "MMM dd yyyy HHmm";
    private static final DateTimeFormatter DATE_TIME_DISPLAY_FORMATTER = DateTimeFormatter
            .ofPattern(DATE_TIME_DISPLAY_PATTERN);

    private LocalDateTime deadlineDatetime;

    /**
     * Creates a new {@link DeadlineTask} that is incomplete.
     *
     * @param details  The details of the taks.
     * @param deadline The deadline date/time of the task.
     * @throws DateTimeParseException If the deadline text cannot be parsed.
     */
    public DeadlineTask(String details, String deadline) {
        super(details);
        this.deadlineDatetime = LocalDateTime.parse(deadline, DATE_TIME_INPUT_FORMATTER);
    }

    public LocalDateTime getDeadlineDatetime() {
        return this.deadlineDatetime;
    }

    @Override
    public String toString() {
        return "[D]%s (by: %s)".formatted(
                super.toString(),
                deadlineDatetime.format(DATE_TIME_DISPLAY_FORMATTER));
    }
}
