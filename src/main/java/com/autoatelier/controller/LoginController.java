package com.autoatelier.controller;

import com.autoatelier.model.User;
import com.autoatelier.service.AuthService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private VBox loadingOverlay;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        if (loadingOverlay != null) loadingOverlay.setVisible(false);

        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showError("Введите email и пароль");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                User user = AuthService.getInstance().login(email, password);
                Platform.runLater(() -> navigateByRole(user));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError(ex.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleRegister() {
        SceneManager.navigate("register");
    }

    private void navigateByRole(User user) {
        setLoading(false);
        if (user.isAdmin()) {
            SceneManager.navigate("admin-dashboard");
        } else if (user.isManager()) {
            SceneManager.navigate("manager-dashboard");
        } else {
            SceneManager.navigate("client-dashboard");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        if (loadingOverlay != null) loadingOverlay.setVisible(loading);
    }
}
