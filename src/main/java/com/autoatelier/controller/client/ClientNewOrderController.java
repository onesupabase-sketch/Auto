package com.autoatelier.controller.client;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.PaymentCard;
import com.autoatelier.model.TuningService;
import com.autoatelier.service.CatalogService;
import com.autoatelier.service.OrderService;
import com.autoatelier.service.ProfileService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.List;

public class ClientNewOrderController extends BaseController {

    @FXML private ComboBox<TuningService> serviceCombo;
    @FXML private TextField   carModelField;
    @FXML private TextField   carYearField;
    @FXML private TextArea    descriptionArea;
    @FXML private Label       priceLabel;
    @FXML private HBox        priceRow;
    @FXML private Button      submitButton;
    @FXML private Label       errorLabel;

    @FXML private VBox        noCardsBox;
    @FXML private VBox        cardsBox;
    @FXML private ComboBox<PaymentCard> cardCombo;

    @Override
    protected void onInit() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        serviceCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TuningService s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getName() + "  —  " + s.getPriceFormatted());
            }
        });
        serviceCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TuningService s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Выберите услугу" : s.getName() + "  —  " + s.getPriceFormatted());
            }
        });
        serviceCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                priceLabel.setText(n.getPriceFormatted());
                priceRow.setVisible(true);
                priceRow.setManaged(true);
            }
        });

        cardCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(PaymentCard c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getMaskedNumber() + "  ·  " + c.getCardHolder());
            }
        });
        cardCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(PaymentCard c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "Выберите карту" : c.getMaskedNumber() + "  ·  " + c.getCardHolder());
            }
        });

        loadServices();
        loadCards();

        String repeatModel = System.getProperty("repeat.car.model");
        String repeatYear  = System.getProperty("repeat.car.year");
        if (repeatModel != null) { carModelField.setText(repeatModel); System.clearProperty("repeat.car.model"); }
        if (repeatYear  != null) { carYearField.setText(repeatYear);   System.clearProperty("repeat.car.year"); }
    }

    private void loadServices() {
        new Thread(() -> {
            try {
                List<TuningService> services = CatalogService.getInstance().getActiveServices();
                Platform.runLater(() -> {
                    serviceCombo.getItems().setAll(services);
                    String selId = System.getProperty("selected.service.id");
                    if (selId != null && !selId.isBlank()) {
                        long id = Long.parseLong(selId);
                        services.stream().filter(s -> s.getId() == id)
                                .findFirst().ifPresent(serviceCombo::setValue);
                        System.clearProperty("selected.service.id");
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadCards() {
        new Thread(() -> {
            try {
                List<PaymentCard> cards = ProfileService.getInstance().getMyCards();
                Platform.runLater(() -> {
                    if (cards.isEmpty()) {
                        noCardsBox.setVisible(true);  noCardsBox.setManaged(true);
                        cardsBox.setVisible(false);   cardsBox.setManaged(false);
                        submitButton.setDisable(true);
                    } else {
                        noCardsBox.setVisible(false); noCardsBox.setManaged(false);
                        cardsBox.setVisible(true);    cardsBox.setManaged(true);
                        cardCombo.getItems().setAll(cards);
                        cardCombo.setValue(cards.get(0));
                        submitButton.setDisable(false);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleSubmit() {
        TuningService svc  = serviceCombo.getValue();
        String carModel    = carModelField.getText().trim();
        String yearText    = carYearField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (svc == null) { showError("Выберите услугу из каталога"); return; }
        if (cardCombo.getValue() == null) {
            showError("Необходимо привязать карту для оплаты в разделе Профиль");
            return;
        }
        if (carModel.isBlank()) { showError("Укажите марку и модель автомобиля"); return; }

        Integer carYear = null;
        if (!yearText.isBlank()) {
            try {
                carYear = Integer.parseInt(yearText);
                if (carYear < 1900 || carYear > 2030) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError("Год выпуска должен быть числом от 1900 до 2030");
                return;
            }
        }

        submitButton.setDisable(true);
        final Integer finalYear = carYear;

        new Thread(() -> {
            try {
                OrderService.getInstance().createOrder(
                        svc.getId(), carModel, finalYear,
                        description.isBlank() ? null : description,
                        svc.getPrice()
                );
                Platform.runLater(() -> {
                    AlertUtil.info("Заявка принята!",
                            "Ваша заявка успешно создана.\nМенеджер рассмотрит её в ближайшее время.");
                    SceneManager.navigate("client-orders");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    showError("Не удалось создать заявку: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText("⚠  " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML private void goToProfile()   { SceneManager.navigate("client-profile"); }
    @FXML private void goToDashboard() { SceneManager.navigate("client-dashboard"); }
    @FXML private void goToCatalog()   { SceneManager.navigate("client-catalog"); }
    @FXML private void goToNewOrder()  {  }
    @FXML private void goToOrders()    { SceneManager.navigate("client-orders"); }
    @FXML private void goToPayHistory() { SceneManager.navigate("client-pay-history"); }
}
