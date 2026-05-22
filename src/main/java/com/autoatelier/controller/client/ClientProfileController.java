package com.autoatelier.controller.client;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.PaymentCard;
import com.autoatelier.service.ProfileService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import com.autoatelier.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class ClientProfileController extends BaseController {

    @FXML private Label    emailLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private Label    profileStatus;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         passwordStatus;

    @FXML private VBox      cardsListBox;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardHolderField;
    @FXML private TextField expiryMonthField;
    @FXML private TextField expiryYearField;
    @FXML private Label     cardStatus;

    @Override
    protected void onInit() {
        var user = SessionManager.getInstance().getCurrentUser();
        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
        nameField.setText(user.getFullName() != null ? user.getFullName() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        loadCards();
    }

    @FXML private void handleSaveProfile() {
        String name  = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        if (name.isEmpty()) { setStatus(profileStatus, "Введите имя", false); return; }
        setStatus(profileStatus, "Сохранение…", true);
        new Thread(() -> {
            try {
                ProfileService.getInstance().updateProfile(name, phone);
                Platform.runLater(() -> setStatus(profileStatus, "Сохранено ✓", true));
            } catch (Exception e) {
                Platform.runLater(() -> setStatus(profileStatus, "Ошибка: " + e.getMessage(), false));
            }
        }).start();
    }

    @FXML private void handleChangePassword() {
        String np = newPasswordField.getText();
        String cp = confirmPasswordField.getText();
        if (np.length() < 6) { setStatus(passwordStatus, "Минимум 6 символов", false); return; }
        if (!np.equals(cp))  { setStatus(passwordStatus, "Пароли не совпадают", false); return; }
        setStatus(passwordStatus, "Применяется…", true);
        new Thread(() -> {
            try {
                ProfileService.getInstance().changePassword(np);
                Platform.runLater(() -> {
                    setStatus(passwordStatus, "Пароль изменён ✓", true);
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus(passwordStatus, "Ошибка: " + e.getMessage(), false));
            }
        }).start();
    }

    @FXML private void handleDeleteAccount() {
        if (!AlertUtil.confirm("Удалить аккаунт",
                "Все ваши данные будут удалены безвозвратно. Продолжить?")) return;
        new Thread(() -> {
            try {
                ProfileService.getInstance().deleteAccount();
                Platform.runLater(() -> SceneManager.navigate("login"));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", "Не удалось удалить аккаунт: " + e.getMessage()));
            }
        }).start();
    }

    private void loadCards() {
        new Thread(() -> {
            try {
                List<PaymentCard> cards = ProfileService.getInstance().getMyCards();
                Platform.runLater(() -> renderCards(cards));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label err = new Label("Не удалось загрузить карты");
                    err.getStyleClass().add("page-subtitle");
                    cardsListBox.getChildren().setAll(err);
                });
            }
        }).start();
    }

    private void renderCards(List<PaymentCard> cards) {
        cardsListBox.getChildren().clear();
        if (cards.isEmpty()) {
            Label empty = new Label("Карты не добавлены");
            empty.getStyleClass().add("page-subtitle");
            cardsListBox.getChildren().add(empty);
            return;
        }
        for (PaymentCard card : cards) {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/autoatelier/views/components/PaymentCardView.fxml"));
                VBox cardNode = loader.load();

                ((Label) cardNode.lookup("#numberLabel")).setText(card.getMaskedNumber());
                ((Label) cardNode.lookup("#holderLabel")).setText(
                    card.getCardHolder() != null ? card.getCardHolder().toUpperCase() : "—");
                ((Label) cardNode.lookup("#expiryLabel")).setText(card.getExpiry());

                String num = card.getCardNumber() != null ? card.getCardNumber() : "";
                String network = num.contains("4") ? "VISA" :
                                 num.contains("5") ? "MASTERCARD" : "CARD";
                ((Label) cardNode.lookup("#networkLabel")).setText(network);

                ((Button) cardNode.lookup("#deleteBtn"))
                    .setOnAction(e -> deleteCard(card.getId()));

                cardsListBox.getChildren().add(cardNode);
            } catch (Exception ex) {
            }
        }
    }

    private void deleteCard(Long cardId) {
        new Thread(() -> {
            try {
                ProfileService.getInstance().deleteCard(cardId);
                Platform.runLater(this::loadCards);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", "Не удалось удалить карту: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleAddCard() {
        String number = cardNumberField.getText().trim();
        String holder = cardHolderField.getText().trim();
        String month  = expiryMonthField.getText().trim();
        String year   = expiryYearField.getText().trim();

        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() < 13 || digits.length() > 19) {
            setStatus(cardStatus, "Неверный номер карты", false); return;
        }
        if (holder.isEmpty()) {
            setStatus(cardStatus, "Введите имя держателя", false); return;
        }
        if (month.isEmpty() || year.isEmpty()) {
            setStatus(cardStatus, "Введите срок действия", false); return;
        }
        setStatus(cardStatus, "Сохранение…", true);
        new Thread(() -> {
            try {
                ProfileService.getInstance().addCard(number, holder, month, year);
                Platform.runLater(() -> {
                    setStatus(cardStatus, "Карта добавлена ✓", true);
                    cardNumberField.clear(); cardHolderField.clear();
                    expiryMonthField.clear(); expiryYearField.clear();
                    loadCards();
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus(cardStatus, "Ошибка: " + e.getMessage(), false));
            }
        }).start();
    }

    @FXML private void goToDashboard() { SceneManager.navigate("client-dashboard"); }
    @FXML private void goToCatalog()   { SceneManager.navigate("client-catalog"); }
    @FXML private void goToNewOrder()  { SceneManager.navigate("client-new-order"); }
    @FXML private void goToOrders()    { SceneManager.navigate("client-orders"); }
    @FXML private void goToProfile()   {  }

    private void setStatus(Label label, String text, boolean ok) {
        label.setText(text);
        label.getStyleClass().removeAll("status-ok", "status-error");
        label.getStyleClass().add(ok ? "status-ok" : "status-error");
    }

    @FXML private void goToPayHistory() { com.autoatelier.util.SceneManager.navigate("client-pay-history"); }
}
