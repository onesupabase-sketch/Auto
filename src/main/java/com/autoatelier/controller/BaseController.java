package com.autoatelier.controller;

import com.autoatelier.service.AuthService;
import com.autoatelier.util.SceneManager;
import com.autoatelier.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class BaseController {

    @FXML protected Label userNameLabel;
    @FXML protected Label userRoleLabel;

    @FXML
    public void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText(SessionManager.getInstance().getCurrentUser().getFullName());
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(SessionManager.getInstance().getCurrentUser().getRoleDisplay());
        }
        onInit();
    }

    protected abstract void onInit();

    @FXML
    protected void handleLogout() {
        AuthService.getInstance().logout();
        SceneManager.navigate("login");
    }
}
