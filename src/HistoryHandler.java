import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendNotFound(exchange, "Некорректный метод запроса");
                return;
            }
            sendText(exchange, gson.toJson(manager.getHistory()), STATUS_OK);
        } catch (Exception e) {
            sendServerError(exchange, "Произошла ошибка при обработке запроса: " + e.getMessage());
        }
    }
}