package com.autoatelier.controller.client;

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

public class ClientDashboardController extends BaseController {

    @FXML private Label greetingLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label completedOrdersLabel;
    @FXML private Label spentLabel;
    @FXML private VBox  recentOrdersBox;

    @Override
    protected void onInit() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        greetingLabel.setText("Здравствуйте, " + name + "!");
        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                List<Order> orders = OrderService.getInstance().getMyOrders();
                long total     = orders.size();
                long active    = orders.stream()
                        .filter(o -> Order.Status.IN_PROGRESS.key.equals(o.getStatus())).count();
                long completed = orders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus())).count();
                double spent   = orders.stream()
                        .filter(o -> Order.Status.COMPLETED.key.equals(o.getStatus()))
                        .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                        .sum();

                Platform.runLater(() -> {
                    totalOrdersLabel.setText(String.valueOf(total));
                    activeOrdersLabel.setText(String.valueOf(active));
                    completedOrdersLabel.setText(String.valueOf(completed));
                    spentLabel.setText(String.format("%.0f ₽", spent));
                    renderRecent(orders.stream().limit(3).toList());
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void renderRecent(List<Order> orders) {
        recentOrdersBox.getChildren().clear();
        if (orders.isEmpty()) {
            Label empty = new Label("Заявок пока нет — создайте первую в каталоге!");
            empty.getStyleClass().add("page-subtitle");
            recentOrdersBox.getChildren().add(empty);
            return;
        }
        for (Order order : orders) {
            recentOrdersBox.getChildren().add(
                OrderCardLoader.create(order, () -> SceneManager.navigate("client-orders"))
            );
        }
    }

    @FXML private void goToProfile()    { SceneManager.navigate("client-profile"); }
    @FXML private void goToDashboard()  {  }
    @FXML private void goToCatalog()    { SceneManager.navigate("client-catalog"); }
    @FXML private void goToOrders()     { SceneManager.navigate("client-orders"); }
    @FXML private void goToNewOrder()   { SceneManager.navigate("client-new-order"); }
    @FXML private void goToPayHistory() { SceneManager.navigate("client-pay-history"); }
}
