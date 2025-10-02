package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Shows the thumbnail cache stats.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ThumbCacheViewStatsAction extends AbstractAction {

    public ThumbCacheViewStatsAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // We don't actually have a ViewStatsDialog, lol
        MainWindow.getInstance().showMessageDialog("Thumbnail cache stats",
                                                   ThumbCacheManager.gatherCacheStats().toString());
    }
}
