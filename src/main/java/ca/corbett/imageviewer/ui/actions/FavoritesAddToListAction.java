package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

public class FavoritesAddToListAction extends AbstractAction {

    private final ImageSet imageSet;

    public FavoritesAddToListAction(ImageSet set) {
        super(set.getName());
        this.imageSet = set;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Rename", "Nothing selected.");
            return;
        }
        File file = currentImage.getImageFile();
        if (imageSet.addImageFile(file)) {
            ReloadUIAction.getInstance().actionPerformed(actionEvent); // TODO overkill? we must rebuild the menu...
        }
    }
}
