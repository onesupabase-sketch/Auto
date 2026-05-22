package com.autoatelier.controller.admin;

import com.autoatelier.controller.BaseController;
import com.autoatelier.model.User;
import com.autoatelier.service.StorageService;
import com.autoatelier.util.AlertUtil;
import com.autoatelier.util.SceneManager;
import com.autoatelier.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AdminUsersController extends BaseController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colDate;
    @FXML private ComboBox<String> roleFilter;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private List<User> allUsers;

    @Override
    protected void onInit() {
        setupTable();
        setupFilters();
        loadUsers();
    }

    private void setupFilters() {
        roleFilter.getItems().addAll("Все", "Клиент", "Менеджер", "Администратор");
        roleFilter.setValue("Все");
        roleFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void setupTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : "—"));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPhone() != null ? c.getValue().getPhone() : "—"));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoleDisplay()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isBlocked() ? "🔒 Заблокирован" : "✅ Активен"));
        colDate.setCellValueFactory(c -> {
            String d = c.getValue().getCreatedAt();
            return new SimpleStringProperty(d != null ? d.substring(0, 10) : "");
        });

        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                User u = getTableView().getItems().get(getIndex());
                String color = u.isAdmin() ? "#E74C3C" : u.isManager() ? "#F5A623" : "#27AE60";
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                User u = getTableView().getItems().get(getIndex());
                setStyle(u.isBlocked()
                    ? "-fx-text-fill: #EF4444; -fx-font-weight: bold;"
                    : "-fx-text-fill: #10B981; -fx-font-weight: bold;");
            }
        });

        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            Menu roleMenu = new Menu("Изменить роль");
            MenuItem toClient  = new MenuItem("👤  Клиент");
            MenuItem toManager = new MenuItem("🔧  Менеджер");
            MenuItem toAdmin   = new MenuItem("⭐  Администратор");
            toClient.setOnAction(e  -> changeRole(row.getItem(), "client"));
            toManager.setOnAction(e -> changeRole(row.getItem(), "manager"));
            toAdmin.setOnAction(e   -> changeRole(row.getItem(), "admin"));
            roleMenu.getItems().addAll(toClient, toManager, toAdmin);

            MenuItem blockItem = new MenuItem("🔒  Заблокировать");
            blockItem.setOnAction(e -> toggleBlock(row.getItem()));

            menu.getItems().addAll(roleMenu, new SeparatorMenuItem(), blockItem);

            menu.setOnShowing(e -> {
                User u = row.getItem();
                if (u != null)
                    blockItem.setText(u.isBlocked() ? "🔓  Разблокировать" : "🔒  Заблокировать");
            });

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null).otherwise(menu));
            return row;
        });
    }

    private void loadUsers() {
        statusLabel.setText("Загрузка пользователей...");
        new Thread(() -> {
            try {
                allUsers = StorageService.getInstance().getAllUsers();
                Platform.runLater(() -> {
                    usersTable.getItems().setAll(allUsers);
                    statusLabel.setText("Всего: " + allUsers.size() + " пользователей");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private void applyFilter() {
        if (allUsers == null) return;
        String roleVal = roleFilter.getValue();
        String search = searchField.getText().toLowerCase().trim();
        String roleKey = switch (roleVal) {
            case "Клиент" -> "client";
            case "Менеджер" -> "manager";
            case "Администратор" -> "admin";
            default -> null;
        };
        List<User> filtered = allUsers.stream()
                .filter(u -> roleKey == null || roleKey.equals(u.getRole()))
                .filter(u -> search.isBlank()
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(search))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(search)))
                .toList();
        usersTable.getItems().setAll(filtered);
        statusLabel.setText("Показано: " + filtered.size());
    }

    private void changeRole(User user, String newRole) {
        if (user == null) return;
        String me = SessionManager.getInstance().getCurrentUser().getId();
        if (user.getId().equals(me)) {
            AlertUtil.error("Ошибка", "Нельзя изменить свою роль"); return;
        }
        String displayRole = switch (newRole) {
            case "admin" -> "Администратор";
            case "manager" -> "Менеджер";
            default -> "Клиент";
        };
        if (!AlertUtil.confirm("Изменить роль",
                "Назначить пользователю «" + user.getFullName() + "» роль «" + displayRole + "»?")) return;
        new Thread(() -> {
            try {
                StorageService.getInstance().updateUserRole(user.getId(), newRole);
                Platform.runLater(() -> { AlertUtil.info("Готово", "Роль обновлена"); loadUsers(); });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void toggleBlock(User user) {
        if (user == null) return;
        String me = SessionManager.getInstance().getCurrentUser().getId();
        if (user.getId().equals(me)) {
            AlertUtil.error("Ошибка", "Нельзя заблокировать себя"); return;
        }
        boolean willBlock = !user.isBlocked();
        String action = willBlock ? "заблокировать" : "разблокировать";
        if (!AlertUtil.confirm(willBlock ? "Блокировка" : "Разблокировка",
                "Вы хотите " + action + " пользователя «" + user.getFullName() + "»?")) return;
        new Thread(() -> {
            try {
                StorageService.getInstance().setUserBlocked(user.getId(), willBlock);
                Platform.runLater(() -> {
                    AlertUtil.info("Готово", "Пользователь " + (willBlock ? "заблокирован" : "разблокирован"));
                    loadUsers();
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.error("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML private void refresh()        { loadUsers(); }
    @FXML private void goToDashboard()  { SceneManager.navigate("admin-dashboard"); }
    @FXML private void goToUsers()      {  }
    @FXML private void goToServices()   { SceneManager.navigate("admin-services"); }
    @FXML private void goToStats()      { SceneManager.navigate("admin-stats"); }
    @FXML private void goToArchive()    { SceneManager.navigate("admin-archive"); }
    @FXML private void goToProfile()    { SceneManager.navigate("admin-profile"); }
}
