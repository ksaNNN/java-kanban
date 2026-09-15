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
    void getHistoryOnEmptyHistoryReturnsEmptyListNotNull() {
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "getHistory не должен возвращать null");
        assertTrue(history.isEmpty(), "История пустого менеджера должна быть пустой");
    }

    @Test
    void historyHasNoSizeLimit() {
        for (int i = 1; i <= 50; i++) {
            Task task = new Task("Задача " + i, "Описание");
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(50, history.size(), "История не должна ограничиваться по размеру");
    }

    @Test
    void repeatedViewDoesNotCreateDuplicate() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Повторный просмотр не должен создавать дубли");
    }

    @Test
    void repeatedViewMovesTaskToTheEnd() {
        Task task1 = new Task("Задача 1", "Описание");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task2, task3, task1), history,
                "Повторно просмотренная задача должна переместиться в конец истории");
    }

    @Test
    void removeFromBeginningKeepsRestInOrder() {
        Task task1 = new Task("Задача 1", "Описание");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        assertEquals(List.of(task2, task3), historyManager.getHistory(),
                "После удаления головы список должен сохранить порядок остальных");
    }

    @Test
    void removeFromMiddleKeepsRestInOrder() {
        Task task1 = new Task("Задача 1", "Описание");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        assertEquals(List.of(task1, task3), historyManager.getHistory(),
                "После удаления середины соседи должны перецепиться друг к другу");
    }

    @Test
    void removeFromEndKeepsRestInOrder() {
        Task task1 = new Task("Задача 1", "Описание");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        assertEquals(List.of(task1, task2), historyManager.getHistory(),
                "После удаления хвоста список должен корректно обновить tail");
    }

    @Test
    void removeOnlyElementResultsInEmptyHistory() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);

        historyManager.add(task);
        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty(),
                "После удаления единственного элемента история должна опустеть");
    }

    @Test
    void removeNonExistentIdDoesNotThrow() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);
        historyManager.add(task);

        assertDoesNotThrow(() -> historyManager.remove(999),
                "Удаление несуществующего id не должно бросать исключение");
        assertEquals(1, historyManager.getHistory().size(), "Существующая запись не должна пострадать");
    }
}