package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * An Action to move the currently selected image, if any, to a new
 * path to be chosen by the user via popup dialog. The chosen destination
 * path may or may not already exist; it is created if needed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetMoveImageAction extends AbstractAction {

    private final boolean removeSourceImage;
    private final String actionLabel;

    public ImageSetMoveImageAction(String name, boolean removeSourceImage) {
        super(name);
        this.removeSourceImage = removeSourceImage;
        actionLabel = removeSourceImage ? "Move image to other image set" : "Copy image to other image set";
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Make sure some image is displayed:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(actionLabel, "No image selected.");
            return;
        }

        // Make sure some ImageSet is selected:
        ImageSet sourceSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet().orElse(null);
        if (sourceSet == null) {
            MainWindow.getInstance().showMessageDialog(actionLabel, "No Image set selected.");
            return;
        }

        ImageSetChooserDialog dialog = new ImageSetChooserDialog("Select or create image set", false);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            ImageSet destinationSet = MainWindow.getInstance().getImageSetManager()
                                                .findOrCreateImageSet(dialog.getSelectedPath());
            if (sourceSet.equals(destinationSet)) {
                MainWindow.getInstance().showMessageDialog(actionLabel, "Source and destination are the same.");
            }

            destinationSet.addImageFilePath(currentImage.getImageFile().getAbsolutePath());
            if (removeSourceImage) {
                sourceSet.removeImageFilePath(currentImage.getImageFile().getAbsolutePath());
            }

            MainWindow.getInstance().getImageSetPanel().resync();
            MainWindow.getInstance().rebuildMenus();
        }

    }
}
