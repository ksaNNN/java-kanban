import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private static final Comparator<Task> BY_START_TIME =
            Comparator.comparing(Task::getStartTime).thenComparing(Task::getId);
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(BY_START_TIME);

    private int generateId() {
        return nextId++;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    public static boolean isOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean hasIntersections(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> isOverlapping(task, t));
    }

    private void validateNoIntersections(Task task) {
        if (hasIntersections(task)) {
            throw new ManagerValidateException(
                    "Задача \"" + task.getName() + "\" пересекается по времени с другой задачей");
        }
    }

    @Override
    public Task createTask(Task task) {
        validateNoIntersections(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        Task copy = new Task(task);
        historyManager.add(copy);
        return copy;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            removeFromPrioritized(oldTask);
            try {
                validateNoIntersections(task);
            } catch (ManagerValidateException e) {
                addToPrioritized(oldTask);
                throw e;
            }
            Task copy = new Task(task);
            tasks.put(task.getId(), copy);
            addToPrioritized(copy);
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritized(task);
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.values().forEach(this::removeFromPrioritized);
        tasks.clear();
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        updateEpicFields(epic);
        return epic;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        Epic copy = new Epic(epic);
        historyManager.add(copy);
        return copy;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            updateEpicStatus(existingEpic);
            updateEpicFields(existingEpic);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    removeFromPrioritized(subtask);
                }
                historyManager.remove(subtaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.values().forEach(this::removeFromPrioritized);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        validateNoIntersections(subtask);
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        addToPrioritized(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
            updateEpicFields(epic);
        }
        return subtask;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }
        Subtask copy = new Subtask(subtask);
        historyManager.add(copy);
        return copy;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            removeFromPrioritized(oldSubtask);
            try {
                validateNoIntersections(subtask);
            } catch (ManagerValidateException e) {
                addToPrioritized(oldSubtask);
                throw e;
            }
            Subtask copy = new Subtask(subtask);
            subtasks.put(subtask.getId(), copy);
            addToPrioritized(copy);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateEpicFields(epic);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
                updateEpicFields(epic);
            }
            subtasks.remove(id);
            removeFromPrioritized(subtask);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.values().forEach(this::removeFromPrioritized);
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
            updateEpicFields(epic);
        }
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> epicSubtasks = getSubtasksByEpic(epic.getId());

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = epicSubtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void updateEpicFields(Epic epic) {
        List<Subtask> epicSubtasks = getSubtasksByEpic(epic.getId());

        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime earliestStart = epicSubtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = epicSubtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Duration totalDuration = epicSubtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(earliestStart);
        epic.setEndTime(latestEnd);
        epic.setDuration(totalDuration);
    }
}