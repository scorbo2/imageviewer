package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ImageOperationHandler;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;

import java.awt.event.ActionEvent;

/**
 * Invokes the default "delete" action for the current browse mode.
 * In filesystem mode, this will delete the currently selected image.
 * In image set mode, this will remove the currently selected image from the set.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class DeleteCurrentAction extends EnhancedAction {

    private static final String NAME = "Delete";
    private final ImageSetRemoveImageAction removeFromImageSetAction = new ImageSetRemoveImageAction();

    public DeleteCurrentAction() {
        super(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage == null) {
            MainWindow.getInstance().showMessageDialog("Nothing selected", "There is no image selected.");
            return;
        }

        // In filesystem mode, we can delegate to the ImageOperationHandler:
        if (MainWindow.getInstance().getBrowseMode() == MainWindow.BrowseMode.FILE_SYSTEM) {
            ImageOperationHandler.deleteImage();
        }

        // In image set mode, we can delegate to the ImageSetRemoveImageAction:
        else {
            removeFromImageSetAction.actionPerformed(e);
        }
    }
}
