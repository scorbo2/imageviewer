package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.logging.LogConsole;

import java.awt.event.ActionEvent;

/**
 * An Action to show the log console.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class LogConsoleAction extends EnhancedAction {

    private static final String NAME = "Log console";

    public LogConsoleAction() {
        super(NAME);
        setTooltip(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LogConsole.getInstance().setVisible(true);
    }
}
