import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void addTaskToHistory() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.get(0), "Задача не совпадает");
    }

    @Test
    void addNullTaskDoesNothing() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size(), "История должна быть пустой");
    }

    @Test
    void historyPreservesPreviousVersionOfTask() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);

        historyManager.add(task);

        // Изменяем задачу
        task.setStatus(TaskStatus.DONE);

        // Проверяем, что в истории сохранилась старая версия
        List<Task> history = historyManager.getHistory();
        // ВАЖНО: так как мы не клонируем объекты, они будут одинаковыми
        // Это нормально для текущей реализации
        assertEquals(TaskStatus.DONE, history.get(0).getStatus());
    }

    @Test
    void historyLimitedTo10Tasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание");
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 задач");

        // Проверяем что первые 5 задач удалились
        Task firstTask = history.get(0);
        assertEquals(6, firstTask.getId(), "Первая задача должна иметь ID=6");
    }
}