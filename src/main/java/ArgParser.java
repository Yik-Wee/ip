import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Parser to parse the list of args given to Grug into the command, inputs and
 * flags
 */
public class ArgParser {
    record ParsedArgs(String command, List<String> inputs, HashMap<String, List<String>> flagValues) {
    };

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
     * @param args  The args to parse.
     * @param flags The flags we want to obtain the values of (e.g.
     *              {@code Set.of("/start", "/end")}).
     * @return {@link ParsedArgs} The command, inputs and flag values.
     */
    public static ParsedArgs parseArgs(String[] args, Collection<String> flags) {
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
}
