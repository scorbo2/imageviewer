package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Represents an action to delete the currently selected ImageSet, if any.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetDeleteAction extends EnhancedAction {

    private static final String NAME = "Delete this image set";

    public ImageSetDeleteAction() {
        this(AppConfig.getInstance().getMiniToolbarIconSize());
    }

    public ImageSetDeleteAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);

        // Debatable, but let's not use the scary red nuke icon for image set deletions.
        // We'll use the regular delete icon instead, and reserve the nuke icon for operations
        // that actually remove image files from disk. This way they are visually distinct.
        setIcon(new ImageIcon(ImageViewerResources.getIconDelete(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String selectedPath = MainWindow.getInstance().getImageSetPanel().getSelectedPath();
        if (selectedPath == null || selectedPath.length() <= 1) {
            MainWindow.getInstance().showMessageDialog("Delete image set", "Nothing selected.");
            return;
        }

        // Is this branch locked?
        ImageSetManager imageSetManager = MainWindow.getInstance().getImageSetManager();
        if (imageSetManager.isBranchLocked(selectedPath)) {
            MainWindow.getInstance().showMessageDialog("Delete image set",
                                                       "One ore more image sets in this branch of "
                                                       + "the tree are locked and cannot be deleted.");
            return;
        }

        // Nuke it:
        imageSetManager.remove(selectedPath);

        // Reload the tree
        MainWindow.getInstance().getImageSetPanel().resync();
        MainWindow.getInstance().rebuildMenus();
        MainWindow.getInstance().setImageSet(null);
    }
}
