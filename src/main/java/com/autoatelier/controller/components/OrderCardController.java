package com.autoatelier.controller.components;

import com.autoatelier.model.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class OrderCardController {

    @FXML private VBox   orderCard;
    @FXML private Rectangle statusBar;
    @FXML private Label  cardServiceName;
    @FXML private Label  cardStatus;
    @FXML private Label  cardCarInfo;
    @FXML private Label  cardPrice;
    @FXML private Label  cardDate;
    @FXML private HBox   commentRow;
    @FXML private Label  cardComment;

    public void setOrder(Order order) {
        Order.Status st = order.getStatusEnum();

        statusBar.setStyle("-fx-fill: " + st.color + ";");

        String svcName = order.getService() != null
                ? order.getService().getName()
                : "Услуга #" + order.getServiceId();
        cardServiceName.setText(svcName);

        cardStatus.setText(st.display);
        cardStatus.getStyleClass().removeIf(c -> c.startsWith("badge-"));
        cardStatus.getStyleClass().add("badge-" + order.getStatus());

        cardCarInfo.setText(order.getCarInfo());

        cardPrice.setText(order.getPriceFormatted());

        String dateStr = order.getCreatedAt() != null
                ? order.getCreatedAt().substring(0, 10) : "";
        cardDate.setText(dateStr);

        String comment = order.getManagerComment();
        if (comment != null && !comment.isBlank()) {
            cardComment.setText(comment);
            commentRow.setVisible(true);
            commentRow.setManaged(true);
        } else {
            commentRow.setVisible(false);
            commentRow.setManaged(false);
        }
    }

    public void setOnClick(Runnable action) {
        orderCard.setOnMouseClicked(e -> action.run());
        orderCard.setCursor(javafx.scene.Cursor.HAND);
    }
}
