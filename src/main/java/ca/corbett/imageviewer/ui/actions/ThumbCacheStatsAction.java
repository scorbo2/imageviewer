package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.dialogs.ThumbCacheStatsDialog;

import java.awt.event.ActionEvent;

/**
 * An action for showing the ThumbCacheStatsDialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ThumbCacheStatsAction extends EnhancedAction {

    public ThumbCacheStatsAction() {
        super("Thumb cache stats...");
        setTooltip("View thumbnail cache statistics...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ThumbCacheStatsDialog(MainWindow.getInstance()).setVisible(true);
    }
}
