import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdAreEqual() {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void tasksWithDifferentIdAreNotEqual() {
        Task task1 = new Task("Задача 1", "Описание 1");
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разным ID не должны быть равны");
    }

    @Test
    void subtasksWithSameIdAreEqual() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", 2);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    void epicsWithSameIdAreEqual() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        epic1.setId(1);

        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
    }

    @Test
    void taskFieldsUnchangedAfterCreation() {
        Task task = new Task("Задача", "Описание задачи");
        task.setId(1);
        task.setStatus(TaskStatus.NEW);

        String originalName = task.getName();
        String originalDescription = task.getDescription();
        int originalId = task.getId();
        TaskStatus originalStatus = task.getStatus();

        // Проверяем, что поля не изменились
        assertEquals(originalName, task.getName());
        assertEquals(originalDescription, task.getDescription());
        assertEquals(originalId, task.getId());
        assertEquals(originalStatus, task.getStatus());
    }
}