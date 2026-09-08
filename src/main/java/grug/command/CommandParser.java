package grug.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import grug.task.DeadlineTask;
import grug.task.EventTask;
import grug.task.Task;
import grug.task.TaskPriority;
import grug.task.TodoTask;

/**
 * Parser to parse the list of args given to Grug into the appropriate
 * {@link GrugCommand}.
 */
public class CommandParser {
    /**
     * Holds the result of the parsing.
     *
     * @param command    The main command.
     * @param inputs     The non-flag inputs passed to the command.
     * @param flagValues A Map that maps the flag to its values.
     */
    private record ParsedArgs(String command, List<String> inputs, Map<String, List<String>> flagValues) {
        /**
         * Checks that the {@code flagValues} contain all the flags specified by
         * {@code requiredFlags}, and their values are non-empty (i.e. values are
         * provided for that flag).
         *
         * @param requiredFlags The flags that are required.
         * @return {@code true} if {@code flagValues} contains all required flags whose
         *         values are non-empty, {@code false} otherwise.
         */
        public boolean hasNonEmptyFlagValues(Collection<String> requiredFlags) {
            boolean areAllValuesNonEmpty = requiredFlags
                    .stream()
                    .map(flagValues::get)
                    .allMatch(values -> values != null && !values.isEmpty());
            return areAllValuesNonEmpty;
        }
    }

    /**
     * Parses the list of args into the command, inputs and flag values, based on
     * the {@code flags} given.
     *
     * e.g.
     *
     * <pre>{@code
     * parseArgs(new String[] { "cmd", "in1", "in2", "/flag1", "value1", "/flag2", "value21", "value22" })
     * }</pre>
     *
     * is parsed into
     *
     * <pre>{@code
     * ParsedArgs(
     *     cmd,
     *     ["in1", "in2"],
     *     HashMap {
     *         "flag1": ["value1"],
     *         "flag2": ["value21", "value22"]
     *     }
     * );
     * }</pre>
     *
     * Also, command, inputs, and flagValues are guaranteed to be non-null. However,
     * {@code flagValues.get("/flag")} is guaranteed to be null if the flag did not
     * appear in {@code args[]}, and guaranteed to be an empty list if the flag
     * appeared without declaring its values, e.g. {@code cmd something /flag}
     *
     * @param args  The args to parse.
     * @param flags The flags we want to obtain the values of (e.g.
     *              {@code Set.of("/start", "/end")}).
     * @return {@link ParsedArgs} The command, inputs and flag values.
     */
    private static ParsedArgs parseArgs(String[] args) {
        if (args.length == 0) {
            return new ParsedArgs("", new ArrayList<>(0), new HashMap<>(0));
        }

        String command = args[0];
        List<String> inputs = new ArrayList<>();
        HashMap<String, List<String>> flagValues = new HashMap<>();

        if (args.length == 1) {
            return new ParsedArgs(command, inputs, flagValues);
        }

        String currentFlag = null;

        // since args[0] is the command, start from args[1] onwards
        for (int i = 1; i < args.length; i++) {
            String token = args[i];

            boolean isFlag = token.startsWith("/");
            if (isFlag) {
                // our token is a flag, add subsequent tokens to the flag's values
                currentFlag = token;
                flagValues.putIfAbsent(token, new ArrayList<>());
                continue;
            }

            if (currentFlag == null) {
                inputs.add(token);
            } else {
                flagValues.get(currentFlag).add(token);
            }
        }

        return new ParsedArgs(command, inputs, flagValues);
    }

