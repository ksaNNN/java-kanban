public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("=== СОЗДАНИЕ ЗАДАЧ ===");

        Task task1 = manager.createTask(new Task("Переезд", "Переехать в новую квартиру"));
        Task task2 = manager.createTask(new Task("Сходить в магазин", "Купить продукты"));

        Epic epic1 = manager.createEpic(new Epic("Важный эпик 1", "Описание важного эпика"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание 1", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание 2", epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Важный эпик 2", "Описание второго эпика"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание 3", epic2.getId()));

        // Просматриваем задачи для создания истории
        System.out.println("\n=== ПРОСМОТР ЗАДАЧ ===");
        manager.getSubtaskById(subtask1.getId());
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());  // Повторно - дубли допускаются

        // Печатаем всё
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\n=== ВСЕ ЗАДАЧИ ===");
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
            for (Subtask subtask : manager.getSubtasksByEpic(epic.getId())) {
                System.out.println("--> " + subtask);
            }
        }

        System.out.println("\nПодзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("\n=== ИСТОРИЯ ПРОСМОТРОВ ===");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}