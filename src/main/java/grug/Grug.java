package grug;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import grug.task.serde.TaskDeserializerException;

/**
 * Entrypoint of the Grug chatbot application
 */
public class Grug {
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
     * Creates a new Grug program instance.
     */
    public Grug(String filepath) {
        this.tasks = new TaskList();
        this.storage = new TaskStorage(filepath);
        this.ui = new Ui(new Scanner(System.in), new PrintWriter(System.out));
    }

    /**
     * Converts the user's input to the appropriate {@link GrugCommand} and
     * exeuctes the command.
     *
     * @return true if program should exit, false otherwise.
     */
    private boolean handleUserInput(String input) {
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
    private void greet() {
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
        new Grug("./tasks.txt").run();
    }
}
