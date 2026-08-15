/**
 * Entrypoint of the Grug chatbot application
 */
public class Grug {
    public static final String DIALOG_SEP = "____________________________________________________________";

    public static void main(String[] args) {
        String banner = "  ▄████  ██▀███   █    ██   ▄████ \n"
                + " ██▒ ▀█▒▓██ ▒ ██▒ ██  ▓██▒ ██▒ ▀█▒\n"
                + "▒██░▄▄▄░▓██ ░▄█ ▒▓██  ▒██░▒██░▄▄▄░\n"
                + "░▓█  ██▓▒██▀▀█▄  ▓▓█  ░██░░▓█  ██▓\n"
                + "░▒▓███▀▒░██▓ ▒██▒▒▒█████▓ ░▒▓███▀▒\n"
                + " ░▒   ▒ ░ ▒▓ ░▒▓░░▒▓▒ ▒ ▒  ░▒   ▒ \n"
                + "  ░   ░   ░▒ ░ ▒░░░▒░ ░ ░   ░   ░ \n"
                + "░ ░   ░   ░░   ░  ░░░ ░ ░ ░ ░   ░ \n"
                + "      ░    ░        ░           ░ \n";

        System.out.println(banner);
        System.out.println("Unga. Me Grug. What do?");
        System.out.println(DIALOG_SEP);
        System.out.println("Unga. Bye. さよなら");
        System.out.println(DIALOG_SEP);
    }
}
