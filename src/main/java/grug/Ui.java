package grug;

import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Represents a display that can be read from and written to.
 */
public class Ui {
    public static final String DIALOG_SEP = "____________________________________________________________";
    private final Scanner reader;
    private final PrintWriter writer;

    /**
     * Creates a new display that reads with the scanner and writes with the writer.
     *
     * @param reader The {@link Scanner} to read from.
     * @param writer The {@link PrintWriter} to write to.
     */
    public Ui(Scanner reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * Displays the text and then a newline.
     *
     * @param message The text to display.
     */
    public void display(String message) {
        this.writer.println(message);
        this.writer.flush();
    }

    /**
     * Displays the text formatted by args, then a newline.
     *
     * @param fmtString The text to format then display.
     * @param args      The arguments to the format string.
     */
    public void display(String fmtString, Object... args) {
        this.writer.println(fmtString.formatted(args));
        this.writer.flush();
    }

    /**
     * Displays the message in red.
     *
     * @param message The messgae to display.
     */
    public void displayError(String message) {
        this.writer.print("\033[31m");
        this.writer.println(message);
        this.writer.print("\033[0m");
        this.writer.flush();
    }

    /**
     * Displays the message in yellow.
     *
     * @param message The messgae to display.
     */
    public void displayWarning(String message) {
        this.writer.print("\033[33m");
        this.writer.println(message);
        this.writer.print("\033[0m");
        this.writer.flush();
    }

    /**
     * Displays the {@link #DIALOG_SEP} separator, then a newline.
     */
    public void displaySeparator() {
        this.writer.println(DIALOG_SEP);
        this.writer.flush();
    }

    /**
     * Displays the prompt (without adding a newline), then reads the input.
     *
     * @param prompt The prompt to display, e.g. {@code "Enter a word: "}.
     * @return An optional containing stripped the user's input, or an empty
     *         optional if no line was found (e.g. ctrl+c pressed).
     * @throws IllegalStateException If the reader was closed.
     */
    public Optional<String> readInput(String prompt) {
        this.writer.print(prompt);
        this.writer.flush();

        try {
            return Optional.of(this.reader.nextLine().strip());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    /**
     * Reads the input in a loop, passing the input to the input handler.
     *
     * @param inputHandlerCallback The callback that takes the user's stripped input
     *                             and handles it. It should return {@code true} if
     *                             the loop should stop, {@code false} otherwise.
     */
    public void runReadInputLoop(Function<String, Boolean> inputHandlerCallback) {
        boolean isDone = false;
        while (!isDone) {
            Optional<String> inputOrEof = this.readInput("> ");
            // EOF, e.g. ctrl+c, ctrl+z, etc.
            if (inputOrEof.isEmpty()) {
                isDone = true;
                continue;
            }

            String input = inputOrEof.get();
            isDone = inputHandlerCallback.apply(input);
            this.displaySeparator();
        }
    }
}
