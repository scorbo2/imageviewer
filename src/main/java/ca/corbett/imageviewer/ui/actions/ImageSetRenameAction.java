package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ImageSetRenameAction extends AbstractAction {

    public ImageSetRenameAction(String name) {
        super(name);
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
