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
    /** Clears form state before a retained login view is shown for a new session. */
    public void resetForLoginScreen() {
        loginEmail.clear();
        registerName.clear();
        registerEmail.clear();
        recoveryEmail.clear();
        resetEmail.clear();
        resetCode.clear();
        rememberLogin.setSelected(false);
        show(loginPane);
    }
    @FXML private void showLogin() { show(loginPane); }
    @FXML private void showRegister() { show(registerPane); }
    @FXML private void showRecovery() { show(recoveryPane); }
    private void show(VBox pane) {
        clearPasswordFields();
        resetCode.clear();
        if (pane != resetPane) resetEmail.clear();
        loginPane.setVisible(false); registerPane.setVisible(false); recoveryPane.setVisible(false); resetPane.setVisible(false);
        loginPane.setManaged(false); registerPane.setManaged(false); recoveryPane.setManaged(false); resetPane.setManaged(false);
        pane.setVisible(true); pane.setManaged(true); clearMessages();
        if (pane == loginPane) Platform.runLater(loginEmail::requestFocus);
        else if (pane == registerPane) Platform.runLater(registerName::requestFocus);
        else if (pane == recoveryPane) Platform.runLater(recoveryEmail::requestFocus);
        else Platform.runLater(resetCode::requestFocus);
    }
    private void clearMessages() { loginMessage.setText(""); registerMessage.setText(""); recoveryMessage.setText(""); resetMessage.setText(""); }
    private void clearPasswordFields() {
        loginPassword.clear();
        registerPassword.clear();
        registerPasswordConfirm.clear();
        resetPassword.clear();
        resetPasswordConfirm.clear();
    }

    @FXML private void handleLogin() {
        UserAccount user;
        try {
            user = auth.login(loginEmail.getText(), loginPassword.getText());
        } catch (IllegalArgumentException e) {
            loginMessage.setText(e.getMessage());
            return;
        } catch (IllegalStateException e) {
            loginMessage.setText("Impossibile leggere i dati locali. Il backup verrà usato se disponibile.");
            return;
        } finally {
            loginPassword.clear();
        }
        authenticated.accept(user, rememberLogin.isSelected());
    }

    @FXML private void handleRegister() {
        UserAccount user;
        try {
            if (!registerPassword.getText().equals(registerPasswordConfirm.getText())) throw new IllegalArgumentException("Le password non coincidono.");
            user = auth.register(registerName.getText(), registerEmail.getText(), registerPassword.getText());
        } catch (IllegalArgumentException e) {
            registerMessage.setText(e.getMessage());
            return;
        } catch (IllegalStateException e) {
            registerMessage.setText("Impossibile salvare l'account. Riprova senza chiudere l'app.");
            return;
        } finally {
            registerPassword.clear();
            registerPasswordConfirm.clear();
        }
        authenticated.accept(user, false);
    }

    @FXML private void handleRecovery() {
        try {
            String email = recoveryEmail.getText().trim();
            String code = auth.requestPasswordReset(email);
            recoveryEmail.clear();
            show(resetPane);
            resetEmail.setText(email);
            resetMessage.setText("Codice locale generato. Per questa demo: " + code + " (valido 15 minuti).");
        } catch (IllegalArgumentException e) { recoveryMessage.setText(e.getMessage()); }
        catch (IllegalStateException e) { recoveryMessage.setText("Impossibile salvare la richiesta di recupero. Riprova senza chiudere l'app."); }
    }

    @FXML private void handleReset() {
        String email;
        try {
            if (!resetPassword.getText().equals(resetPasswordConfirm.getText())) throw new IllegalArgumentException("Le password non coincidono.");
            email = resetEmail.getText();
            auth.resetPassword(email, resetCode.getText(), resetPassword.getText());
        } catch (IllegalArgumentException e) {
            resetMessage.setText(e.getMessage());
            return;
        } catch (IllegalStateException e) {
            resetMessage.setText("Impossibile salvare la nuova password. Riprova senza chiudere l'app.");
            return;
        } finally {
            resetPassword.clear();
            resetPasswordConfirm.clear();
            resetCode.clear();
        }
        loginEmail.setText(email);
        resetEmail.clear();
        show(loginPane);
        loginMessage.setText("Password aggiornata. Ora puoi accedere.");
    }
}
