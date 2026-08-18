import java.util.List;

import task.Task;

/**
 * Algebraic Data Type that represents the user's command.
 */
public sealed interface GrugCommand {
    /**
     * Represents no command given.
     */
    record Empty() implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
        }
    }

    /**
     * Command to quit the program.
     */
    record Quit() implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            System.out.println("Unga. Bye. さよなら");
        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link Quit} command instance
         * @throws GrugCommandParserException If something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        protected static Quit from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            // only `bye` is valid, with no additional args
            if (args.length > 1) {
                throw new GrugCommandParserException.InvalidArgument(
                        "bye", "`bye` command expects no arguments");
            }
            return new GrugCommand.Quit();
        }
    }

    /**
     * Command to list all tasks.
     */
    record ListTasks() implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            if (tasks.size() == 0) {
                System.out.println("No tasks added.");
                return;
            }

            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("%d. %s".formatted(i + 1, tasks.get(i)));
            }
        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link ListTasks} command instance
         * @throws GrugCommandParserException if something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        public static ListTasks from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            // only `list` is valid, with no additional args
            if (args.length > 1) {
                throw new GrugCommandParserException.InvalidArgument(
                        "list", "`list` command expects no arguments");
            }
            return new GrugCommand.ListTasks();

        }
    }

    /**
     * Command to add a task to the list of tasks to do.
     *
     * @param task the task to add.
     */
    record AddTask(Task task) implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            tasks.add(task);

            System.out.println("added: " + task);
        }
    }

    /**
     * Command to mark a task as complete.
     *
     * @param taskNum The 1-based task number.
     */
    record MarkTask(int taskNum) implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            int taskIdx = taskNum - 1;

            // ensure index is in range so .get() does not throw
            if (taskIdx < 0 || taskIdx >= tasks.size()) {
                System.out.println("Can't find task number %d".formatted(taskNum));
                return;
            }

            Task task = tasks.get(taskIdx);
            task.markComplete();
            System.out.println("Updated task %d: %s".formatted(taskNum, task));
        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link MarkTask} command instance
         * @throws GrugCommandParserException if something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        public static MarkTask from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            // only `mark <tasknum>` is valid
            if (args.length != 2) {
                throw new GrugCommandParserException.InvalidArgument(
                        "mark <tasknum>",
                        "must provide exactly 1 integer tasknum");
            }

            String rhs = args[1];
            int taskNum;
            try {
                taskNum = Integer.parseInt(rhs);
            } catch (NumberFormatException e) {
                throw new GrugCommandParserException.InvalidArgument(
                        "mark <tasknum>",
                        "tasknum must be an integer");
            }
            return new GrugCommand.MarkTask(taskNum);
        }
    }

    /**
     * Command to unmark a task (i.e. mark as incomplete).
     *
     * @param taskNum The 1-based task number.
     */
    record UnmarkTask(int taskNum) implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            int taskIdx = taskNum - 1;

            // ensure index is in range so .get() does not throw
            if (taskIdx < 0 || taskIdx >= tasks.size()) {
                System.out.println("Can't find task number %d".formatted(taskNum));
                return;
            }

            Task task = tasks.get(taskIdx);
            task.markIncomplete();
            System.out.println("Updated task %d: %s".formatted(taskNum, task));
        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link UnmarkTask} command instance
         * @throws GrugCommandParserException if something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        public static UnmarkTask from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            // only `unmark <tasknum>` is valid
            if (args.length != 2) {
                throw new GrugCommandParserException.InvalidArgument(
                        "unmark <tasknum>",
                        "must provide exactly 1 integer tasknum");
            }

            String rhs = args[1];
            int taskNum;
            try {
                taskNum = Integer.parseInt(rhs);
            } catch (NumberFormatException e) {
                throw new GrugCommandParserException.InvalidArgument(
                        "unmark <tasknum>",
                        "tasknum must be an integer");
            }
            return new GrugCommand.UnmarkTask(taskNum);
        }
    }

    /**
     * Parses raw user input and maps it to the appropriate {@link GrugCommand}
     * variant.
     *
     * @param input the raw string input from the user's terminal.
     * @return the corresponding command variant.
     * @throws GrugCommandParserException if there was an error parsing the input.
     */
    public static GrugCommand from(String input) throws GrugCommandParserException {
        input = input.strip();
        String[] args = input.split("\\s+");

        // no command given, e.g. user just presses enter without inputting anything
        if (args.length == 0) {
            return new GrugCommand.Empty();
        }

        String commandString = args[0].toLowerCase();

        return switch (commandString) {
            case "bye" -> Quit.from(args);
            case "list" -> ListTasks.from(args);
            case "mark" -> MarkTask.from(args);
            case "unmark" -> UnmarkTask.from(args);
            default -> new GrugCommand.AddTask(new Task(input));
        };
    }

    /**
     * Executes the command.
     *
     * For example, `{@link AddTask}::execute()` should add the task
     * to the list of tasks and display an appropriate message.
     */
    public abstract void execute(List<Task> tasks);
}
