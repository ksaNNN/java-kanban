import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File file;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(file);
    }

    @Test
    void saveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void saveSeveralTasks() throws IOException {
        taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика"));
        taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи", epic.getId()));

        String content = Files.readString(file.toPath());

        assertTrue(content.contains("Задача 1"));
        assertTrue(content.contains("Эпик 1"));
        assertTrue(content.contains("Подзадача 1"));
    }

    @Test
    void loadSeveralTasks() {
        Task task = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());
        assertEquals(task.getName(), loaded.getAllTasks().get(0).getName());
        assertEquals(subtask.getEpicId(), loaded.getAllSubtasks().get(0).getEpicId());
    }

    @Test
    void saveAndLoadPreservesDurationAndStartTime() {
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 1, 9, 0);
        Task task = taskManager.createTask(new Task("Задача", "Описание", Duration.ofMinutes(45), startTime));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loaded.getTaskById(task.getId());

        assertEquals(Duration.ofMinutes(45), loadedTask.getDuration());
        assertEquals(startTime, loadedTask.getStartTime());
    }

    @Test
    void loadingFromNonExistentFileThrowsManagerSaveException() {
        File missingFile = new File("no_such_file_" + System.nanoTime() + ".csv");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(missingFile));
    }

    @Test
    void savingToValidFileDoesNotThrow() {
        assertDoesNotThrow(() -> taskManager.createTask(new Task("Задача", "Описание")));
    }
}