package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.MenuManager;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Removes the currently showing image from the current ImageSet.
 * This has no effect on the underlying image file - this is not a delete operation.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetRemoveImageAction extends EnhancedAction {

    private static final String NAME = "Remove this image from image set";

    private static final Logger log = Logger.getLogger(ImageSetRemoveImageAction.class.getName());

    public ImageSetRemoveImageAction() {
        this(MenuManager.MENU_ICON_SIZE);
    }

    public ImageSetRemoveImageAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);

        // Use the regular delete icon and not the scary nuke icon for image set item removals:
        setIcon(new ImageIcon(ImageViewerResources.getIconDelete(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Make sure some image is displayed:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(NAME, "No image selected.");
            return;
        }

        // Make sure some ImageSet is selected:
        ImageSet imageSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet().orElse(null);
        if (imageSet == null) {
            MainWindow.getInstance().showMessageDialog(NAME, "No Image set selected.");
            return;
        }

        imageSet.removeImageFilePath(currentImage.getImageFile().getAbsolutePath());
        log.log(Level.INFO, "removeFromImageSet: {0} removed from image set {1}",
                new Object[]{currentImage.getImageFile().getAbsolutePath(), imageSet.getFullyQualifiedName()});
        MainWindow.getInstance().selectedImageRemoved();
    }
}
