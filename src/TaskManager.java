import java.util.ArrayList;

public interface TaskManager {

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);


    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);


    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getAllSubtasks();


    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);


    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);


    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    ArrayList<Subtask> getSubtasksByEpic(int epicId);

    ArrayList<Task> getHistory();
}