package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * An Action to switch browse modes to FILE_SYSTEM and select the source directory of
 * whatever image is currently showing.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetBrowseToSourceDirAction extends EnhancedAction {

    private static final String NAME = "Browse to source dir";

    public ImageSetBrowseToSourceDirAction() {
        super(NAME);
        setTooltip(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(NAME, "No image selected.");
            return;
        }
        File file = currentImage.getImageFile();
        MainWindow.getInstance().setDirectory(file.getParentFile());
    }
}
