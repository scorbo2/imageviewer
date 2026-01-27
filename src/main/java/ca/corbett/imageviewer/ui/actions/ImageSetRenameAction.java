package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * An Action to rename or move the currently selected image set, if any, to a new path
 * to be selected by the user via popup dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetRenameAction extends EnhancedAction {

    private static final String NAME = "Rename/move this image set";

    public ImageSetRenameAction() {
        this(AppConfig.getInstance().getMiniToolbarIconSize());
    }

    public ImageSetRenameAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconMoveItem(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String selectedPath = MainWindow.getInstance().getImageSetPanel().getSelectedPath();
        if (selectedPath == null || selectedPath.length() <= 1) {
            MainWindow.getInstance().showMessageDialog("Move image set", "No Image set selected.");
            return;
        }

        // Is this branch locked?
        ImageSetManager imageSetManager = MainWindow.getInstance().getImageSetManager();
        if (imageSetManager.isBranchLocked(selectedPath)) {
            MainWindow.getInstance().showMessageDialog("Move image set",
                                                       "One ore more image sets in this branch of "
                                                               + "the tree are locked and cannot be moved.");
            return;
        }

        ImageSetChooserDialog dialog = new ImageSetChooserDialog("Choose new path", true);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            imageSetManager.renameBranch(selectedPath, dialog.getSelectedPath());

            MainWindow.getInstance().getImageSetPanel().resync();
            MainWindow.getInstance().rebuildMenus();
        }
    }
}
