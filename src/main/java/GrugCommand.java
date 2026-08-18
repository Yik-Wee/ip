import java.util.List;
import java.util.Set;

import task.DeadlineTask;
import task.EventTask;
import task.Task;
import task.TodoTask;

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
                throw new GrugCommandParserException.InvalidUsage("bye (with no arguments)");
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
                throw new GrugCommandParserException.InvalidUsage("list (with no arguments)");
            }
            return new GrugCommand.ListTasks();

        }
    }

    /**
     * Command to add a todo task to the list of tasks.
     *
     * @param task the task to add.
     */
    record AddTodoTask(TodoTask task) implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            tasks.add(task);

            System.out.println("added: " + task);
        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link AddTodoTask} command instance
         * @throws GrugCommandParserException If something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        public static AddTodoTask from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            ArgParser.ParsedArgs parsedArgs = ArgParser.parseArgs(args, Set.of());
            if (parsedArgs.inputs().isEmpty()) {
                throw new GrugCommandParserException.InvalidUsage("todo <details>");
            }
            String details = String.join(" ", parsedArgs.inputs());

            return new AddTodoTask(new TodoTask(details.toString()));
        }
    }

    /**
     * Command to add a task with deadline to the list of tasks.
     *
     * @param task The task to add.
     */
    record AddDeadlineTask(DeadlineTask task) implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            tasks.add(task);

            System.out.println("added: " + task);
        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link AddDeadlineTask} command instance
         * @throws GrugCommandParserException If something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        public static AddDeadlineTask from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            ArgParser.ParsedArgs parsedArgs = ArgParser.parseArgs(args, Set.of("/by"));
            List<String> detailsTokens = parsedArgs.inputs();
            List<String> deadlineTokens = parsedArgs.flagValues().get("/by");

            if (detailsTokens.isEmpty() || deadlineTokens == null || deadlineTokens.isEmpty()) {
                throw new GrugCommandParserException.InvalidUsage("deadline <details> /by <deadline>");
            }

            String details = String.join(" ", detailsTokens);
            String deadline = String.join(" ", deadlineTokens);

            return new AddDeadlineTask(new DeadlineTask(details.toString(), deadline.toString()));
        }
    }

    /**
     * Command to add an event task with a start and end date/time.
     *
     * @param task The task to add.
     */
    record AddEventTask(EventTask task) implements GrugCommand {
        @Override
        public void execute(List<Task> tasks) {
            tasks.add(task);

            System.out.println("added: " + task);

        }

        /**
         * Parses the argument list `args`.
         *
         * @param args The list of arguments read from the input, including the initial
         *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
         * @return A new {@link AddEventTask} command instance
         * @throws GrugCommandParserException If something went wrong parsing the args.
         * @throws IllegalArgumentException   If args is empty
         */
        public static AddEventTask from(String[] args) throws GrugCommandParserException {
            if (args.length == 0) {
                throw new IllegalArgumentException("`args[]` must be non-empty.");
            }

            ArgParser.ParsedArgs parsedArgs = ArgParser.parseArgs(args, Set.of("/from", "/to"));
            List<String> detailsTokens = parsedArgs.inputs();
            // these can be null or empty
            List<String> fromTokens = parsedArgs.flagValues().get("/from");
            List<String> toTokens = parsedArgs.flagValues().get("/to");

            // fromTokens and toTokens can be null if the user omits those flags.
            // But if the user passes the flag without the value (e.g. "event abc /from
            // /to") then fromTokens and toTokens will be empty lists, which is also
            // invalid.
            if (detailsTokens.isEmpty()
                    || fromTokens == null || fromTokens.isEmpty()
                    || toTokens == null || toTokens.isEmpty()) {
                throw new GrugCommandParserException.InvalidUsage("event <details> /from <from> /to <to>");
            }

            String details = String.join(" ", parsedArgs.inputs());
            String from = String.join(" ", parsedArgs.flagValues().get("/from"));
            String to = String.join(" ", parsedArgs.flagValues().get("/to"));

            return new AddEventTask(new EventTask(details, from, to));
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
                throw new GrugCommandParserException.InvalidUsage("mark <tasknum>");
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

        // no command given, e.g. user just presses enter without inputting anything, or
        // inputting whitespace
        if (input.isEmpty()) {
            return new GrugCommand.Empty();
        }

        // input must have had a non-whitespace character, so splitting it must give an
        // array with at least length 1, so args[0] will not throw
        String[] args = input.split("\\s+");
        String commandString = args[0].toLowerCase();

        return switch (commandString) {
            case "bye" -> Quit.from(args);
            case "list" -> ListTasks.from(args);
            case "mark" -> MarkTask.from(args);
            case "unmark" -> UnmarkTask.from(args);
            case "todo" -> AddTodoTask.from(args);
            case "deadline" -> AddDeadlineTask.from(args);
            case "event" -> AddEventTask.from(args);
            default -> new GrugCommand.AddTodoTask(new TodoTask(input));
        };
    }

    /**
     * Executes the command.
     *
     * For example, `{@link AddTodoTask}::execute()` should add the task
     * to the list of tasks and display an appropriate message.
     */
    public abstract void execute(List<Task> tasks);
}