    /**
     * Retrives the value of the {@code /priority} flag passed in and parses it into
     * the appropriate {@link TaskPriority} variant.
     *
     * @param parsedArgs The parsed arguments from the user input.
     * @param default    The default priority if no {@code /priority}
     *                   was given.
     * @return The appropriate {@link TaskPriority} variant based on the
     *         {@code /priority} flag's value if it was given,
     *         {@code default} if no {@code /priority} was given.
     * @throws GrugCommandParserException If the number of non-empty
     *                                    {@code /priority} flags given is not
     *                                    exactly 1, or the priority is invalid.
     */
    private static TaskPriority parseTaskPriorityFrom(ParsedArgs parsedArgs, TaskPriority defaultPriority)
            throws GrugCommandParserException {
        List<String> priorities = parsedArgs.flagValues().get("/priority");
        if (priorities == null) {
            return defaultPriority;
        }

        if (priorities.isEmpty()) {
            String reason = "No priority specified. priority must be one of <opt | low | med | hig | urg>";
            throw new GrugCommandParserException.InvalidArgument("/priority", reason);
        }

        if (priorities.size() != 1) {
            throw new GrugCommandParserException.InvalidArgument(
                    "/priority", "A task can only have 1 priority");
        }

        String priorityName = priorities.get(0);

        try {
            return TaskPriority.createFromDisplayName(priorityName);
        } catch (IllegalArgumentException e) {
            String reason = "priority must be one of <opt | low | med | hig | urg>";
            throw new GrugCommandParserException.InvalidArgument("/priority", reason);
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
    public static GrugCommand parseInput(String input) throws GrugCommandParserException {
        input = input.strip();

        // no command given, e.g. user just presses enter without inputting anything, or
        // inputting whitespace
        if (input.isEmpty()) {
            return new GrugCommand.EmptyCommand();
        }

        // input must have had a non-whitespace character, so splitting it must give an
        // array with at least length 1, so args[0] will not throw
        String[] args = input.split("\\s+");
        assert args.length > 0;
        String commandString = args[0].toLowerCase();

        return switch (commandString) {
            case "bye" -> parseQuitCommand(args);
            case "list" -> parseListCommand(args);
            case "mark" -> parseMarkCommand(args);
            case "unmark" -> parseUnmarkCommand(args);
            case "todo" -> parseTodoCommand(args);
            case "deadline" -> parseDeadlineCommand(args);
            case "event" -> parseEventCommand(args);
            case "delete" -> parseDeleteCommand(args);
            case "find-on" -> parseFindByDateCommand(args);
            case "find" -> parseFindByDetailsCommand(args);
            case "set-priority" -> parseSetPriorityCommand(args);
            default -> throw new GrugCommandParserException.UnknownCommand(commandString);
        };
    }

    /**
     * Requires {@code args} to be non-empty and throws if it is empty.
     *
     * @param args The argument list which should be non-empty.
     * @throws IllegalArgumentException If {@code args} is empty.
     */
    private static void requireNonEmptyArgs(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.QuitCommand} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.QuitCommand parseQuitCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        // only `bye` is valid, with no additional args
        if (args.length > 1) {
            throw new GrugCommandParserException.InvalidUsage("bye (with no arguments)");
        }
        return new GrugCommand.QuitCommand();
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.ListTasksCommand} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.ListTasksCommand parseListCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        // only `list` is valid, with no additional args
        if (args.length > 1) {
            throw new GrugCommandParserException.InvalidUsage("list (with no arguments)");
        }
        return new GrugCommand.ListTasksCommand();
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.AddTodoTaskCommand} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.AddTaskCommand parseTodoCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args);
        if (parsedArgs.inputs().isEmpty()) {
            throw new GrugCommandParserException.InvalidUsage("todo <details> [/priority opt|low|med|hig|urg]");
        }

        String details = String.join(" ", parsedArgs.inputs());
        TaskPriority priority = parseTaskPriorityFrom(parsedArgs, TaskPriority.DEFAULT);

        TodoTask todoTask = new TodoTask(details);
        todoTask.setPriority(priority);
        return new GrugCommand.AddTaskCommand(todoTask);
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.AddDeadlineTaskCommand} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.AddTaskCommand parseDeadlineCommand(String[] args)
            throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        Collection<String> requiredFlags = Set.of("/by");
        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args);

        if (parsedArgs.inputs().isEmpty() || !parsedArgs.hasNonEmptyFlagValues(requiredFlags)) {
            String usage = "deadline <details> /by <%s>".formatted(DeadlineTask.DATE_TIME_INPUT_PATTERN);
            throw new GrugCommandParserException.InvalidUsage(usage);
        }

        String details = String.join(" ", parsedArgs.inputs());
        String deadline = String.join(" ", parsedArgs.flagValues().get("/by"));
        TaskPriority priority = parseTaskPriorityFrom(parsedArgs, TaskPriority.DEFAULT);

        try {
            DeadlineTask deadlineTask = new DeadlineTask(details, deadline);
            deadlineTask.setPriority(priority);
            return new GrugCommand.AddTaskCommand(deadlineTask);
        } catch (DateTimeParseException e) {
            String reason = "deadline must be in the format " + DeadlineTask.DATE_TIME_INPUT_PATTERN;
            throw new GrugCommandParserException.InvalidArgument("deadline", reason);
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.AddEventTaskCommand} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.AddTaskCommand parseEventCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        Set<String> requiredFlags = Set.of("/from", "/to");
        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args);

        if (parsedArgs.inputs().isEmpty() || !parsedArgs.hasNonEmptyFlagValues(requiredFlags)) {
            String usage = "event <details> /from <from> /to <to>";
            throw new GrugCommandParserException.InvalidUsage(usage);
        }

        String details = String.join(" ", parsedArgs.inputs());
        String from = String.join(" ", parsedArgs.flagValues().get("/from"));
        String to = String.join(" ", parsedArgs.flagValues().get("/to"));
        TaskPriority priority = parseTaskPriorityFrom(parsedArgs, TaskPriority.DEFAULT);

        try {
            EventTask eventTask = new EventTask(details, from, to);
            eventTask.setPriority(priority);
            return new GrugCommand.AddTaskCommand(eventTask);
        } catch (DateTimeParseException e) {
            String reason = "must be in the format " + EventTask.DATE_TIME_INPUT_PATTERN;
            throw new GrugCommandParserException.InvalidArgument("from / to", reason);
        } catch (IllegalArgumentException e) {
            String reason = "from date/time `%s` must not occur after to date/time `%s`".formatted(from, to);
            throw new GrugCommandParserException.InvalidArgument("from / to", reason);
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.MarkTaskCommand} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.MarkTaskCommand parseMarkCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        // only `mark <tasknum>` is valid
        if (args.length != 2) {
            throw new GrugCommandParserException.InvalidUsage("mark <tasknum>");
        }

        String taskNumText = args[1];
        int taskNum;
        try {
            taskNum = Integer.parseInt(taskNumText);
        } catch (NumberFormatException e) {
            String reason = "tasknum must be an integer";
            throw new GrugCommandParserException.InvalidArgument("mark <tasknum>", reason);
        }
        return new GrugCommand.MarkTaskCommand(taskNum);
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.UnmarkTaskCommand} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.UnmarkTaskCommand parseUnmarkCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        // only `unmark <tasknum>` is valid
        if (args.length != 2) {
            String reason = "must provide exactly 1 integer tasknum";
            throw new GrugCommandParserException.InvalidArgument("unmark <tasknum>", reason);
        }

        String taskNumText = args[1];
        int taskNum;
        try {
            taskNum = Integer.parseInt(taskNumText);
        } catch (NumberFormatException e) {
            String reason = "tasknum must be an integer";
            throw new GrugCommandParserException.InvalidArgument("unmark <tasknum>", reason);
        }
        return new GrugCommand.UnmarkTaskCommand(taskNum);
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.DeleteTaskCommand} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.DeleteTaskCommand parseDeleteCommand(String[] args) throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        // only `delete <tasknum>` is valid
        if (args.length != 2) {
            throw new GrugCommandParserException.InvalidUsage("delete <tasknum>");
        }

        String taskNumText = args[1];
        int taskNum;
        try {
            taskNum = Integer.parseInt(taskNumText);
        } catch (NumberFormatException e) {
            String reason = "tasknum must be an integer";
            throw new GrugCommandParserException.InvalidArgument("delete <tasknum>", reason);
        }
        return new GrugCommand.DeleteTaskCommand(taskNum);
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.FindTasksByDateCommand} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.FindTasksByDateCommand parseFindByDateCommand(String[] args)
            throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        // command without any arguments passed
        if (args.length == 1) {
            String usage = "find-on <%s>".formatted(Task.DATE_TIME_INPUT_PATTERN);
            throw new GrugCommandParserException.InvalidUsage(usage);
        }

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args);
        List<String> datetimeTokens = parsedArgs.inputs();
        String target = String.join(" ", datetimeTokens);

        try {
            LocalDate targetDateTime = LocalDate.parse(target, Task.DATE_TIME_INPUT_FORMATTER);
            return new GrugCommand.FindTasksByDateCommand(targetDateTime);
        } catch (DateTimeParseException e) {
            String reason = "must be in the format %s\n(but the time provided is ignored)"
                    .formatted(Task.DATE_TIME_INPUT_PATTERN);
            throw new GrugCommandParserException.InvalidArgument("find-on", reason);
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.FindTasksByDetailsCommand} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.FindTasksByDetailsCommand parseFindByDetailsCommand(String[] args)
            throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args);
        if (parsedArgs.inputs().isEmpty()) {
            throw new GrugCommandParserException.InvalidUsage("find <details>");
        }
        String details = String.join(" ", parsedArgs.inputs());

        return new GrugCommand.FindTasksByDetailsCommand(details);
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.SetPriorityCommand} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.SetPriorityCommand parseSetPriorityCommand(String[] args)
            throws GrugCommandParserException {
        requireNonEmptyArgs(args);

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args);

        TaskPriority newPriority = parseTaskPriorityFrom(parsedArgs, null);
        if (parsedArgs.inputs().size() != 1 || newPriority == null) {
            String usage = "set-priority <tasknum> /priority <opt|low|med|hig|urg>";
            System.out.println(parsedArgs);
            throw new GrugCommandParserException.InvalidUsage(usage);
        }

        String taskNumText = parsedArgs.inputs().get(0);
        int taskNum;
        try {
            taskNum = Integer.parseInt(taskNumText);
        } catch (NumberFormatException e) {
            String reason = "tasknum %s is not an integer".formatted(taskNumText);
            throw new GrugCommandParserException.InvalidArgument("set-priority <tasknum>", reason);
        }

        return new GrugCommand.SetPriorityCommand(taskNum, newPriority);
    }
}
