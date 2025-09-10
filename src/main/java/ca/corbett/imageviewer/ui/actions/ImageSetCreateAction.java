package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * An Action that can show a popup and create a new ImageSet with the user-supplied path and name.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetCreateAction extends AbstractAction {

    public ImageSetCreateAction() {
        super("Create new list...");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Rename", "Nothing selected.");
            return;
        }
        File file = currentImage.getImageFile();
        String name = JOptionPane.showInputDialog(MainWindow.getInstance(), "Enter name for new list:");
        if (name != null) {
            ImageSet imageSetOptional = ImageSetManager.getInstance().findOrCreateImageSet(name);
            if (imageSetOptional.addImageFilePath(file.getAbsolutePath())) {
                ReloadUIAction.getInstance().actionPerformed(actionEvent); // TODO overkill... just reload menus
            }
            MainWindow.getInstance().getImageSetPanel().resync();
        }
    }
}
