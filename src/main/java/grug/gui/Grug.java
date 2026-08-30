package grug.gui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import grug.command.CommandParser;
import grug.command.CommandResult;
import grug.command.GrugCommand;
import grug.command.GrugCommandParserException;
import grug.storage.TaskStorage;
import grug.storage.serde.TaskDeserializerException;
import grug.task.TaskList;
import grug.ui.Ui;

/**
 * Exposes the API to control the Grug chatbot.
 */
public class Grug {
    public static final String DEFAULT_STORAGE_FILE = "./tasks.txt";
    private static final String BANNER = """
            ▄████  ██▀███   █    ██   ▄████
            ██▒ ▀█▒▓██ ▒ ██▒ ██  ▓██▒ ██▒ ▀█▒
            ▒██░▄▄▄░▓██ ░▄█ ▒▓██  ▒██░▒██░▄▄▄░
            ░▓█  ██▓▒██▀▀█▄  ▓▓█  ░██░░▓█  ██▓
            ░▒▓███▀▒░██▓ ▒██▒▒▒█████▓ ░▒▓███▀▒
            ░▒   ▒ ░ ▒▓ ░▒▓░░▒▓▒ ▒ ▒  ░▒   ▒
            ░   ░   ░▒ ░ ▒░░░▒░ ░ ░   ░   ░
            ░ ░   ░   ░░   ░  ░░░ ░ ░ ░ ░   ░
                ░    ░        ░           ░
            """.stripIndent();

    private Ui ui;
    private TaskList tasks;
    private TaskStorage storage;

    /**
     * Creates a new Grug chatbot instance which saves and loads tasks from the
     * {@code filepath}.
     *
     * @param filepath The filepath to save and load tasks data from.
     * @param ui       The {@link Ui} instance that specifies where to read from
     *                 (e.g. {@code System.in}) and where to write to (e.g.
     *                 {@code System.out}).
     */
    public Grug(String filepath, Ui ui) {
        this.tasks = new TaskList();
        this.storage = new TaskStorage(filepath);
        this.ui = ui;
    }

    /**
     * Creates a new Grug chatbot which saves and loads tasks from
     * {@link #DEFAULT_STORAGE_FILE}, and with a {@link Ui} that reads from
     * {@code System.in} and outputs to {@code System.out}.
     */
    public Grug() {
        Ui ui = new Ui(new Scanner(System.in), new PrintWriter(System.out));
        this(DEFAULT_STORAGE_FILE, ui);
    }

    /**
     * Parses raw user input and maps it to the appropriate {@link GrugCommand}
     * variant.
     *
     * @param input the raw string input from the user's terminal.
     * @return the corresponding command variant.
     * @throws GrugCommandParserException if there was an error parsing the input.
     * @see CommandParser#parseInput(String)
     */
    public static GrugCommand parseInput(String input) throws GrugCommandParserException {
        return CommandParser.parseInput(input);
    }

    /**
     * Executes the command and may mutate the state of the instance's task list and
     * storage depending on the command.
     *
     * @param command The command to execute.
     * @return The result of executing the command, including the message and
     *         whether the program should exit.
     */
    public CommandResult execute(GrugCommand command) {
        return command.execute(tasks, storage);
    }

    /**
     * Converts the user's input to the appropriate {@link GrugCommand} and
     * executes the command.
     *
     * @return true if program should exit, false otherwise.
     */
    public boolean handleUserInput(String input) {
        GrugCommand command;
        try {
            command = CommandParser.parseInput(input);
        } catch (GrugCommandParserException e) {
            ui.display(e.getMessage());
            return false;
        }

        CommandResult res = command.execute(tasks, storage);

        switch (res) {
            case CommandResult.Ok(String message, boolean shouldExit) -> {
                ui.display(message);
                return shouldExit;
            }
            case CommandResult.Err(String message, boolean shouldExit) -> {
                ui.displayError(message);
                return shouldExit;
            }
            case CommandResult.Partial(String message, boolean shouldExit) -> {
                ui.displayWarning(message);
                return shouldExit;
            }
        }
    }

    /**
     * Loads tasks from the storage and displays any errors.
     */
    private void loadTasks() {
        ui.display("Loading tasks from %s...", storage.getFilepath());
        try {
            this.tasks = new TaskList(storage.loadTasks());
            ui.display("Loaded tasks from %s", storage.getFilepath());
        } catch (IOException | TaskDeserializerException e) {
            ui.display("Failed to load tasks from %s: %s", storage.getFilepath(), e.getMessage());
        }
    }

    /**
     * Displays the banner and greets the user.
     */
    public void greet() {
        ui.display(BANNER);
        ui.display("Unga. Me Grug. What do? ('bye' to quit)");
        ui.displaySeparator();
    }

    /**
     * Runs the Grug program.
     */
    public void run() {
        this.loadTasks();
        this.greet();
        ui.runReadInputLoop(this::handleUserInput);
    }

    public static void main(String[] args) {
        new Grug().run();
    }
}
