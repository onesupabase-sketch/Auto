package com.autoatelier.controller.manager;

import com.autoatelier.controller.BaseProfileController;
import com.autoatelier.util.SceneManager;
import javafx.fxml.FXML;

public class ManagerProfileController extends BaseProfileController {
    @FXML private void goToDashboard() { SceneManager.navigate("manager-dashboard"); }
    @FXML private void goToOrders()    { SceneManager.navigate("manager-orders"); }
    @FXML private void goToCatalog()   { SceneManager.navigate("manager-catalog"); }
    @FXML private void goToProfile()   {  }
}
