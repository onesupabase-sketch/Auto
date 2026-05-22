package com.autoatelier.controller.manager;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.TuningService;
import com.autoatelier.service.CatalogService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ManagerCatalogController extends BaseController {

    @FXML private VBox servicesListBox;
    @FXML private VBox editPane;
    @FXML private Label editPaneTitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private CheckBox showInactiveCheck;
    @FXML private Label catalogStatus;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox activeCheck;
    @FXML private Label formStatus;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private static final java.util.List<String> KNOWN_CATEGORIES = java.util.List.of(
        "Кузов и покраска", "Двигатель", "Подвеска",
        "Интерьер", "Мультимедиа", "Аэродинамика"
    );

    private TuningService editingService;
    private List<TuningService> allServices;

    @Override
    protected void onInit() {
        editPane.setVisible(false);
        editPane.setManaged(false);

        categoryCombo.getItems().addAll(KNOWN_CATEGORIES);

        categoryFilter.getItems().add("Все категории");
        categoryFilter.setValue("Все категории");
        categoryFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        loadServices();
    }

    private void loadServices() {
        catalogStatus.setText("Загрузка...");
        new Thread(() -> {
            try {
                allServices = CatalogService.getInstance().getAllServices();
                List<String> cats = allServices.stream()
                        .map(TuningService::getCategory)
                        .filter(c -> c != null && !c.isBlank())
                        .distinct().sorted().toList();

                Platform.runLater(() -> {
                    String prev = categoryFilter.getValue();
                    categoryFilter.getItems().clear();
                    categoryFilter.getItems().add("Все категории");
                    categoryFilter.getItems().addAll(cats);
                    categoryFilter.setValue(prev != null ? prev : "Все категории");

                    catalogStatus.setText("Услуг в каталоге: " + allServices.size());
                    applyFilter();
                });
            } catch (Exception e) {
                Platform.runLater(() -> catalogStatus.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void applyFilter() {
        if (allServices == null) return;
        String cat   = categoryFilter.getValue();
        String q     = searchField.getText().toLowerCase().trim();
        boolean showInactive = showInactiveCheck.isSelected();

        List<TuningService> filtered = allServices.stream()
                .filter(s -> showInactive || Boolean.TRUE.equals(s.getActive()))
                .filter(s -> cat == null || "Все категории".equals(cat) || cat.equals(s.getCategory()))
                .filter(s -> q.isBlank() || (s.getName() != null && s.getName().toLowerCase().contains(q)))
                .toList();

        renderServices(filtered);
    }

    private void renderServices(List<TuningService> list) {
        servicesListBox.getChildren().clear();
        if (list.isEmpty()) {
            Label lbl = new Label("Услуг не найдено");
            lbl.getStyleClass().add("page-subtitle");
            servicesListBox.getChildren().add(lbl);
            return;
        }
        list.forEach(s -> servicesListBox.getChildren().add(buildServiceRow(s)));
    }

    private HBox buildServiceRow(TuningService svc) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("service-list-row");
        row.setCursor(Cursor.HAND);
        row.setOnMouseClicked(e -> openEdit(svc));

        Label dot = new Label(Boolean.TRUE.equals(svc.getActive()) ? "●" : "○");
        dot.setStyle("-fx-text-fill: " + (Boolean.TRUE.equals(svc.getActive()) ? "#10B981" : "#CBD5E0") + "; -fx-font-size: 10px;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(svc.getName());
        name.getStyleClass().add("service-row-name");
        Label cat = new Label((svc.getCategory() != null ? svc.getCategory() : "—") +
                (Boolean.TRUE.equals(svc.getActive()) ? "" : "  ·  Скрыта"));
        cat.getStyleClass().add("service-row-cat");
        info.getChildren().addAll(name, cat);

        Label price = new Label(svc.getPriceFormatted());
        price.getStyleClass().add("service-row-price");

        Button editBtn = new Button("Изменить");
        editBtn.getStyleClass().add("btn-outline-sm");
        editBtn.setOnAction(e -> openEdit(svc));

        row.getChildren().addAll(dot, info, price, editBtn);
        return row;
    }

    private void openEdit(TuningService svc) {
        editingService = svc;
        editPaneTitle.setText("Редактировать услугу");
        nameField.setText(svc.getName());
        categoryCombo.setValue(svc.getCategory() != null ? svc.getCategory() : "");
        priceField.setText(svc.getPrice() != null ? String.valueOf(svc.getPrice().intValue()) : "0");
        descriptionArea.setText(svc.getDescription() != null ? svc.getDescription() : "");
        activeCheck.setSelected(Boolean.TRUE.equals(svc.getActive()));
        deleteButton.setDisable(false);
        formStatus.setText("");
        editPane.setVisible(true);
        editPane.setManaged(true);
    }

    @FXML
    private void handleNew() {
        editingService = null;
        editPaneTitle.setText("Новая услуга");
        nameField.clear();
        categoryCombo.setValue(null);
        priceField.setText("0");
        descriptionArea.clear();
        activeCheck.setSelected(true);
        deleteButton.setDisable(true);
        formStatus.setText("");
        editPane.setVisible(true);
        editPane.setManaged(true);
        nameField.requestFocus();
    }

    @FXML
    private void handleSave() {
        String name     = nameField.getText().trim();
        String category = categoryCombo.getValue() != null ? categoryCombo.getValue().trim() :
                (categoryCombo.getEditor() != null ? categoryCombo.getEditor().getText().trim() : "");
        String priceStr = priceField.getText().trim();
        String desc     = descriptionArea.getText().trim();
        boolean active  = activeCheck.isSelected();

        if (name.isBlank()) { formStatus.setText("Введите название услуги"); return; }

        Double price = 0.0;
        if (!priceStr.isBlank()) {
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { formStatus.setText("Некорректная цена"); return; }
        }

        saveButton.setDisable(true);
        formStatus.setText("Сохранение...");

        final Double finalPrice = price;
        final String finalCat   = category.isBlank() ? null : category;
        final String finalDesc  = desc.isBlank() ? null : desc;

        new Thread(() -> {
            try {
                if (editingService == null) {
                    CatalogService.getInstance().createService(name, finalDesc, finalPrice, finalCat, null);
                } else {
                    editingService.setName(name);
                    editingService.setCategory(finalCat);
                    editingService.setPrice(finalPrice);
                    editingService.setDescription(finalDesc);
                    editingService.setActive(active);
                    CatalogService.getInstance().updateService(editingService);
                }
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    formStatus.setText("✅ Сохранено!");
                    loadServices();

                    new Thread(() -> {
                        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::handleCancelEdit);
                    }).start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    formStatus.setText("Ошибка: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleDelete() {
        if (editingService == null) return;
        if (!AlertUtil.confirm("Удалить услугу",
                "Удалить \"" + editingService.getName() + "\"?\n\nЭто действие нельзя отменить."))
            return;
        new Thread(() -> {
            try {
                CatalogService.getInstance().deleteService(editingService.getId());
                Platform.runLater(() -> {
                    handleCancelEdit();
                    loadServices();
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCancelEdit() {
        editPane.setVisible(false);
        editPane.setManaged(false);
        editingService = null;
    }

    @FXML private void goToDashboard()  { SceneManager.navigate("manager-dashboard"); }
    @FXML private void goToCatalog()    {  }
    @FXML private void goToProfile()    { SceneManager.navigate("manager-profile"); }
    @FXML private void goToOrders()     { SceneManager.navigate("manager-orders"); }
}
