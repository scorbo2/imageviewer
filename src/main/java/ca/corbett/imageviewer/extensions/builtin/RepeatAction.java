package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ImageOperationHandler;

import java.awt.event.ActionEvent;

/**
 * This action is a wrapper around ImageOperationHandler.repeatLastOperation().
 * If the last ImageOperation was repeatable, this action will do it again with the current image.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class RepeatAction extends EnhancedAction {

    public RepeatAction() {
        super("Repeat last action");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageOperationHandler.repeatLastOperation();
    }
}
