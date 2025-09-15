package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * An Action to switch browse modes to FILE_SYSTEM and select the source directory of
 * whatever image is currently showing.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetBrowseToSourceDirAction extends AbstractAction {

    public ImageSetBrowseToSourceDirAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Browse to source dir", "No image selected.");
            return;
        }
        File file = currentImage.getImageFile();
        MainWindow.getInstance().setDirectory(file.getParentFile());
    }
}
