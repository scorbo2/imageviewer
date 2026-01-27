package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.UIReloadable;
import ca.corbett.imageviewer.ui.actions.ReloadUIAction;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Shows a confirmation prompt and then starts a thumbnail generation thread on the currently selected
 * directory. Does nothing if no directory is selected or if the current browse mode is ImageSet.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
class ThumbCachePregenerateAction extends EnhancedAction implements UIReloadable {

    public ThumbCachePregenerateAction() {
        super("Pre-generate thumbnails...");
        setTooltip("Pre-generate thumbnails...");
        setIcon(new ImageIcon(ImageViewerResources.getIconThumbnails(AppConfig.getInstance().getToolbarIconSize())));
        ReloadUIAction.getInstance().registerReloadable(this);
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

    @Override
    public void reloadUI() {
        // Our icon size may have changed:
        setIcon(new ImageIcon(ImageViewerResources.getIconThumbnails(AppConfig.getInstance().getToolbarIconSize())));
    }
}
