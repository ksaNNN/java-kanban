import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void createTask() {
        Task task = new Task("Тест задача", "Описание тест задачи");
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask, "Задача не создана");
        assertTrue(createdTask.getId() > 0, "ID не сгенерирован");
        assertEquals(task.getName(), createdTask.getName(), "Имя не совпадает");
        assertEquals(TaskStatus.NEW, createdTask.getStatus(), "Статус должен быть NEW");
    }

    @Test
    void getTaskById() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        Task foundTask = taskManager.getTaskById(task.getId());

        assertNotNull(foundTask, "Задача не найдена");
        assertEquals(task, foundTask, "Задачи не совпадают");
    }

    @Test
    void getAllTasks() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание 2"));

        List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Список задач не возвращается");
        assertEquals(2, tasks.size(), "Неверное количество задач");
        assertTrue(tasks.contains(task1), "Задача 1 отсутствует");
        assertTrue(tasks.contains(task2), "Задача 2 отсутствует");
    }

    @Test
    void updateTask() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals(TaskStatus.DONE, updatedTask.getStatus(), "Статус не обновился");
    }

    @Test
    void deleteTask() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        taskManager.deleteTask(task.getId());

        Task deletedTask = taskManager.getTaskById(task.getId());
        assertNull(deletedTask, "Задача не удалена");
    }

    @Test
    void deleteTaskAlsoRemovesItFromHistory() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        taskManager.getTaskById(task.getId());

        taskManager.deleteTask(task.getId());

        assertFalse(taskManager.getHistory().contains(task),
                "Удалённая задача не должна оставаться в истории просмотров");
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic, "Эпик не создан");
        assertTrue(createdEpic.getId() > 0, "ID не сгенерирован");
        assertEquals(TaskStatus.NEW, createdEpic.getStatus(), "Статус должен быть NEW");
    }

    @Test
    void createSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask, "Подзадача не создана");
        assertEquals(epic.getId(), createdSubtask.getEpicId(), "ID эпика не совпадает");

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(createdSubtask.getId()), "Подзадача не добавлена в эпик");
    }

    @Test
    void epicStatusWithAllSubtasksNew() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        taskManager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId()));
        taskManager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId()));

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus(), "Статус должен быть NEW");
    }

    @Test
    void epicStatusWithAllSubtasksDone() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus(), "Статус должен быть DONE");
    }

    @Test
    void epicStatusWithSubtasksInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание", epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание", epic.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Статус должен быть IN_PROGRESS");
    }

    @Test
    void deleteEpicAlsoDeletesSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()), "Эпик не удалён");
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не удалена");
    }

    @Test
    void deleteEpicAlsoRemovesItsSubtasksFromHistory() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());

        taskManager.deleteEpic(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(epic), "Эпик не должен остаться в истории после удаления");
        assertFalse(history.contains(subtask), "Подзадача эпика не должна остаться в истории после удаления");
    }

    @Test
    void deletedSubtaskIdIsRemovedFromEpicSubtaskIds() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        taskManager.deleteSubtask(subtask.getId());

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertFalse(updatedEpic.getSubtaskIds().contains(subtask.getId()),
                "Внутри эпика не должно оставаться id удалённой подзадачи");
    }

    @Test
    void subtaskIsNotAccessibleAfterEpicDeletion() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        taskManager.deleteEpic(epic.getId());

        assertTrue(taskManager.getAllSubtasks().isEmpty(),
                "После удаления эпика её подзадачи не должны храниться в общем списке подзадач");
    }

    @Test
    void historyContainsViewedTasks() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        assertEquals(task, history.get(0), "Первая задача не совпадает");
        assertEquals(epic, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void historyHasNoLimitViaManager() {
        for (int i = 0; i < 15; i++) {
            Task task = taskManager.createTask(new Task("Задача " + i, "Описание"));
            taskManager.getTaskById(task.getId());
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(15, history.size(), "История через менеджер не должна ограничиваться 10 элементами");
    }

    @Test
    void repeatedViewViaManagerDoesNotCreateDuplicate() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size(),
                "Повторный просмотр через менеджер не должен создавать дубли в истории");
    }

    @Test
    void changingTaskViaSetterAfterGettingItDoesNotAffectStoredTask() {
        Task task = taskManager.createTask(new Task("Задача", "Описание"));

        Task retrievedTask = taskManager.getTaskById(task.getId());
        retrievedTask.setStatus(TaskStatus.DONE);

        Task storedTaskAgain = taskManager.getTaskById(task.getId());
        assertEquals(TaskStatus.NEW, storedTaskAgain.getStatus(),
                "Изменение копии не должно влиять на задачу, хранимую внутри менеджера");
    }

    @Test
    void modifyingEpicSubtaskIdsListDirectlyDoesNotAffectManager() {
        Epic epic = taskManager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        epic.getSubtaskIds().clear();

        Epic storedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(storedEpic.getSubtaskIds().contains(subtask.getId()),
                "getSubtaskIds() должен возвращать копию — очистка снаружи не должна ломать эпик внутри менеджера");
    }
}