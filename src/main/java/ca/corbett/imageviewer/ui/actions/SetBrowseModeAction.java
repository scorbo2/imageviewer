package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.MainWindow;

import java.awt.event.ActionEvent;

/**
 * An action to set the current browse mode when invoked.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class SetBrowseModeAction extends EnhancedAction {

    private final MainWindow.BrowseMode targetMode;

    public SetBrowseModeAction(MainWindow.BrowseMode mode) {
        super("Set browse mode to " + mode.name());
        this.targetMode = mode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().setBrowseMode(targetMode, true);
    }
}
