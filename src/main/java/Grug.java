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

    private static ArrayList<String> tasks = new ArrayList<>(100);

    /**
     * Converts the user's input to the appropriate {@link GrugCommand} and exeuctes
     * the command.
     *
     * @return the command that was parsed from the user input
     */
    private static GrugCommand handleUserInput() {
        System.out.print("> ");
        String input = STDIN_SCANNER.nextLine().strip();

        GrugCommand command = GrugCommand.from(input);
        command.execute(tasks);
        System.out.println(DIALOG_SEP);

        return command;
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println("Unga. Me Grug. What do? ('bye' to quit)");
        System.out.println(DIALOG_SEP);

        boolean done = false;
        while (!done) {
            GrugCommand command = handleUserInput();
            switch (command) {
                case GrugCommand.Quit() -> done = true;
                default -> {
                }
            }
        }
    }
}
