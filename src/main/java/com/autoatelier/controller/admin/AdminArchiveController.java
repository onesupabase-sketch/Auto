package com.autoatelier.controller.admin;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.OrderCardLoader;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class AdminArchiveController extends BaseController {

    @FXML private VBox            archiveListBox;
    @FXML private Label           statusLabel;
    @FXML private ComboBox<String> typeFilter;

    @Override
    protected void onInit() {
        typeFilter.getItems().addAll("Все (завершённые + отменённые)", "Только завершённые", "Только отменённые");
        typeFilter.setValue("Все (завершённые + отменённые)");
        typeFilter.setOnAction(e -> loadArchive());
        loadArchive();
    }

    private void loadArchive() {
        statusLabel.setText("Загрузка архива...");
        archiveListBox.getChildren().clear();

        String filter = typeFilter.getValue();
        new Thread(() -> {
            try {
                List<Order> orders;
                if ("Только завершённые".equals(filter)) {
                    orders = OrderService.getInstance().getOrdersByStatus(Order.Status.COMPLETED.key);
                } else if ("Только отменённые".equals(filter)) {
                    orders = OrderService.getInstance().getOrdersByStatus(Order.Status.CANCELLED.key);
                } else {
                    List<Order> completed = OrderService.getInstance().getOrdersByStatus(Order.Status.COMPLETED.key);
                    List<Order> cancelled = OrderService.getInstance().getOrdersByStatus(Order.Status.CANCELLED.key);
                    orders = new java.util.ArrayList<>();
                    orders.addAll(completed);
                    orders.addAll(cancelled);
                    orders.sort((a, b) -> {
                        String da = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                        String db = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                        return db.compareTo(da);
                    });
                }
                final List<Order> finalOrders = orders;
                double revenue = orders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus()))
                        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                        .sum();
                final double finalRevenue = revenue;
                Platform.runLater(() -> {
                    if (finalOrders.isEmpty()) {
                        statusLabel.setText("Архив пуст");
                    } else {
                        statusLabel.setText(String.format("Записей: %d  |  Выручка: %.0f ₽",
                                finalOrders.size(), finalRevenue));
                        finalOrders.forEach(o -> archiveListBox.getChildren().add(
                            OrderCardLoader.create(o)
                        ));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private void handleDeleteOrder(Order order) {
        String svcName = order.getService() != null ? order.getService().getName() : "#" + order.getId();
        if (!AlertUtil.confirm("Удалить заявку",
                "Удалить заявку «" + svcName + "» из архива? Это действие необратимо.")) return;
        new Thread(() -> {
            try {
                OrderService.getInstance().deleteOrder(order.getId());
                Platform.runLater(this::loadArchive);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML private void refresh()       { loadArchive(); }
    @FXML private void goToDashboard() { SceneManager.navigate("admin-dashboard"); }
    @FXML private void goToUsers()     { SceneManager.navigate("admin-users"); }
    @FXML private void goToServices()  { SceneManager.navigate("admin-services"); }
    @FXML private void goToStats()     { SceneManager.navigate("admin-stats"); }
    @FXML private void goToProfile()   { SceneManager.navigate("admin-profile"); }
    @FXML private void goToArchive()   { }
}
