import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        taskManager = createManager();
    }

    @Test
    void createTask() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        assertTrue(task.getId() > 0);
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    void getTaskById() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    void deleteTaskAlsoRemovesItFromHistory() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        taskManager.getTaskById(task.getId());
        taskManager.deleteTask(task.getId());
        assertFalse(taskManager.getHistory().contains(task));
    }

    @Test
    void subtaskHasLinkedEpic() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        assertEquals(epic.getId(), subtask.getEpicId());
        assertTrue(taskManager.getEpicById(epic.getId()).getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void epicStatusAllSubtasksNew() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        taskManager.createSubtask(new Subtask("П1", "Описание", epic.getId()));
        taskManager.createSubtask(new Subtask("П2", "Описание", epic.getId()));

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusAllSubtasksDone() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask s1 = taskManager.createSubtask(new Subtask("П1", "Описание", epic.getId()));
        Subtask s2 = taskManager.createSubtask(new Subtask("П2", "Описание", epic.getId()));

        s1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(s1);
        s2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(s2);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusMixedNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask s1 = taskManager.createSubtask(new Subtask("П1", "Описание", epic.getId()));
        taskManager.createSubtask(new Subtask("П2", "Описание", epic.getId()));

        s1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(s1);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusSubtasksInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask s1 = taskManager.createSubtask(new Subtask("П1", "Описание", epic.getId()));

        s1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(s1);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void deletedSubtaskIdRemovedFromEpic() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        taskManager.deleteSubtask(subtask.getId());

        assertFalse(taskManager.getEpicById(epic.getId()).getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void prioritizedTasksSortedByStartTime() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 12, 0)));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0)));

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertEquals(task2, prioritized.get(0));
        assertEquals(task1, prioritized.get(1));
    }

    @Test
    void taskWithoutStartTimeNotInPrioritizedList() {
        Task task = taskManager.createTask(new Task("Задача без времени", "Описание"));
        assertFalse(taskManager.getPrioritizedTasks().contains(task));
    }

    @Test
    void overlappingTasksThrowException() {
        taskManager.createTask(new Task("Задача 1", "Описание",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0)));

        Task overlapping = new Task("Задача 2", "Описание",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 30));

        assertThrows(ManagerValidateException.class, () -> taskManager.createTask(overlapping));
    }

    @Test
    void nonOverlappingTasksDoNotThrow() {
        taskManager.createTask(new Task("Задача 1", "Описание",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0)));

        Task nonOverlapping = new Task("Задача 2", "Описание",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 11, 0));

        assertDoesNotThrow(() -> taskManager.createTask(nonOverlapping));
    }
}