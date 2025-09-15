package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Takes the currently selected image, if any, and adds it to a new ImageSet with a path
 * to be chosen by the user via popup dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetCreateAction extends AbstractAction {

    public ImageSetCreateAction() {
        super("Create new list...");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Rename", "Nothing selected.");
            return;
        }
        File file = currentImage.getImageFile();
        ImageSetChooserDialog dialog = new ImageSetChooserDialog("Create new image set");
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            ImageSet imageSet = MainWindow.getInstance().getImageSetManager()
                                          .findOrCreateImageSet(dialog.getSelectedPath());
            imageSet.addImageFilePath(file.getAbsolutePath());
            MainWindow.getInstance().getImageSetPanel().resync();
            MainWindow.getInstance().rebuildMenus();
        }
    }
}
