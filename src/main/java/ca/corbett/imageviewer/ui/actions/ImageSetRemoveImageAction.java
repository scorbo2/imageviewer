package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Removes the currently showing image from the current ImageSet.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetRemoveImageAction extends AbstractAction {

    private static final Logger log = Logger.getLogger(ImageSetRemoveImageAction.class.getName());

    public ImageSetRemoveImageAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Make sure some image is displayed:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Remove image from image set", "No image selected.");
            return;
        }

        // Make sure some ImageSet is selected:
        ImageSet imageSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet().orElse(null);
        if (imageSet == null) {
            MainWindow.getInstance().showMessageDialog("Remove image from image set", "No Image set selected.");
            return;
        }

        imageSet.removeImageFilePath(currentImage.getImageFile().getAbsolutePath());
        log.log(Level.INFO, "removeFromImageSet: {0} removed from image set {1}",
                new Object[]{currentImage.getImageFile().getAbsolutePath(), imageSet.getFullyQualifiedName()});
        MainWindow.getInstance().selectedImageRemoved();
    }
}
