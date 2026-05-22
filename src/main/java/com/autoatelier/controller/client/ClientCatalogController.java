package com.autoatelier.controller.client;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.TuningService;
import com.autoatelier.service.CatalogService;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.prefs.Preferences;

public class ClientCatalogController extends BaseController {

    @FXML
    private FlowPane catalogPane;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;

    private List<TuningService> allServices;
    private final Set<Long> favorites = new HashSet<>();
    private static final String FAV_KEY = "favorites";
    private final Preferences prefs = Preferences.userNodeForPackage(ClientCatalogController.class);

    private static final double CARD_WIDTH = 240;
    private static final double CARD_HEIGHT = 280;

    @Override
    protected void onInit() {
        loadFavoritesFromPrefs();
        catalogPane.setPrefWrapLength(1200);

        categoryFilter.getItems().add("Все категории");
        categoryFilter.getItems().add("⭐ Избранное");
        categoryFilter.setValue("Все категории");
        categoryFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        loadServices();
    }

    private void loadFavoritesFromPrefs() {
        String raw = prefs.get(FAV_KEY, "");
        if (!raw.isBlank())
            Arrays.stream(raw.split(",")).forEach(s -> {
                try {
                    favorites.add(Long.parseLong(s.trim()));
                } catch (Exception ignored) {
                }
            });
    }

    private void saveFavoritesToPrefs() {
        String val = favorites.stream().map(String::valueOf).reduce("", (a, b) -> a.isBlank() ? b : a + "," + b);
        prefs.put(FAV_KEY, val);
    }

    private void toggleFavorite(long id) {
        if (favorites.contains(id)) favorites.remove(id);
        else favorites.add(id);
        saveFavoritesToPrefs();
        applyFilter();
    }

    private void loadServices() {
        statusLabel.setText("Загрузка каталога...");
        new Thread(() -> {
            try {
                allServices = CatalogService.getInstance().getActiveServices();
                List<String> cats = allServices.stream()
                        .map(TuningService::getCategory)
                        .filter(c -> c != null && !c.isBlank())
                        .distinct().sorted().toList();
                Platform.runLater(() -> {

                    categoryFilter.getItems().addAll(cats);
                    renderServices(allServices);
                    statusLabel.setText("Услуг: " + allServices.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private void applyFilter() {
        if (allServices == null) return;
        String cat = categoryFilter.getValue();
        String q = searchField.getText().toLowerCase().trim();

        List<TuningService> filtered = allServices.stream()
                .filter(s -> {
                    if ("⭐ Избранное".equals(cat)) return favorites.contains(s.getId());
                    return "Все категории".equals(cat) || cat.equals(s.getCategory());
                })
                .filter(s -> q.isBlank()
                        || s.getName().toLowerCase().contains(q)
                        || (s.getDescription() != null && s.getDescription().toLowerCase().contains(q)))
                .toList();
        renderServices(filtered);
    }

    private void renderServices(List<TuningService> list) {
        catalogPane.getChildren().clear();
        for (TuningService svc : list)
            catalogPane.getChildren().add(buildCard(svc));
    }

    private VBox buildCard(TuningService svc) {
        VBox card = new VBox(0);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setPrefHeight(CARD_HEIGHT);
        card.getStyleClass().add("svc-card");

        StackPane header = new StackPane();
        header.getStyleClass().add("svc-card-header");
        header.setPrefHeight(90);
        header.setMinHeight(90);
        header.setMaxHeight(90);

        Label catBadge = new Label(svc.getCategory() != null ? svc.getCategory() : "Услуга");
        catBadge.getStyleClass().add("svc-card-cat-badge");
        StackPane.setAlignment(catBadge, javafx.geometry.Pos.TOP_LEFT);
        catBadge.setTranslateX(10);
        catBadge.setTranslateY(10);

        boolean isFav = favorites.contains(svc.getId());
        Button favBtn = new Button(isFav ? "⭐" : "☆");
        favBtn.getStyleClass().add(isFav ? "fav-btn-active" : "fav-btn");
        favBtn.setOnAction(e -> {
            toggleFavorite(svc.getId());
        });
        StackPane.setAlignment(favBtn, javafx.geometry.Pos.TOP_RIGHT);
        favBtn.setTranslateX(-8);
        favBtn.setTranslateY(8);

        Label icon = new Label(getCategoryIcon(svc.getCategory()));
        icon.getStyleClass().add("svc-card-icon");
        header.getChildren().addAll(icon, catBadge, favBtn);

        VBox body = new VBox(6);
        body.getStyleClass().add("svc-card-body");
        VBox.setVgrow(body, Priority.ALWAYS);

        Label name = new Label(svc.getName());
        name.getStyleClass().add("svc-card-name");
        name.setWrapText(true);
        name.setMaxHeight(42);

        Label desc = new Label(svc.getDescription() != null ? svc.getDescription() : "");
        desc.getStyleClass().add("svc-card-desc");
        desc.setWrapText(true);
        desc.setPrefHeight(44);
        desc.setMaxHeight(44);
        VBox.setVgrow(desc, Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(0);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("svc-card-footer");

        Label price = new Label(svc.getPriceFormatted());
        price.getStyleClass().add("svc-card-price");
        HBox.setHgrow(price, Priority.ALWAYS);

        Button btn = new Button("Заказать");
        btn.getStyleClass().add("svc-card-btn");
        btn.setOnAction(e -> orderService(svc));

        footer.getChildren().addAll(price, btn);
        body.getChildren().addAll(name, desc, spacer, footer);
        card.getChildren().addAll(header, body);
        return card;
    }

    private String getCategoryIcon(String cat) {
        if (cat == null) return "🔧";
        return switch (cat) {
            case "Кузов и покраска" -> "🎨";
            case "Двигатель" -> "⚡";
            case "Подвеска" -> "🔩";
            case "Интерьер" -> "🪑";
            case "Мультимедиа" -> "📱";
            case "Аэродинамика" -> "💨";
            default -> "🔧";
        };
    }

    private void orderService(TuningService svc) {
        System.setProperty("selected.service.id", String.valueOf(svc.getId()));
        SceneManager.navigate("client-new-order");
    }

    @FXML
    private void goToDashboard() {
        SceneManager.navigate("client-dashboard");
    }

    @FXML
    private void goToCatalog() {
    }

    @FXML
    private void goToOrders() {
        SceneManager.navigate("client-orders");
    }

    @FXML
    private void goToNewOrder() {
        SceneManager.navigate("client-new-order");
    }

    @FXML
    private void goToProfile() {
        SceneManager.navigate("client-profile");
    }

    @FXML
    private void goToPayHistory() {
        SceneManager.navigate("client-pay-history");
    }
}
