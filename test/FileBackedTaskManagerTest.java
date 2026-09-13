import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void saveAndLoadEmptyFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.getAllTasks(); // пустой менеджер, ничего не сохраняем явно — save() не вызывался

        // Сохраняем пустое состояние явно, создав и удалив задачу, либо просто проверяем загрузку
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loaded.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void saveSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        manager.createTask(new Task("Задача 1", "Описание 1"));
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика"));
        manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи", epic.getId()));

        String content = java.nio.file.Files.readString(file.toPath());

        assertTrue(content.contains("Задача 1"), "Файл должен содержать сохранённую задачу");
        assertTrue(content.contains("Эпик 1"), "Файл должен содержать сохранённый эпик");
        assertTrue(content.contains("Подзадача 1"), "Файл должен содержать сохранённую подзадачу");
    }

    @Test
    void loadSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Задача 1", "Описание 1"));
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size(), "Должна загрузиться 1 задача");
        assertEquals(1, loaded.getAllEpics().size(), "Должен загрузиться 1 эпик");
        assertEquals(1, loaded.getAllSubtasks().size(), "Должна загрузиться 1 подзадача");

        assertEquals(task.getName(), loaded.getAllTasks().get(0).getName(), "Имя задачи не совпадает");
        assertEquals(subtask.getEpicId(), loaded.getAllSubtasks().get(0).getEpicId(), "epicId не совпадает");
    }

    @Test
    void loadedManagerPreservesTaskIds() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Задача 1", "Описание 1"));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loaded.getTaskById(task.getId());

        assertNotNull(loadedTask, "Задача с исходным id должна найтись после загрузки");
        assertEquals(task.getId(), loadedTask.getId(), "id должен сохраниться таким же после загрузки");
    }

    @Test
    void newTaskAfterLoadGetsUniqueId() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task1 = manager.createTask(new Task("Задача 1", "Описание"));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task task2 = loaded.createTask(new Task("Задача 2", "Описание"));

        assertNotEquals(task1.getId(), task2.getId(),
                "Новая задача после загрузки не должна получить id, который уже занят");
    }

    @Test
    void epicSubtaskLinkIsRestoredAfterLoad() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpicById(epic.getId());

        assertTrue(loadedEpic.getSubtaskIds().contains(subtask.getId()),
                "Связь эпика с подзадачей должна восстановиться после загрузки");
    }
}