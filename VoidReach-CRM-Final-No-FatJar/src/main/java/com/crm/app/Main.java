package com.crm.app;

import com.crm.controller.SplashScreenController;
import com.crm.controller.LoginController;
import com.crm.controller.MainController;
import com.crm.model.UserAccount;
import com.crm.repository.LocalUserRepository;
import com.crm.service.SessionService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private Stage mainStage;
    private Stage splashStage;
    private SplashScreenController splashController;
    private Parent loginRoot;
    private LoginController loginController;
    private MainController rememberedAppController;
    private UserAccount rememberedUser;
    private final SessionService sessionService = new SessionService(new LocalUserRepository());

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.mainStage = primaryStage;
        showSplashScreen();
    }

    private void showSplashScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/crm/view/SplashScreen.fxml"));
        Parent root = loader.load();
        splashController = loader.getController();

        splashStage = new Stage();
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setAlwaysOnTop(true);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/style-dark.css").toExternalForm());
        splashStage.setScene(scene);
        
        splashStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        splashStage.centerOnScreen();
        splashStage.show();
        splashStage.toFront();
        splashStage.requestFocus();

        startLoadingTask();
    }

    private void startLoadingTask() {
        Task<UserAccount> loadTask = new Task<>() {
            @Override
            protected UserAccount call() throws Exception {
                // 1. Core Initialization
                updateMessage("Core Initialization...");
                updateProgress(0.1, 1.0);
                Thread.sleep(400); 

                // 2. Authentication and session loading
                updateMessage("Checking saved session...");
                updateProgress(0.3, 1.0);
                UserAccount savedUser = sessionService.getRememberedUser().orElse(null);
                if (savedUser != null) updateMessage("Loading your workspace...");
                updateProgress(0.6, 1.0);
                Thread.sleep(300);

                // 3. Database and Model Setup
                updateMessage("Preparing local account storage...");
                updateProgress(0.8, 1.0);
                Thread.sleep(400);

                // 4. Finalization
                updateMessage("Interface Optimization...");
                updateProgress(1.0, 1.0);
                Thread.sleep(300);

                return savedUser;
            }
        };

        loadTask.messageProperty().addListener((obs, old, msg) -> splashController.setStatus(msg));
        loadTask.progressProperty().addListener((obs, old, prog) -> splashController.setProgress(prog.doubleValue()));

        loadTask.setOnSucceeded(e -> {
            try {
                rememberedUser = loadTask.getValue();
                loadLoginView();
                if (rememberedUser != null) loadAndShowRememberedApp();
                else transitionToLogin();
            } catch (Exception ex) {
                throw new IllegalStateException("Impossibile caricare l'interfaccia", ex);
            }
        });

        Thread loaderThread = new Thread(loadTask, "voidreach-startup-loader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    private void loadLoginView() throws Exception {
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/com/crm/view/LoginView.fxml"));
        loginRoot = loginLoader.load();
        loginController = loginLoader.getController();
        configureLoginHandler();
    }

    private void loadAndShowRememberedApp() throws Exception {
        FXMLLoader appLoader = new FXMLLoader(getClass().getResource("/com/crm/view/MainView.fxml"));
        Parent root = appLoader.load();
        rememberedAppController = appLoader.getController();
        transitionToRememberedApp(root);
    }

    private void transitionToLogin() {
        if (loginRoot == null) return;

        // 1. Main Stage Setup
        mainStage.setTitle("VoidReach CRM — Accesso");
        mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        
        Scene scene = new Scene(loginRoot);
        scene.setFill(Color.web("#0f172a"));
        scene.getStylesheets().add(getClass().getResource("/css/style-dark.css").toExternalForm());
        
        mainStage.setScene(scene);
        mainStage.setMaximized(false);
        mainStage.setWidth(580);
        mainStage.setHeight(700);
        mainStage.centerOnScreen();
        showMainStage(loginController::requestInitialFocus);
    }

    private void transitionToRememberedApp(Parent root) {
        if (root == null || rememberedAppController == null) return;
        mainStage.setTitle("VoidReach CRM");
        mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        rememberedAppController.setCurrentUser(rememberedUser, this::logout);
        Scene scene = new Scene(root);
        scene.setFill(Color.web("#0f172a"));
        scene.getStylesheets().add(getClass().getResource("/css/style-dark.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.setMaximized(true);
        showMainStage(rememberedAppController::requestInitialFocus);
    }

    private void configureLoginHandler() {
        loginController.setOnAuthenticated((user, remember) -> {
            if (remember) sessionService.remember(user);
            else sessionService.forget();
            showMainApplication(user);
        });
    }

    private void showMainApplication(UserAccount user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/crm/view/MainView.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setCurrentUser(user, this::logout);
            Scene scene = new Scene(root);
            scene.setFill(Color.web("#0f172a"));
            scene.getStylesheets().add(getClass().getResource("/css/style-dark.css").toExternalForm());
            mainStage.setTitle("VoidReach CRM");
            mainStage.setScene(scene);
            mainStage.setMaximized(true);
            mainStage.show();
            mainStage.toFront();
            mainStage.requestFocus();
            Platform.runLater(controller::requestInitialFocus);
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile aprire l'applicazione", e);
        }
    }

    private void showLoginScreen() {
        mainStage.setTitle("VoidReach CRM — Accesso");
        configureLoginHandler();
        Scene loginScene = new Scene(loginRoot);
        loginScene.setFill(Color.web("#0f172a"));
        loginScene.getStylesheets().add(getClass().getResource("/css/style-dark.css").toExternalForm());
        mainStage.setScene(loginScene);
        mainStage.setMaximized(false);
        mainStage.setWidth(580);
        mainStage.setHeight(700);
        mainStage.centerOnScreen();
        bringToFront(loginController::requestInitialFocus);
    }

    private void logout() {
        sessionService.forget();
        showLoginScreen();
    }

    private void showMainStage(Runnable focusTarget) {
        if (splashStage != null) splashStage.close();
        mainStage.setOpacity(1.0);
        bringToFront(focusTarget);
    }

    /** Focuses the JavaFX window without starting a second native GUI toolkit. */
    private void bringToFront(Runnable focusTarget) {
        mainStage.setIconified(false);
        mainStage.show();
        mainStage.toFront();
        mainStage.requestFocus();
        Platform.runLater(() -> {
            if (!mainStage.isShowing()) return;
            mainStage.toFront();
            mainStage.requestFocus();
            focusTarget.run();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
