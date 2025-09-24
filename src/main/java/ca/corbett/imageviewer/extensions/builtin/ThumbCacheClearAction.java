package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Prompts for confirmation and then clears the thumbnail cache.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ThumbCacheClearAction extends AbstractAction {

    public ThumbCacheClearAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                          "Are you sure you wish to clear cache? This can't be undone.",
                                          "Confirm",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            ThumbCacheManager.clear();
            MainWindow.getInstance().showMessageDialog("Thumbnail cache cleared", "Cache cleared!");
        }
    }
}
