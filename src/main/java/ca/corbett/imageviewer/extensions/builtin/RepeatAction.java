package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ImageOperationHandler;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * This action is a wrapper around ImageOperationHandler.repeatLastOperation().
 *
 * @author scorbo2
 */
public class RepeatAction extends AbstractAction {

    public RepeatAction() {
        super("Repeat last action");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageOperationHandler.repeatLastOperation();
    }

}
