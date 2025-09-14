package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetChooserDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class ImageSetAddAllImagesAction extends AbstractAction {

    public ImageSetAddAllImagesAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        File currentDir = MainWindow.getInstance().getCurrentDirectory();
        if (currentDir == null) {
            MainWindow.getInstance().showMessageDialog("Add all images to image set", "No directory selected.");
            return;
        }

        List<File> imageList = MainWindow.getInstance().getCurrentFileList();
        if (imageList.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Add all images to image set", "Nothing to add.");
            return;
        }

        ImageSetChooserDialog dialog = new ImageSetChooserDialog("Add all images to image set", false);
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
