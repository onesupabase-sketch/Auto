package com.autoatelier.controller.admin;

import com.autoatelier.controller.BaseProfileController;
import com.autoatelier.util.SceneManager;
import javafx.fxml.FXML;

public class AdminProfileController extends BaseProfileController {
    @FXML private void goToDashboard() { SceneManager.navigate("admin-dashboard"); }
    @FXML private void goToUsers()     { SceneManager.navigate("admin-users"); }
    @FXML private void goToServices()  { SceneManager.navigate("admin-services"); }
    @FXML private void goToStats()     { SceneManager.navigate("admin-stats"); }
    @FXML private void goToProfile()   {  }
}
