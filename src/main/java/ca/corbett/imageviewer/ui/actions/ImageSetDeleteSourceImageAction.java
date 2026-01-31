package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ImageOperationHandler;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.MenuManager;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Represents an action to delete the currently showing image in the currently selected image set.
 * This is NOT the same as ImageSetRemoveImageAction! That action merely removes the image from
 * the image set. This action will REMOVE the image file from disk!
 * <p>
 * If there is no image selected, or if there is no image set selected, this action will
 * display an error message and do nothing.
 * </p>
 * <p>
 * A confirmation dialog is shown before proceeding with the delete operation.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 3.0
 */
public class ImageSetDeleteSourceImageAction extends EnhancedAction {

    private static final String NAME = "Delete source image";

    public ImageSetDeleteSourceImageAction() {
        this(MenuManager.MENU_ICON_SIZE);
    }

    public ImageSetDeleteSourceImageAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);

        // Use the scary red icon and not the regular delete icon, as this will delete the actual image file:
        setIcon(new ImageIcon(ImageViewerResources.getIconNuke(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Make sure our browse mode is correct:
        // (The action is not visible in filesystem mode, so this *shouldn't* be possible, but just in case...)
        if (MainWindow.getInstance().getBrowseMode() != MainWindow.BrowseMode.IMAGE_SET) {
            MainWindow.getInstance().showMessageDialog(NAME, "This action can only be used in ImageSet browse mode.");
            return;
        }

        // Make sure some image is displayed:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(NAME, "No image selected.");
            return;
        }

        // Prompt to confirm first!
        if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                          "Are you sure you want to permanently delete this image from disk?\n"
                                                  + "The image will be removed from all image sets, and the file will be deleted.\n"
                                                  + "This action cannot be undone.",
                                          "Confirm delete source image",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        // We can entirely delegate to the ImageOperationHandler for this. It will remove the image file,
        // update the thumbnail cache, update all affected image sets, and refresh the UI as needed.
        ImageOperationHandler.deleteImage();
    }
}
