package grug.command;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import grug.storage.TaskStorage;
import grug.task.DeadlineTask;
import grug.task.EventTask;
import grug.task.Task;
import grug.task.TaskList;
import grug.task.TodoTask;

/**
 * Algebraic Data Type that represents the user's command.
 */
public sealed interface GrugCommand {
    /**
     * Represents no command given.
     */
    record Empty() implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            return new CommandResult.Ok("", false);
        }
    }

    /**
     * Command to quit the program.
     */
    record Quit() implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            return new CommandResult.Ok("Unga. Bye. さよなら", true);
        }
    }

    /**
     * Command to list all tasks.
     */
    record ListTasks() implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            if (tasks.isEmpty()) {
                return new CommandResult.Ok("No tasks added.", false);
            }

            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < tasks.size(); i++) {
                // Optional::get() here will not throw since our index is always in range
                msg.append("%d. %s\n".formatted(i + 1, tasks.getTask(i).get()));
            }
            return new CommandResult.Ok(msg.toString().stripTrailing(), false);
        }
    }

    /**
     * Command to add a todo task to the list of tasks.
     *
     * @param task the task to add.
     */
    record AddTodoTask(TodoTask task) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            tasks.addTask(task);

            StringBuilder msg = new StringBuilder();
            msg.append("added: ").append(task);

            try {
                storage.saveTasks(tasks.getTasks());
                return new CommandResult.Ok(msg.toString(), false);
            } catch (IOException e) {
                msg.append("\nFailed to save tasks to ").append(storage.getFilepath());
                return new CommandResult.Partial(msg.toString(), false);
            }
        }
    }

    /**
     * Command to add a task with deadline to the list of tasks.
     *
     * @param task The task to add.
     */
    record AddDeadlineTask(DeadlineTask task) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            tasks.addTask(task);

            StringBuilder msg = new StringBuilder();
            msg.append("added: ").append(task);

            try {
                storage.saveTasks(tasks.getTasks());
                return new CommandResult.Ok(msg.toString(), false);
            } catch (IOException e) {
                msg.append("\nFailed to save tasks to ").append(storage.getFilepath());
                return new CommandResult.Partial(msg.toString(), false);
            }
        }
    }

    /**
     * Command to add an event task with a start and end date/time.
     *
     * @param task The task to add.
     */
    record AddEventTask(EventTask task) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            tasks.addTask(task);

            StringBuilder msg = new StringBuilder();
            msg.append("added: ").append(task);

            try {
                storage.saveTasks(tasks.getTasks());
                return new CommandResult.Ok(msg.toString(), false);
            } catch (IOException e) {
                msg.append("\nFailed to save tasks to ").append(storage.getFilepath());
                return new CommandResult.Partial(msg.toString(), false);
            }
        }
    }

    /**
     * Command to mark a task as complete.
     *
     * @param taskNum The 1-based task number.
     */
    record MarkTask(int taskNum) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            int taskIdx = taskNum - 1;

            // empty optional if index out of bounds
            Optional<Task> optionalTask = tasks.getTask(taskIdx);
            if (optionalTask.isEmpty()) {
                return new CommandResult.Err("Can't find task number %d".formatted(taskNum), false);
            }

            Task task = optionalTask.get();
            task.markComplete();

            StringBuilder msg = new StringBuilder();
            msg.append("Updated task %d: %s".formatted(taskNum, task));

            try {
                storage.saveTasks(tasks.getTasks());
                return new CommandResult.Ok(msg.toString(), false);
            } catch (IOException e) {
                msg.append("\nFailed to save tasks to ").append(storage.getFilepath());
                return new CommandResult.Partial(msg.toString(), false);
            }
        }
    }

    /**
     * Command to unmark a task (i.e. mark as incomplete).
     *
     * @param taskNum The 1-based task number.
     */
    record UnmarkTask(int taskNum) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            int taskIdx = taskNum - 1;

            // empty optional if index out of bounds
            Optional<Task> optionalTask = tasks.getTask(taskIdx);
            if (optionalTask.isEmpty()) {
                return new CommandResult.Err("Can't find task number %d".formatted(taskNum), false);
            }

            Task task = optionalTask.get();
            task.markIncomplete();

            StringBuilder msg = new StringBuilder();
            msg.append("Updated task %d: %s".formatted(taskNum, task));

            try {
                storage.saveTasks(tasks.getTasks());
                return new CommandResult.Ok(msg.toString(), false);
            } catch (IOException e) {
                msg.append("\nFailed to save tasks to ").append(storage.getFilepath());
                return new CommandResult.Partial(msg.toString(), false);
            }
        }
    }

    /**
     * Command to delete a task based on the task number (1-based).
     *
     * @param taskNum The task to delete.
     */
    record DeleteTask(int taskNum) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            int taskIdx = taskNum - 1;

            // empty optional if index out of bounds
            Optional<Task> optionalRemoved = tasks.removeTask(taskIdx);
            if (optionalRemoved.isEmpty()) {
                return new CommandResult.Err("Can't find task number %d".formatted(taskNum), false);
            }

            Task removedTask = optionalRemoved.get();

            StringBuilder msg = new StringBuilder();
            msg.append("deleted: %s".formatted(removedTask));

            try {
                storage.saveTasks(tasks.getTasks());
                return new CommandResult.Ok(msg.toString(), false);
            } catch (IOException e) {
                msg.append("\nFailed to save tasks to ").append(storage.getFilepath());
                return new CommandResult.Partial(msg.toString(), false);
            }
        }
    }

    /**
     * Command to list tasks that coincide with a given date.
     */
    record FindTasksByDate(LocalDate date) implements GrugCommand {
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            if (tasks.isEmpty()) {
                return new CommandResult.Ok("No tasks added.", false);
            }

            StringBuilder msg = new StringBuilder();

            for (int i = 0; i < tasks.size(); i++) {
                // get() here will not throw because index is in range
                Task task = tasks.getTask(i).get();

                if (task.doesOccurOn(date)) {
                    msg.append("%d. %s\n".formatted(i + 1, task));
                }
            }

            return new CommandResult.Ok(msg.toString().stripTrailing(), false);
        }
    }

    /**
     * Command to list tasks whose details contain the {@code detailsSubstring},
     * case insensitive, ignoring extra whitespace/newlines.
     *
     * @param detailsSubstring The details substring to use to search for the tasks.
     */
    record FindTasksByDetails(String detailsSubstring) implements GrugCommand {
        /**
         * Lists all tasks in the tasks list that contain the
         * {@link #detailsSubstring()} (case insensitive, ignoring extra
         * whitespace/newlines).
         *
         * @param tasks   The task list to read from.
         * @param storage The task storage that is never used. Can be {@code null}
         * @return A {@link CommandResult.Ok} result containing the list of tasks
         *         containing the {@link #detailsSubstring()} seperated by newlines in
         *         {@code message} (or an appropriate message
         *         if task list is empty or no tasks coincide with the {@link #date()})
         *         and {@code shouldExit: false}.
         */
        @Override
        public CommandResult execute(TaskList tasks, TaskStorage storage) {
            if (tasks.isEmpty()) {
                return new CommandResult.Ok("No tasks added.", false);
            }

            StringBuilder msg = new StringBuilder();

            // remove all extra whitespace/newlines
            String targetLower = detailsSubstring.strip().replaceAll("\\s+", " ").toLowerCase();
            for (int i = 0; i < tasks.size(); i++) {
                // this won't throw because index is in range
                Task task = tasks.getTask(i).get();
                String detailsLower = task.getDetails().toLowerCase();
                if (detailsLower.contains(targetLower)) {
                    msg.append("%d. %s\n".formatted(i + 1, task));
                }
            }

            if (msg.isEmpty()) {
                return new CommandResult.Ok("No matching tasks found.", false);
            }

            return new CommandResult.Ok(msg.toString().stripTrailing(), false);
        }
    }

    /**
     * Executes the command, modifying the {@link TaskList} or using the
     * {@link TaskStorage} if necessary.
     * <p>
     * For example, `{@link AddTodoTask}::execute()` should add the task
     * to the list of tasks.
     *
     * @param tasks   The task list that the command may modify.
     * @param storage The task storage that the command may need to save data.
     * @return The {@link CommandResult} sum type. {@link CommandResult.Ok} if
     *         execution was successful, {@link CommandResult.Err} if execution was
     *         unsuccessful, and {@link CommandResult.Partial} if execution was
     *         successful, but something else failed.
     *         Each {@link CommandResult} stores the status message and whether the
     *         command loop should exit.
     */
    public abstract CommandResult execute(TaskList tasks, TaskStorage storage);
}
