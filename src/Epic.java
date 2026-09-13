import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();
    }

    // Конструктор копирования
    public Epic(Epic other) {
        super(other);
        this.subtaskIds = new ArrayList<>(other.subtaskIds); // копируем сПиСоК, а не ссылку на него
    }

    public ArrayList<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds); // отдаём копию вместо оригинала
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(Integer subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", subtasksCount=" + subtaskIds.size() +
                '}';
    }
}