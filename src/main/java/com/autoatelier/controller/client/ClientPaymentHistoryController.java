package com.autoatelier.controller.client;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.OrderCardLoader;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

public class ClientPaymentHistoryController extends BaseController {

    @FXML private VBox  historyListBox;
    @FXML private Label statusLabel;
    @FXML private Label totalLabel;

    @Override
    protected void onInit() {
        loadHistory();
    }

    private void loadHistory() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                List<Order> all = OrderService.getInstance().getMyOrders();
                List<Order> paid = all.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus()))
                        .toList();
                double total = paid.stream()
                        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                        .sum();
                Platform.runLater(() -> {
                    historyListBox.getChildren().clear();
                    if (paid.isEmpty()) {
                        statusLabel.setText("Нет оплаченных заявок");
                        totalLabel.setText("Итого: 0 ₽");
                    } else {
                        statusLabel.setText("Оплачено заявок: " + paid.size());
                        totalLabel.setText(String.format("Итого потрачено: %.0f ₽", total));
                        paid.forEach(o -> historyListBox.getChildren().add(
                            OrderCardLoader.create(o)
                        ));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void refresh()         { loadHistory(); }
    @FXML private void goToDashboard()   { SceneManager.navigate("client-dashboard"); }
    @FXML private void goToCatalog()     { SceneManager.navigate("client-catalog"); }
    @FXML private void goToOrders()      { SceneManager.navigate("client-orders"); }
    @FXML private void goToNewOrder()    { SceneManager.navigate("client-new-order"); }
    @FXML private void goToProfile()     { SceneManager.navigate("client-profile"); }
    @FXML private void goToPayHistory()  { }
}
