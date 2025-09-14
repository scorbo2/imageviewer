package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
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
        // Make sure some ImageSet is selected:
        ImageSet sourceSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet().orElse(null);
        if (sourceSet == null) {
            MainWindow.getInstance().showMessageDialog("Move image set", "No Image set selected.");
            return;
        }


        // Is this branch locked?
        ImageSetManager imageSetManager = MainWindow.getInstance().getImageSetManager();
        if (imageSetManager.isBranchLocked(sourceSet.getFullyQualifiedName())) {
            MainWindow.getInstance().showMessageDialog("Move image set",
                                                       "One ore more image sets in this branch of "
                                                               + "the tree are locked and cannot be moved.");
            return;
        }

        ImageSetChooserDialog dialog = new ImageSetChooserDialog("Choose new path", true);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            ImageSetManager manager = MainWindow.getInstance().getImageSetManager();
            ImageSet newImageSet = manager.findOrCreateImageSet(dialog.getSelectedPath());
            if (sourceSet.equals(newImageSet)) {
                MainWindow.getInstance().showMessageDialog("Move image set", "Source and destination are the same.");
            }

            for (String filePath : sourceSet.getImageFilePaths()) {
                newImageSet.addImageFilePath(filePath);
            }
            manager.remove(sourceSet);

            MainWindow.getInstance().getImageSetPanel().resync();
            MainWindow.getInstance().rebuildMenus();
        }


    }
}
