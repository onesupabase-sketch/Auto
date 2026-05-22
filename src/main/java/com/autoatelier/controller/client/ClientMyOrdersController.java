package com.autoatelier.controller.client;

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

public class ClientMyOrdersController extends BaseController {

    @FXML private VBox   ordersListBox;
    @FXML private VBox   detailPane;
    @FXML private Label  detailService;
    @FXML private Label  detailCar;
    @FXML private Label  detailStatus;
    @FXML private Label  detailPrice;
    @FXML private Label  detailComment;
    @FXML private Label  detailDescription;
    @FXML private Label  statusLabel;
    @FXML private Button cancelOrderBtn;
    @FXML private Button repeatOrderBtn;

    private Order selectedOrder;

    @Override
    protected void onInit() {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
        loadOrders();
    }

    private void loadOrders() {
        statusLabel.setText("Загрузка заявок...");
        new Thread(() -> {
            try {
                List<Order> orders = OrderService.getInstance().getMyOrders();
                Platform.runLater(() -> {
                    ordersListBox.getChildren().clear();
                    if (orders.isEmpty()) {
                        statusLabel.setText("У вас пока нет заявок — создайте первую!");
                    } else {
                        statusLabel.setText("Заявок: " + orders.size());
                        orders.forEach(o -> ordersListBox.getChildren().add(buildOrderCard(o)));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private VBox buildOrderCard(Order order) {
        return OrderCardLoader.create(order, () -> showDetail(order));
    }

    private void showDetail(Order order) {
        selectedOrder = order;
        detailPane.setVisible(true);
        detailPane.setManaged(true);

        detailService.setText(order.getService() != null ? order.getService().getName() : "—");
        detailCar.setText(order.getCarInfo());
        detailStatus.setText(order.getStatusDisplay());
        detailStatus.setStyle("-fx-text-fill: " + order.getStatusEnum().color + ";");
        detailPrice.setText(order.getPriceFormatted());
        detailDescription.setText(order.getDescription() != null ? order.getDescription() : "—");
        detailComment.setText(order.getManagerComment() != null
                ? order.getManagerComment() : "Комментарий не добавлен");

        boolean isNew = Order.Status.NEW.key.equals(order.getStatus());
        cancelOrderBtn.setVisible(isNew);
        cancelOrderBtn.setManaged(isNew);

        boolean isDone = Order.Status.COMPLETED.key.equals(order.getStatus());
        repeatOrderBtn.setVisible(isDone);
        repeatOrderBtn.setManaged(isDone);
    }

    @FXML
    private void handleCancelOrder() {
        if (selectedOrder == null) return;
        if (!AlertUtil.confirm("Отмена заявки",
                "Вы уверены, что хотите отменить заявку «"
                + (selectedOrder.getService() != null ? selectedOrder.getService().getName() : "#" + selectedOrder.getId())
                + "»?")) return;

        cancelOrderBtn.setDisable(true);
        new Thread(() -> {
            try {
                OrderService.getInstance().cancelOrder(selectedOrder.getId());
                Platform.runLater(() -> {
                    detailPane.setVisible(false);
                    detailPane.setManaged(false);
                    selectedOrder = null;
                    loadOrders();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    cancelOrderBtn.setDisable(false);
                    AlertUtil.error("Ошибка", e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleRepeatOrder() {
        if (selectedOrder == null) return;
        if (selectedOrder.getServiceId() != null)
            System.setProperty("selected.service.id", String.valueOf(selectedOrder.getServiceId()));
        if (selectedOrder.getCarModel() != null)
            System.setProperty("repeat.car.model", selectedOrder.getCarModel());
        if (selectedOrder.getCarYear() != null)
            System.setProperty("repeat.car.year", String.valueOf(selectedOrder.getCarYear()));
        SceneManager.navigate("client-new-order");
    }

    @FXML private void refresh()         { loadOrders(); }
    @FXML private void goToProfile()     { SceneManager.navigate("client-profile"); }
    @FXML private void goToDashboard()   { SceneManager.navigate("client-dashboard"); }
    @FXML private void goToCatalog()     { SceneManager.navigate("client-catalog"); }
    @FXML private void goToNewOrder()    { SceneManager.navigate("client-new-order"); }
    @FXML private void goToOrders()      {  }
    @FXML private void goToPayHistory()  { SceneManager.navigate("client-pay-history"); }
}
