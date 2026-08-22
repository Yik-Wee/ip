import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import task.Task;
import task.serde.TaskSerializer;

/**
 * Utility class used to read tasks from and write tasks to a single file.
 */
public final class TaskStorage {
    private final String filepath;

    /**
     * Creates a new task storage that uses the {@code filepath} as a save file.
     *
     * @param filepath The path to the save file for the storage to read and write
     *                 from / to.
     */
    public TaskStorage(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return this.filepath;
    }

    /**
     * Saves the tasks to the file specified by the storage filepath
     * {@link #getFilepath()}.
     *
     * @throws IOException If the named file exists but is a directory rather than a
     *                     regular file, does not exist but cannot be created, or
     *                     cannot be opened or written to for any other reason.
     */
    public void saveTasks(List<Task> tasks) throws IOException {
        // the writer is automatically closed at the end of this try-resource block
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.filepath))) {
            // we expect at most around 100 tasks, so it is easier to just replace the
            // contents of the entire file every save, rather than appending or deleting
            // based on some diffs / operations, which requires more complexity
            StringBuilder contents = new StringBuilder();
            for (Task task : tasks) {
                String serialized = TaskSerializer.serialize(task);
                contents.append(serialized).append("\n");
            }

            bufferedWriter.write(contents.toString());
        }
    }

    /** TODO (also may fail to deserialize, thats a different exception) */
    public List<Task> loadTasks() throws IOException {
        // TODO
        return null;
    }
}
