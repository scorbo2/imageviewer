package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Represents an action to delete the currently selected ImageSet, if any.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetDeleteAction extends AbstractAction {

    public ImageSetDeleteAction(String name) {
        super(name);
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
