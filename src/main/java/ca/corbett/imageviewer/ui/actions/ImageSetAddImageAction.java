package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Adds the currently showing image (if any) to a specific ImageSet.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetAddImageAction extends AbstractAction {

    private final ImageSet imageSet;

    /**
     * Creates an ImageSetAddAction with the given ImageSet. Every time the action is invoked,
     * the currently showing image (if any) will be added to this ImageSet if it is not already there.
     */
    public ImageSetAddImageAction(ImageSet set) {
        super(set.getName());
        this.imageSet = set;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Add image to image set", "No image selected.");
            return;
        }
        File file = currentImage.getImageFile();
        if (imageSet.addImageFilePath(file.getAbsolutePath())) {
            MainWindow.getInstance().rebuildMenus();
        }
    }
}
