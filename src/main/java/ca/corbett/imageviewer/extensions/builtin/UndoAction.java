package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ImageOperationHandler;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * This action is a wrapper around ImageOperationHandler.undoLastOperation().
 * Some actions, such as file deletes, can't be undone via this action.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class UndoAction extends AbstractAction {

    public UndoAction() {
        super("Undo last action");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageOperationHandler.undoLastOperation();
    }
}
