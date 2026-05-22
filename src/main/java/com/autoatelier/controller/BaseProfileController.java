package com.autoatelier.controller;

import com.autoatelier.service.ProfileService;
import com.autoatelier.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public abstract class BaseProfileController extends BaseController {

    @FXML protected Label         emailLabel;
    @FXML protected TextField     nameField;
    @FXML protected TextField     phoneField;
    @FXML protected Label         profileStatus;
    @FXML protected PasswordField newPasswordField;
    @FXML protected PasswordField confirmPasswordField;
    @FXML protected Label         passwordStatus;

    @Override
    protected void onInit() {
        var user = SessionManager.getInstance().getCurrentUser();
        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
        nameField.setText(user.getFullName() != null ? user.getFullName() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
    }

    @FXML
    protected void handleSaveProfile() {
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

    @FXML
    protected void handleChangePassword() {
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

    protected void setStatus(Label label, String text, boolean ok) {
        label.setText(text);
        label.getStyleClass().removeAll("status-ok", "status-error");
        label.getStyleClass().add(ok ? "status-ok" : "status-error");
    }
}
