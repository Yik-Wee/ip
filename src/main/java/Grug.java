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

    private static enum GrugCommand {
        Echo,
        Quit
    }

    /**
     * Echoes back the user's input. exits when the user types "bye".
     *
     * @return true if user quits, false otherwise
     */
    private static GrugCommand handleUserInput() {
        System.out.print("> ");
        String input = STDIN_SCANNER.nextLine().strip();

        if (input.equals("bye")) {
            return GrugCommand.Quit;
        }

        System.out.println(input);
        System.out.println(DIALOG_SEP);
        return GrugCommand.Echo;
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println("Unga. Me Grug. What do? ('bye' to quit)");
        System.out.println(DIALOG_SEP);

        boolean done = false;
        while (!done) {
            GrugCommand command = handleUserInput();
            if (command == GrugCommand.Quit) {
                done = true;
            }
        }

        System.out.println("Unga. Bye. さよなら");
        System.out.println(DIALOG_SEP);
    }
}
