import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager, "Менеджер не должен быть null");
        assertInstanceOf(InMemoryTaskManager.class, manager, "Должен вернуть InMemoryTaskManager");
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не должен быть null");
        assertInstanceOf(InMemoryHistoryManager.class, historyManager, "Должен вернуть InMemoryHistoryManager");
    }

    @Test
    void managersAlwaysReturnWorkingInstances() {
        TaskManager manager = Managers.getDefault();

        // Проверяем что менеджер работает
        Task task = manager.createTask(new Task("Тест", "Описание"));
        assertNotNull(task, "Менеджер должен создавать задачи");
        assertTrue(task.getId() > 0, "ID должен быть сгенерирован");
    }
}