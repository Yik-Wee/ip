package grug.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import grug.storage.serde.TaskDeserializer;
import grug.storage.serde.TaskDeserializerException;
import grug.storage.serde.TaskSerializer;
import grug.task.Task;

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
            // replace the contents of the entire file every save, rather than modify based
            // on some diffs (which is unnecessarily complex)
            String contents = tasks.stream()
                    .map(TaskSerializer::serialize)
                    .collect(Collectors.joining(System.lineSeparator()));

            bufferedWriter.write(contents);
        }
    }

    /**
     * Loads the tasks to the file specified by the storage filepath
     * {@link #getFilepath()}.
     *
     * @throws IOException               If the named file exists but is a directory
     *                                   rather than a regular file, does not exist
     *                                   but cannot be created, or cannot be opened
     *                                   or written to for any other reason.
     * @throws TaskDeserializerException If there was an error deserializing the
     *                                   saved tasks.
     */
    public List<Task> loadTasks() throws IOException, TaskDeserializerException {
        String serializedTasks;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filepath))) {
            serializedTasks = bufferedReader.readAllAsString();
        }

        return TaskDeserializer.deserializeMany(serializedTasks);
    }
}
