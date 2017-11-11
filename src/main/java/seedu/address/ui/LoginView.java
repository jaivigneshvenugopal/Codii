package seedu.address.ui;

import static seedu.address.logic.commands.LoginCommand.isLoggedIn;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PASSWORD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_USERNAME;

import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.ChangeToCommandBoxView;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.logic.Logic;
import seedu.address.logic.Password;
import seedu.address.logic.Username;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

//@@author jelneo
/**
 * Displays username and password fields
 */
public class LoginView extends UiPart<Region> {
    public static final String GUI_LOGIN_COMMAND_FORMAT = "login " + PREFIX_USERNAME + "%1$s"
            + " " + PREFIX_PASSWORD + "%2$s";

    private static final String FXML = "LoginView.fxml";
    private static final Logger logger = LogsCenter.getLogger(LoginView.class);

    private static boolean showingLoginView = false;
    private final Logic logic;
    private ObjectProperty<Username> username;
    private ObjectProperty<Password> password;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    public LoginView(Logic logic) {
        super(FXML);
        this.logic = logic;
        logger.info("Showing login view...");
        usernameField.textProperty().addListener((unused1, unused2, unused3) -> {
        });
        passwordField.textProperty().addListener((unused1, unused2, unused3) -> {
        });
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleLoginInputChanged() {
        String usernameText = usernameField.getText();
        String passwordText = passwordField.getText();
        // process login inputs
        try {
            CommandResult commandResult;
            commandResult = logic.execute(String.format(GUI_LOGIN_COMMAND_FORMAT, usernameText, passwordText));
            raise(new NewResultAvailableEvent(commandResult.feedbackToUser, false));
        } catch (CommandException | ParseException e) {
            raise(new NewResultAvailableEvent(e.getMessage(), true));
        }
        if (isLoggedIn()) {
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    /**
     * Handles the key press event, {@code keyEvent}.
     */
    @FXML
    private void handleBackToCommandView() {
        EventsCenter.getInstance().post(new ChangeToCommandBoxView());
    }

    public static void setShowingLoginView(boolean val) {
        showingLoginView = val;
    }

    public static boolean isShowingLoginView() {
        return showingLoginView;
    }
}
