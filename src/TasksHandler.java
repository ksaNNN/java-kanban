import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] parts = exchange.getRequestURI().getPath().split("/");

            switch (method) {
                case "GET":
                    handleGet(exchange, parts);
                    break;
                case "POST":
                    handlePost(exchange, parts);
                    break;
                case "DELETE":
                    handleDelete(exchange, parts);
                    break;
                default:
                    sendNotFound(exchange, "Некорректный метод запроса");
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Некорректный идентификатор задачи");
        } catch (ManagerValidateException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(exchange, "Произошла ошибка при обработке запроса: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) {
            sendText(exchange, gson.toJson(manager.getAllTasks()), 200);
            return;
        }
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            Task task = manager.getTaskById(id);
            sendText(exchange, gson.toJson(task), 200);
            return;
        }
        sendNotFound(exchange, "Некорректный путь запроса");
    }

    private void handlePost(HttpExchange exchange, String[] parts) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == 0) {
            manager.createTask(task);
        } else {
            manager.updateTask(task);
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            manager.deleteTask(id);
        } else {
            manager.deleteAllTasks();
        }
        sendText(exchange, "", 200);
    }
}