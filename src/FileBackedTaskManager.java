import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("id,type,name,status,description,epic,duration,startTime\n");

            for (Task task : getAllTasks()) {
                sb.append(taskToString(task)).append("\n");
            }
            for (Epic epic : getAllEpics()) {
                sb.append(taskToString(epic)).append("\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                sb.append(taskToString(subtask)).append("\n");
            }

            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл: " + file.getName(), e);
        }
    }

    private String taskToString(Task task) {
        String epicId = "";
        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        String durationMinutes = task.getDuration() != null
                ? String.valueOf(task.getDuration().toMinutes())
                : "";
        String startTime = task.getStartTime() != null
                ? task.getStartTime().toString()
                : "";

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                epicId,
                durationMinutes,
                startTime
        );
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        Duration duration = null;
        LocalDateTime startTime = null;
        if (fields.length > 6 && !fields[6].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(fields[6]));
        }
        if (fields.length > 7 && !fields[7].isEmpty()) {
            startTime = LocalDateTime.parse(fields[7]);
        }

        Task task;
        switch (type) {
            case EPIC:
                task = new Epic(name, description);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                task = new Subtask(name, description, epicId, duration, startTime);
                break;
            case TASK:
            default:
                task = new Task(name, description, duration, startTime);
                break;
        }

        task.setId(id);
        task.setStatus(status);
        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return manager;
            }

            String[] lines = content.split("\n");
            int maxId = 0;

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }

                Task task = fromString(line);
                maxId = Math.max(maxId, task.getId());

                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        if (subtask.getStartTime() != null) {
                            manager.prioritizedTasks.add(subtask);
                        }
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                        break;
                }
            }

            manager.nextId = maxId + 1;

            for (Epic epic : manager.epics.values()) {
                manager.recalculateEpicAfterLoad(epic);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось прочитать данные из файла: " + file.getName(), e);
        }

        return manager;
    }

    private void recalculateEpicAfterLoad(Epic epic) {
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

        epic.setStartTime(epicSubtasks.stream()
                .map(Task::getStartTime).filter(Objects::nonNull)
                .min(LocalDateTime::compareTo).orElse(null));
        epic.setEndTime(epicSubtasks.stream()
                .map(Task::getEndTime).filter(Objects::nonNull)
                .max(LocalDateTime::compareTo).orElse(null));
        epic.setDuration(epicSubtasks.stream()
                .map(Task::getDuration).filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus));
    }

    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}