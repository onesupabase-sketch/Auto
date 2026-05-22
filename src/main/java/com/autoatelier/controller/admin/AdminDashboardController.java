package com.autoatelier.controller.admin;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.service.StorageService;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;
import java.util.Map;
public class AdminDashboardController extends BaseController {
    @FXML private Label totalOrdersLabel;
    @FXML private Label newOrdersLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label completedLabel;
    @FXML private Label cancelledLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalRevenueLabel;
    @Override
    protected void onInit() {
        loadStats();
    }
    private void loadStats() {
        new Thread(() -> {
            try {
                Map<String, Long> stats = OrderService.getInstance().getStats();
                List<Order> allOrders = OrderService.getInstance().getAllOrders();
                List<com.autoatelier.model.User> users = StorageService.getInstance().getAllUsers();
                long total = stats.values().stream().mapToLong(Long::longValue).sum();
                double revenue = allOrders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus()))
                        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                        .sum();
                Platform.runLater(() -> {
                    totalOrdersLabel.setText(String.valueOf(total));
                    newOrdersLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.NEW.key, 0L)));
                    inProgressLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.IN_PROGRESS.key, 0L)));
                    completedLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.COMPLETED.key, 0L)));
                    cancelledLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.CANCELLED.key, 0L)));
                    totalUsersLabel.setText(String.valueOf(users.size()));
                    totalRevenueLabel.setText(String.format("%.0f ₽", revenue));
                });
            } catch (Exception e) {
            }
        }).start();
    }

    @FXML private void goToDashboard()  {  }
    @FXML private void goToUsers()      { SceneManager.navigate("admin-users"); }
    @FXML private void goToServices()   { SceneManager.navigate("admin-services"); }
    @FXML private void goToStats()      { SceneManager.navigate("admin-stats"); }
    @FXML private void goToArchive()    { SceneManager.navigate("admin-archive"); }
    @FXML private void goToProfile()    { SceneManager.navigate("admin-profile"); }
}
