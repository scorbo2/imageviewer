package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.dialogs.QuickMoveDialog;

import java.awt.event.ActionEvent;

/**
 * An action to show quick move options for the currently selected image.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class QuickMoveEditAction extends EnhancedAction {

    private static final String NAME = "Configure Quick Move destinations...";

    public QuickMoveEditAction() {
        super(NAME);
        setTooltip(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new QuickMoveDialog().setVisible(true);
    }
}
