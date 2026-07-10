package com.crm.controller;

import com.crm.model.UserAccount;
import com.crm.repository.LocalUserRepository;
import com.crm.service.AuthService;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML private VBox loginPane, registerPane, recoveryPane, resetPane;
    @FXML private TextField loginEmail, registerName, registerEmail, recoveryEmail, resetEmail, resetCode;
    @FXML private PasswordField loginPassword, registerPassword, registerPasswordConfirm, resetPassword, resetPasswordConfirm;
    @FXML private CheckBox rememberLogin;
    @FXML private Label loginMessage, registerMessage, recoveryMessage, resetMessage;
    private final AuthService auth = new AuthService(new LocalUserRepository());
    private BiConsumer<UserAccount, Boolean> authenticated;

    public void setOnAuthenticated(BiConsumer<UserAccount, Boolean> authenticated) { this.authenticated = authenticated; }
    public void requestInitialFocus() { Platform.runLater(loginEmail::requestFocus); }
    @FXML private void showLogin() { show(loginPane); }
    @FXML private void showRegister() { show(registerPane); }
    @FXML private void showRecovery() { show(recoveryPane); }
    private void show(VBox pane) {
        loginPane.setVisible(false); registerPane.setVisible(false); recoveryPane.setVisible(false); resetPane.setVisible(false);
        loginPane.setManaged(false); registerPane.setManaged(false); recoveryPane.setManaged(false); resetPane.setManaged(false);
        pane.setVisible(true); pane.setManaged(true); clearMessages();
        if (pane == loginPane) Platform.runLater(loginEmail::requestFocus);
        else if (pane == registerPane) Platform.runLater(registerName::requestFocus);
        else if (pane == recoveryPane) Platform.runLater(recoveryEmail::requestFocus);
        else Platform.runLater(resetCode::requestFocus);
    }
    private void clearMessages() { loginMessage.setText(""); registerMessage.setText(""); recoveryMessage.setText(""); resetMessage.setText(""); }

    @FXML private void handleLogin() { try { authenticated.accept(auth.login(loginEmail.getText(), loginPassword.getText()), rememberLogin.isSelected()); } catch (IllegalArgumentException e) { loginMessage.setText(e.getMessage()); } }
    @FXML private void handleRegister() { try { if (!registerPassword.getText().equals(registerPasswordConfirm.getText())) throw new IllegalArgumentException("Le password non coincidono."); authenticated.accept(auth.register(registerName.getText(), registerEmail.getText(), registerPassword.getText()), false); } catch (IllegalArgumentException e) { registerMessage.setText(e.getMessage()); } }
    @FXML private void handleRecovery() { try { String code = auth.requestPasswordReset(recoveryEmail.getText()); resetEmail.setText(recoveryEmail.getText().trim()); show(resetPane); resetMessage.setText("Codice locale generato. Per questa demo: " + code + " (valido 15 minuti)."); } catch (IllegalArgumentException e) { recoveryMessage.setText(e.getMessage()); } }
    @FXML private void handleReset() { try { if (!resetPassword.getText().equals(resetPasswordConfirm.getText())) throw new IllegalArgumentException("Le password non coincidono."); auth.resetPassword(resetEmail.getText(), resetCode.getText(), resetPassword.getText()); loginEmail.setText(resetEmail.getText()); loginPassword.clear(); show(loginPane); loginMessage.setText("Password aggiornata. Ora puoi accedere."); } catch (IllegalArgumentException e) { resetMessage.setText(e.getMessage()); } }
}
