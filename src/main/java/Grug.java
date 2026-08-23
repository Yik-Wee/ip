import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import task.serde.TaskDeserializerException;

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
            command = GrugCommand.from(input);
        } catch (GrugCommandParserException e) {
            ui.display(e.getMessage());
            return false;
        }

        command.execute(tasks, storage);

        boolean shouldExit = switch (command) {
            case GrugCommand.Quit() -> true;
            default -> false;
        };
        return shouldExit;
    }

    /**
     * Loads tasks from the storage and displays any errors.
     */
    private void loadTasks() {
        ui.display("Loading tasks from %s...", storage.getFilepath());
        try {
            storage.loadTasks().forEach(task -> tasks.addTask(task));
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
        ui.displaySeperator();
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
