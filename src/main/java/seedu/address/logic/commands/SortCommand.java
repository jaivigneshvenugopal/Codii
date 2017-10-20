package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.exceptions.CommandException;

//@@author khooroko
/**
 * Sorts the masterlist by the input argument (i.e. "name" or "debt").
 */
public class SortCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_SUCCESS = "Address book has been sorted by %1$s!";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the addressbook by specified ordering.\n"
            + "Parameters: ORDERING (i.e. \"name\" or \"debt\")\n"
            + "Example: " + COMMAND_WORD + " name";

    private final String order;

    public SortCommand(String order) {
        //validity of order to sort is checked in {@code SortCommandParser}
        this.order = order;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        try {
            model.sortBy(order);
        } catch (IllegalValueException ive) {
            throw new CommandException(ive.getMessage());
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, order));
    }
}
