package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Shows a confirmation prompt and then starts a thumbnail generation thread on the currently selected
 * directory. Does nothing if no directory is selected or if the current browse mode is ImageSet.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
class ThumbCachePregenerateAction extends AbstractAction {

    public ThumbCachePregenerateAction() {
        super("Pre-generate thumbnails...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (MainWindow.getInstance().getBrowseMode() == MainWindow.BrowseMode.IMAGE_SET) {
            MainWindow.getInstance().showMessageDialog("Pregenerate thumbnails",
                                                       "This option is only available when browsing the file system.");
            return;
        }
        if (MainWindow.getInstance().getCurrentDirectory() == null) {
            MainWindow.getInstance().showMessageDialog("Pregenerate thumbnails", "Nothing is selected.");
            return;
        }
        if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                          "Thumbnails will be pregenerated in this directory and all "
                                                  + "subdirectories. This may take some time to complete. Proceed?",
                                          "Confirm thumbnail generation",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            new Thread(new ThumbCachePregenerateThread(MainWindow.getInstance().getCurrentDirectory())).start();
        }
    }

}
