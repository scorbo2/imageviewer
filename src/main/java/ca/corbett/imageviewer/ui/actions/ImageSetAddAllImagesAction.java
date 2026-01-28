package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * An action to add all images in the currently selected directory to a new image set, to be
 * chosen by the user via popup dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetAddAllImagesAction extends EnhancedAction {

    private static final String NAME = "Add all images in this directory to image set...";
    private static final String SHORT_NAME = "Add all images to image set";

    public ImageSetAddAllImagesAction() {
        super(NAME);
        setTooltip(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        File currentDir = MainWindow.getInstance().getCurrentDirectory();
        if (currentDir == null) {
            MainWindow.getInstance().showMessageDialog(SHORT_NAME, "No directory selected.");
            return;
        }

        List<File> imageList = MainWindow.getInstance().getCurrentFileList();
        if (imageList.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(SHORT_NAME, "Nothing to add.");
            return;
        }

        // Note: the user can manually enter a path that already exists.
        //       This is not an error. We'll just add all images to that existing image set.
        ImageSetChooserDialog dialog = new ImageSetChooserDialog(SHORT_NAME, false);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            ImageSet imageSet = MainWindow.getInstance().getImageSetManager()
                                          .findOrCreateImageSet(dialog.getSelectedPath());
            for (File file : imageList) {
                imageSet.addImageFilePath(file.getAbsolutePath());
            }
            MainWindow.getInstance().getImageSetPanel().resync();
            MainWindow.getInstance().rebuildMenus();
        }
    }
}
