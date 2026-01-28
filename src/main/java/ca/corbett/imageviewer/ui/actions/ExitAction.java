package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.MainWindow;

import java.awt.event.ActionEvent;

/**
 * An Action to exit the application.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ExitAction extends EnhancedAction {

    private static final String NAME = "Exit";

    public ExitAction() {
        super(NAME);
        setTooltip(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().dispose(); // the window listener there will clean up.
    }
}
