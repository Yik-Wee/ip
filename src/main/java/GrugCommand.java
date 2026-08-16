import java.util.List;

/**
 * Algebraic Data Type that represents the user's command.
 */
public sealed interface GrugCommand {
    /**
     * Command to quit the program.
     */
    record Quit() implements GrugCommand {
        @Override
        public void execute(List<String> tasks) {
            System.out.println("Unga. Bye. さよなら");
        }
    }

    /**
     * Command to list all tasks.
     */
    record ListTasks() implements GrugCommand {
        @Override
        public void execute(List<String> tasks) {
            if (tasks.size() == 0) {
                System.out.println("No tasks added.");
                return;
            }

            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("%d. %s".formatted(i + 1, tasks.get(i)));
            }
        }
    }

    /**
     * Command to add a task to the list of tasks to do.
     *
     * @param task the task to add.
     */
    record AddTask(String task) implements GrugCommand {
        @Override
        public void execute(List<String> tasks) {
            tasks.add(task);

            System.out.println("added: " + task);
        }
    }

    /**
     * Parses raw user input and maps it to the appropriate {@link GrugCommand}
     * variant.
     *
     * @param input the raw string input from the user's terminal
     * @return the corresponding command variant
     */
    public static GrugCommand from(String input) {
        // command to exit program
        if (input.equalsIgnoreCase("bye")) {
            return new GrugCommand.Quit();
        }

        // command to list tasks
        if (input.equalsIgnoreCase("list")) {
            return new GrugCommand.ListTasks();
        }

        // command to add a task
        return new GrugCommand.AddTask(input);
    }

    /**
     * Executes the command.
     *
     * For example, `{@link AddTask}::execute()` should add the task
     * to the list of tasks and display an appropriate message.
     */
    public abstract void execute(List<String> tasks);
}
