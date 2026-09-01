package grug.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import grug.task.DeadlineTask;
import grug.task.EventTask;
import grug.task.Task;
import grug.task.TodoTask;

/**
 * Parser to parse the list of args given to Grug into the appropriate
 * {@link GrugCommand}.
 */
public class CommandParser {
    /**
     * Immutable record to hold the result of the parsing.
     *
     * @param command    The main command.
     * @param inputs     The non-flag inputs passed to the command.
     * @param flagValues A HashMap that maps the flag to its values.
     */
    private record ParsedArgs(String command, List<String> inputs, HashMap<String, List<String>> flagValues) {
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
    private static ParsedArgs parseArgs(String[] args, Collection<String> flags) {
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

            boolean isFlag = flags.contains(token);
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
            return new GrugCommand.Empty();
        }

        // input must have had a non-whitespace character, so splitting it must give an
        // array with at least length 1, so args[0] will not throw
        String[] args = input.split("\\s+");
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
            case "find" -> parseFindByDetails(args);
            default -> throw new GrugCommandParserException.UnknownCommand(commandString);
        };
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.Quit} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.Quit parseQuitCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        // only `bye` is valid, with no additional args
        if (args.length > 1) {
            throw new GrugCommandParserException.InvalidUsage("bye (with no arguments)");
        }
        return new GrugCommand.Quit();
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.ListTasks} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.ListTasks parseListCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        // only `list` is valid, with no additional args
        if (args.length > 1) {
            throw new GrugCommandParserException.InvalidUsage("list (with no arguments)");
        }
        return new GrugCommand.ListTasks();
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.AddTodoTask} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.AddTodoTask parseTodoCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args, Set.of());
        if (parsedArgs.inputs().isEmpty()) {
            throw new GrugCommandParserException.InvalidUsage("todo <details>");
        }
        String details = String.join(" ", parsedArgs.inputs());

        return new GrugCommand.AddTodoTask(new TodoTask(details.toString()));
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.AddDeadlineTask} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.AddDeadlineTask parseDeadlineCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args, Set.of("/by"));
        List<String> detailsTokens = parsedArgs.inputs();
        List<String> deadlineTokens = parsedArgs.flagValues().get("/by");

        if (detailsTokens.isEmpty() || deadlineTokens == null || deadlineTokens.isEmpty()) {
            throw new GrugCommandParserException.InvalidUsage(
                    "deadline <details> /by <%s>".formatted(DeadlineTask.DATE_TIME_INPUT_PATTERN));
        }

        String details = String.join(" ", detailsTokens);
        String deadline = String.join(" ", deadlineTokens);

        try {
            return new GrugCommand.AddDeadlineTask(new DeadlineTask(details.toString(), deadline.toString()));
        } catch (DateTimeParseException e) {
            throw new GrugCommandParserException.InvalidArgument(
                    "deadline",
                    "deadline must be in the format " + DeadlineTask.DATE_TIME_INPUT_PATTERN);
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.AddEventTask} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.AddEventTask parseEventCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args, Set.of("/from", "/to"));
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

        try {
            return new GrugCommand.AddEventTask(new EventTask(details, from, to));
        } catch (DateTimeParseException e) {
            throw new GrugCommandParserException.InvalidArgument(
                    "from / to",
                    "must be in the format " + EventTask.DATE_TIME_INPUT_PATTERN);
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.MarkTask} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.MarkTask parseMarkCommand(String[] args) throws GrugCommandParserException {
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

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.UnmarkTask} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.UnmarkTask parseUnmarkCommand(String[] args) throws GrugCommandParserException {
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

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.DeleteTask} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.DeleteTask parseDeleteCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty");
        }

        // only `delete <tasknum>` is valid
        if (args.length != 2) {
            throw new GrugCommandParserException.InvalidUsage("delete <tasknum>");
        }

        String rhs = args[1];
        int taskNum;
        try {
            taskNum = Integer.parseInt(rhs);
        } catch (NumberFormatException e) {
            throw new GrugCommandParserException.InvalidArgument(
                    "delete <tasknum>",
                    "tasknum must be an integer");
        }
        return new GrugCommand.DeleteTask(taskNum);
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.FindTasksByDate} command instance.
     * @throws GrugCommandParserException if something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.FindTasksByDate parseFindByDateCommand(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        // command without any arguments passed
        if (args.length == 1) {
            throw new GrugCommandParserException.InvalidUsage(
                    "find-on <%s>".formatted(Task.DATE_TIME_INPUT_PATTERN));
        }

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args, Set.of("/from", "/to"));
        List<String> datetimeTokens = parsedArgs.inputs();
        String target = String.join(" ", datetimeTokens);

        try {
            LocalDate targetDateTime = LocalDate.parse(target, Task.DATE_TIME_INPUT_FORMATTER);
            return new GrugCommand.FindTasksByDate(targetDateTime);
        } catch (DateTimeParseException e) {
            throw new GrugCommandParserException.InvalidArgument(
                    "target datetime",
                    "must be in the format %s\n(but the time provided is ignored)"
                            .formatted(Task.DATE_TIME_INPUT_PATTERN));
        }
    }

    /**
     * Parses the argument list `args`.
     *
     * @param args The list of arguments read from the input, including the initial
     *             command to quit as well (e.g. `{ "cmd", "arg1", "arg2", ... }`).
     * @return A new {@link GrugCommand.FindTasksByDetails} command instance.
     * @throws GrugCommandParserException If something went wrong parsing the args.
     * @throws IllegalArgumentException   If args is empty.
     */
    private static GrugCommand.FindTasksByDetails parseFindByDetails(String[] args) throws GrugCommandParserException {
        if (args.length == 0) {
            throw new IllegalArgumentException("`args[]` must be non-empty.");
        }

        CommandParser.ParsedArgs parsedArgs = CommandParser.parseArgs(args, Set.of());
        if (parsedArgs.inputs().isEmpty()) {
            throw new GrugCommandParserException.InvalidUsage("find <details>");
        }
        String details = String.join(" ", parsedArgs.inputs());

        return new GrugCommand.FindTasksByDetails(details);
    }
}
