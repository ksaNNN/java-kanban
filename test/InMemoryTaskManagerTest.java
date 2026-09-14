import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void changingTaskViaSetterAfterGettingItDoesNotAffectStoredTask() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));

        Task retrievedTask = taskManager.getTaskById(task.getId());
        retrievedTask.setStatus(TaskStatus.DONE);

        Task storedTaskAgain = taskManager.getTaskById(task.getId());
        assertEquals(TaskStatus.NEW, storedTaskAgain.getStatus());
    }

    @Test
    void modifyingEpicSubtaskIdsListDirectlyDoesNotAffectManager() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        epic.getSubtaskIds().clear();

        Epic storedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(storedEpic.getSubtaskIds().contains(subtask.getId()));
    }
}