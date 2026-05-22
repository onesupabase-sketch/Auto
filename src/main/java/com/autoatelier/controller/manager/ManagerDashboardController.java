package com.autoatelier.controller.manager;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.Order;
import com.autoatelier.service.OrderService;
import com.autoatelier.util.OrderCardLoader;
import com.autoatelier.util.SceneManager;
import com.autoatelier.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class ManagerDashboardController extends BaseController {

    @FXML private Label totalLabel;
    @FXML private Label newLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label completedLabel;

    @FXML private Label myTotalLabel;
    @FXML private Label myInProgressLabel;
    @FXML private Label myCompletedLabel;
    @FXML private Label myRevenueLabel;

    @FXML private VBox urgentOrdersBox;

    @Override
    protected void onInit() {
        loadStats();
    }

    private void loadStats() {
        String myId = SessionManager.getInstance().getCurrentUser().getId();

        new Thread(() -> {
            try {
                Map<String, Long> stats   = OrderService.getInstance().getStats();
                List<Order> newOrders     = OrderService.getInstance().getOrdersByStatus(Order.Status.NEW.key);
                List<Order> allOrders     = OrderService.getInstance().getAllOrders();
                long total = stats.values().stream().mapToLong(Long::longValue).sum();

                List<Order> myOrders = allOrders.stream()
                        .filter(o -> myId.equals(o.getManagerId())).toList();
                long myTotal       = myOrders.size();
                long myInProgress  = myOrders.stream()
                        .filter(o -> Order.Status.IN_PROGRESS.key.equals(o.getStatus())).count();
                long myCompleted   = myOrders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus())).count();
                double myRevenue   = myOrders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus()))
                        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                        .sum();

                Platform.runLater(() -> {
                    totalLabel.setText(String.valueOf(total));
                    newLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.NEW.key, 0L)));
                    inProgressLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.IN_PROGRESS.key, 0L)));
                    completedLabel.setText(String.valueOf(stats.getOrDefault(Order.Status.COMPLETED.key, 0L)));

                    myTotalLabel.setText(String.valueOf(myTotal));
                    myInProgressLabel.setText(String.valueOf(myInProgress));
                    myCompletedLabel.setText(String.valueOf(myCompleted));
                    myRevenueLabel.setText(String.format("%.0f ₽", myRevenue));

                    renderUrgent(newOrders.stream().limit(5).toList());
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void renderUrgent(List<Order> orders) {
        urgentOrdersBox.getChildren().clear();
        if (orders.isEmpty()) {
            Label lbl = new Label("Новых заявок нет — всё обработано ✅");
            lbl.getStyleClass().add("page-subtitle");
            urgentOrdersBox.getChildren().add(lbl);
            return;
        }
        for (Order o : orders) {
            urgentOrdersBox.getChildren().add(
                OrderCardLoader.create(o, () -> SceneManager.navigate("manager-orders"))
            );
        }
    }

    @FXML private void goToDashboard() {  }
    @FXML private void goToOrders()    { SceneManager.navigate("manager-orders"); }
    @FXML private void goToCatalog()   { SceneManager.navigate("manager-catalog"); }
    @FXML private void goToProfile()   { SceneManager.navigate("manager-profile"); }
}
