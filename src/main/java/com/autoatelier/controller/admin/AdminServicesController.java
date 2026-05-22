package com.autoatelier.controller.admin;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.TuningService;
import com.autoatelier.service.CatalogService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AdminServicesController extends BaseController {

    @FXML private TableView<TuningService> servicesTable;
    @FXML private TableColumn<TuningService, String> colName;
    @FXML private TableColumn<TuningService, String> colCategory;
    @FXML private TableColumn<TuningService, String> colPrice;
    @FXML private TableColumn<TuningService, String> colActive;

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextArea  descriptionArea;
    @FXML private Button    saveButton;
    @FXML private Button    deleteButton;
    @FXML private Label     formStatus;

    private TuningService editingService;

    @Override
    protected void onInit() {
        setupTable();
        loadServices();
        deleteButton.setDisable(true);
    }

    private void setupTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategory() != null ? c.getValue().getCategory() : "—"));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriceFormatted()));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(
                Boolean.TRUE.equals(c.getValue().getActive()) ? "✅ Да" : "❌ Нет"));
        servicesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> { if (n != null) fillForm(n); });
    }

    private void loadServices() {
        new Thread(() -> {
            try {
                List<TuningService> services = CatalogService.getInstance().getAllServices();
                Platform.runLater(() -> {
                    servicesTable.getItems().setAll(services);
                    formStatus.setText("Услуг: " + services.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> formStatus.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private void fillForm(TuningService svc) {
        editingService = svc;
        nameField.setText(svc.getName());
        categoryField.setText(svc.getCategory() != null ? svc.getCategory() : "");
        priceField.setText(svc.getPrice() != null ? String.valueOf(svc.getPrice().intValue()) : "");
        descriptionArea.setText(svc.getDescription() != null ? svc.getDescription() : "");
        deleteButton.setDisable(false);
    }

    @FXML private void handleNew() {
        editingService = null;
        nameField.clear();
        categoryField.clear();
        priceField.clear();
        descriptionArea.clear();
        deleteButton.setDisable(true);
        formStatus.setText("");
        servicesTable.getSelectionModel().clearSelection();
    }

    @FXML private void handleSave() {
        String name      = nameField.getText().trim();
        String category  = categoryField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isBlank()) { formStatus.setText("Введите название услуги"); return; }

        Double price = null;
        if (!priceText.isBlank()) {
            try { price = Double.parseDouble(priceText); }
            catch (NumberFormatException e) { formStatus.setText("Некорректная цена"); return; }
        }

        saveButton.setDisable(true);
        formStatus.setText("Сохранение...");

        final Double finalPrice    = price;
        final String finalName     = name;
        final String finalCategory = category.isBlank() ? null : category;
        final String finalDesc     = descriptionArea.getText().trim();
        final String existingImage = editingService != null ? editingService.getImageUrl() : null;

        new Thread(() -> {
            try {
                if (editingService == null) {
                    CatalogService.getInstance().createService(
                            finalName, finalDesc.isBlank() ? null : finalDesc,
                            finalPrice, finalCategory, existingImage);
                } else {
                    editingService.setName(finalName);
                    editingService.setCategory(finalCategory);
                    editingService.setPrice(finalPrice);
                    editingService.setDescription(finalDesc.isBlank() ? null : finalDesc);
                    CatalogService.getInstance().updateService(editingService);
                }
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    formStatus.setText("✅ Сохранено!");
                    loadServices();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    formStatus.setText("Ошибка: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML private void handleDelete() {
        if (editingService == null) return;
        if (!AlertUtil.confirm("Удалить услугу",
                "Удалить «" + editingService.getName() + "»? Это действие нельзя отменить.")) return;
        new Thread(() -> {
            try {
                CatalogService.getInstance().deleteService(editingService.getId());
                Platform.runLater(() -> { handleNew(); loadServices(); });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML private void goToDashboard() { SceneManager.navigate("admin-dashboard"); }
    @FXML private void goToUsers()     { SceneManager.navigate("admin-users"); }
    @FXML private void goToStats()     { SceneManager.navigate("admin-stats"); }
    @FXML private void goToArchive()   { SceneManager.navigate("admin-archive"); }
    @FXML private void goToServices()  {  }
    @FXML private void goToProfile()   { SceneManager.navigate("admin-profile"); }
}
