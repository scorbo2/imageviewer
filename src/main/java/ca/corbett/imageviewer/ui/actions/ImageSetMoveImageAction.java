package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.MenuManager;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An Action to move or copy the currently selected image, if any, to a new
 * path to be chosen by the user via popup dialog. The chosen destination
 * path may or may not already exist; it is created if needed.
 * The source image can be optionally removed after the operation completes.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetMoveImageAction extends EnhancedAction {

    private static final String NAME_MOVE = "Move this image to other image set...";
    private static final String NAME_COPY = "Copy this image to other image set...";

    private static final Logger log = Logger.getLogger(ImageSetMoveImageAction.class.getName());

    private final boolean removeSourceImage;

    public ImageSetMoveImageAction(boolean removeSourceImage) {
        this(removeSourceImage, MenuManager.MENU_ICON_SIZE);
    }

    public ImageSetMoveImageAction(boolean removeSourceImage, int iconSize) {
        super(removeSourceImage ? NAME_MOVE : NAME_COPY);
        setTooltip(removeSourceImage ? NAME_MOVE : NAME_COPY);
        this.removeSourceImage = removeSourceImage;
        setIcon(new ImageIcon(ImageViewerResources.getIconMoveItem(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Make sure some image is displayed:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(getTooltip(), "No image selected.");
            return;
        }

        // Make sure some ImageSet is selected:
        ImageSet sourceSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet().orElse(null);
        if (sourceSet == null) {
            MainWindow.getInstance().showMessageDialog(getTooltip(), "No Image set selected.");
            return;
        }

        ImageSetChooserDialog dialog = new ImageSetChooserDialog("Select or create image set", false);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            ImageSet destinationSet = MainWindow.getInstance().getImageSetManager()
                                                .findOrCreateImageSet(dialog.getSelectedPath());
            if (sourceSet.equals(destinationSet)) {
                MainWindow.getInstance().showMessageDialog(getTooltip(), "Source and destination are the same.");
            }

            String opName = removeSourceImage ? "moveImageToImageSet" : "copyImageToImageSet";
            destinationSet.addImageFilePath(currentImage.getImageFile().getAbsolutePath());
            if (removeSourceImage) {
                sourceSet.removeImageFilePath(currentImage.getImageFile().getAbsolutePath());
            }
            log.log(Level.INFO, "{0}: {1} from {2} -> {3}",
                    new Object[]{opName,
                            currentImage.getImageFile().getName(),
                            sourceSet.getFullyQualifiedName(),
                            destinationSet.getFullyQualifiedName()});

            MainWindow.getInstance().getImageSetPanel().resync();
            MainWindow.getInstance().rebuildMenus();
        }
    }
}
