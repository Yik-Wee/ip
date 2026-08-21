package task.serde;

import task.DeadlineTask;
import task.EventTask;
import task.Task;
import task.TodoTask;

/**
 * Contains static methods to serialize tasks into a human-readable format that
 * can be saved to disk.
 * <p>
 * Example:
 *
 * <pre>{@code
 * Task task = new TodoTask("read a book");
 * String serialized = TaskSerializer.serialize(task);
 * // save the result to a file...
 * }</pre>
 *
 * @see #serialize(Task) The general serialized format
 */
public class TaskSerializer {
    /**
     * Serializes the {@link Task} instance into the following format:
     *
     * <pre>
     * [task-type] // e.g. todo, event, deadline
     * completed = 1 // 1 for completed, 0 for not completed
     * details = details here
     * task-specific-property-1 = value-1
     * task-specific-property-2 = value-2
     * // ...
     * </pre>
     *
     * @param task The task to serialize.
     */
    public static String serialize(Task task) {
        return switch (task) {
            case TodoTask todo -> serialize(todo);
            case DeadlineTask deadline -> serialize(deadline);
            case EventTask event -> serialize(event);
            default -> throw new UnsupportedOperationException(
                    "`%s` task is not supported".formatted(task.getClass().getName()));
        };
    }

    /**
     * Serializes the {@link TodoTask} instance into the following format:
     *
     * <pre>
     * [todo]
     * completed = 1 // 1 for completed, 0 for not completed
     * details = details here // newlines are converted into \n
     * </pre>
     *
     * @param task The task to serialize.
     * @return The serialized task.
     */
    public static String serialize(TodoTask task) {
        String completed = task.getCompleted() ? "1" : "0";
        String details = task.getDetails().replaceAll("\n", "\\n");
        return """
                [todo]
                completed = %s
                details = %s
                """.formatted(completed, details);
    }

    /**
     * Serializes the {@link DeadlineTask} instance into the following format:
     *
     * <pre>
     * [deadline]
     * completed = 1 // 1 for completed, 0 for not completed
     * details = details here // newlines are converted into \n
     * by = deadline date/time
     * </pre>
     *
     * @param task The task to serialize.
     * @return The serialized task.
     */
    public static String serialize(DeadlineTask task) {
        String completed = task.getCompleted() ? "1" : "0";
        String details = task.getDetails().replaceAll("\n", "\\n");
        String by = task.getDeadlineDatetime();
        return """
                [deadline]
                completed = %s
                details = %s
                by = %s
                """.formatted(completed, details, by);
    }

    /**
     * Serializes the {@link EventTask} instance into the following format:
     *
     * <pre>
     * [deadline]
     * completed = 1 // 1 for completed, 0 for not completed
     * details = details here // newlines are converted into \n
     * start = start date/time
     * end = end date/time
     * </pre>
     *
     * @param task The task to serialize.
     * @return The serialized task.
     */
    public static String serialize(EventTask task) {
        String completed = task.getCompleted() ? "1" : "0";
        String details = task.getDetails().replaceAll("\n", "\\n");
        String start = task.getStartDatetime();
        String end = task.getEndDatetime();

        return """
                [event]
                completed = %s
                details = %s
                start = %s
                end = %s
                """.formatted(completed, details, start, end);
    }
}
