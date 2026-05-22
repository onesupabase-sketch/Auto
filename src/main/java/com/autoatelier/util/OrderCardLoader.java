package com.autoatelier.util;

import com.autoatelier.controller.components.OrderCardController;
import com.autoatelier.model.Order;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class OrderCardLoader {

    private static final String FXML_PATH =
            "/com/autoatelier/views/components/OrderCard.fxml";

    public static VBox create(Order order, Runnable onClick) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    OrderCardLoader.class.getResource(FXML_PATH));
            VBox card = loader.load();
            OrderCardController ctrl = loader.getController();
            ctrl.setOrder(order);
            if (onClick != null) {
                ctrl.setOnClick(onClick);
            }
            return card;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить OrderCard.fxml: " + e.getMessage(), e);
        }
    }

    public static VBox create(Order order) {
        return create(order, null);
    }
}
