package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;

import java.awt.event.ActionEvent;

/**
 * Launches the ImageInformation dialog for the currently selected image, if there is one.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
class ImageInfoAction extends EnhancedAction {

    public ImageInfoAction() {
        super("Image information");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageInstance image = MainWindow.getInstance().getSelectedImage();
        if (image.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Image information", "No image selected.");
            return;
        }
        new ImageInfoDialog(MainWindow.getInstance(), image).setVisible(true);
    }
}
