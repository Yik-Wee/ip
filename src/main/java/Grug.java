import java.util.ArrayList;
import java.util.Scanner;

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

    private static ArrayList<Task> tasks = new ArrayList<>(100);

    /**
     * Converts the user's input to the appropriate {@link GrugCommand} and exeuctes
     * the command.
     *
     * @return true if program should exit, false otherwise.
     */
    private static boolean handleUserInput() {
        System.out.print("> ");
        String input = STDIN_SCANNER.nextLine().strip();

        try {
            GrugCommand command = GrugCommand.from(input);
            command.execute(tasks);
            System.out.println(DIALOG_SEP);

            boolean shouldExit = switch (command) {
                case GrugCommand.Quit() -> true;
                default -> false;
            };
            return shouldExit;
        } catch (GrugCommandParserException e) {
            System.out.println(e.getMessage());
            System.out.println(DIALOG_SEP);
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println("Unga. Me Grug. What do? ('bye' to quit)");
        System.out.println(DIALOG_SEP);

        boolean done = false;
        while (!done) {
            done = handleUserInput();
        }
    }
}
