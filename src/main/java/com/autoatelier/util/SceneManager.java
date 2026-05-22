package com.autoatelier.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static Stage primaryStage;
    private static final Map<String, String> ROUTES = new HashMap<>();

    static {
        ROUTES.put("login",             "/com/autoatelier/views/Login.fxml");
        ROUTES.put("register",          "/com/autoatelier/views/Register.fxml");

        ROUTES.put("comp-order-card",      "/com/autoatelier/views/components/OrderCard.fxml");
        ROUTES.put("comp-service-card",     "/com/autoatelier/views/components/ServiceCard.fxml");
        ROUTES.put("comp-stat-card",        "/com/autoatelier/views/components/StatCard.fxml");
        ROUTES.put("comp-topbar",           "/com/autoatelier/views/components/TopBar.fxml");
        ROUTES.put("comp-payment-card",    "/com/autoatelier/views/components/PaymentCardView.fxml");

        ROUTES.put("client-dashboard", "/com/autoatelier/views/client/Catalog.fxml");
        ROUTES.put("client-catalog",   "/com/autoatelier/views/client/Catalog.fxml");
        ROUTES.put("client-orders",    "/com/autoatelier/views/client/MyOrders.fxml");
        ROUTES.put("client-new-order", "/com/autoatelier/views/client/NewOrder.fxml");
        ROUTES.put("client-profile",   "/com/autoatelier/views/client/Profile.fxml");

        ROUTES.put("manager-dashboard","/com/autoatelier/views/manager/Orders.fxml");
        ROUTES.put("manager-orders",   "/com/autoatelier/views/manager/Orders.fxml");
        ROUTES.put("manager-catalog",  "/com/autoatelier/views/manager/Catalog.fxml");
        ROUTES.put("manager-profile",  "/com/autoatelier/views/manager/Profile.fxml");

        ROUTES.put("admin-dashboard",  "/com/autoatelier/views/admin/Users.fxml");
        ROUTES.put("admin-users",      "/com/autoatelier/views/admin/Users.fxml");
        ROUTES.put("admin-services",   "/com/autoatelier/views/admin/Services.fxml");
        ROUTES.put("admin-stats",      "/com/autoatelier/views/admin/Statistics.fxml");
        ROUTES.put("admin-profile",    "/com/autoatelier/views/admin/Profile.fxml");
        ROUTES.put("admin-archive",    "/com/autoatelier/views/admin/Archive.fxml");
        ROUTES.put("client-pay-history", "/com/autoatelier/views/client/PaymentHistory.fxml");
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void navigate(String route) {
        try {
            String fxml = ROUTES.get(route);
            if (fxml == null) throw new IllegalArgumentException("Unknown route: " + route);

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxml));
            Parent root = loader.load();

            Scene currentScene = primaryStage.getScene();
            Scene scene;
            if (currentScene != null) {
                scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            } else {
                scene = new Scene(root);
            }

            scene.getStylesheets().add(
                SceneManager.class.getResource("/com/autoatelier/css/style.css").toExternalForm()
            );

            root.prefWidth(scene.getWidth());
            root.prefHeight(scene.getHeight());

            primaryStage.setScene(scene);
        } catch (Exception e) {
        }
    }

    public static <T> T navigateWithController(String route) {
        try {
            String fxml = ROUTES.get(route);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneManager.class.getResource("/com/autoatelier/css/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            return loader.getController();
        } catch (Exception e) {
            return null;
        }
    }

    public static Stage getPrimaryStage() { return primaryStage; }
}
