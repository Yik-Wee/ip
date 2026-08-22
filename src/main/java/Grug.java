import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import task.serde.TaskDeserializerException;

/**
 * Entrypoint of the Grug chatbot application
 */
public class Grug {
    private static final String DIALOG_SEP = "____________________________________________________________";
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

    private static final Scanner STDIN_SCANNER = new Scanner(System.in);

    private static TaskList tasks = new TaskList();
    private static TaskStorage storage = new TaskStorage("./tasks.txt");

    /**
     * Converts the user's input to the appropriate {@link GrugCommand} and
     * exeuctes the command.
     *
     * @return true if program should exit, false otherwise.
     */
    private static boolean handleUserInput() {
        System.out.print("> ");
        String input;
        try {
            // IllegalStateException should NOT be thrown since we should never close stdin
            input = STDIN_SCANNER.nextLine().strip();
        } catch (NoSuchElementException e) {
            // should just quit if e.g. ctrl+c is pressed
            System.out.println("Quitting...");
            return true;
        }

        GrugCommand command;
        try {
            command = GrugCommand.from(input);
        } catch (GrugCommandParserException e) {
            System.out.println(e.getMessage());
            System.out.println(DIALOG_SEP);
            return false;
        }

        command.execute(tasks, storage);

        System.out.println(DIALOG_SEP);

        boolean shouldExit = switch (command) {
            case GrugCommand.Quit() -> true;
            default -> false;
        };
        return shouldExit;
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println("Loading tasks from %s...".formatted(storage.getFilepath()));
        try {
            storage.loadTasks().forEach(task -> tasks.addTask(task));
            System.out.println("Loaded tasks from %s".formatted(storage.getFilepath()));
        } catch (IOException | TaskDeserializerException e) {
            System.out.println(
                    "Failed to load tasks from %s: %s".formatted(storage.getFilepath(), e.getMessage()));
        }
        System.out.println("Unga. Me Grug. What do? ('bye' to quit)");
        System.out.println(DIALOG_SEP);

        boolean done = false;
        while (!done) {
            done = handleUserInput();
        }
    }
}
